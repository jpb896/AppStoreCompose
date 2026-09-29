package com.jpb.appstore.data

import com.jpb.appstore.utils.AppItem
import com.jpb.appstore.utils.InstallState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.net.HttpURLConnection
import java.net.URL

data class AppRelease(
    val versionName: String?,
    val versionCode: Long?,
    val minSdk: Int?,
    val targetSdk: Int?,
    val downloadUrl: String?,
    val sha256Hash: String?,
    val sizeBytes: Long?
)

data class FdroidAppItem(
    val packageId: String,
    val name: String,
    val summary: String,
    val categories: List<String>,
    val suggestedVersionCode: Long?,
    val releases: List<AppRelease>
)

class FdroidRepoParser {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun parseRepo(repoUrl: String = "https://f-droid.org"): List<FdroidAppItem> = withContext(Dispatchers.IO) {
        val indexUrl = "$repoUrl/index-v2.json"
        val url = URL(indexUrl)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            setRequestProperty("User-Agent", "Mozilla/5.0 (YourAppStoreClient/1.0)")
            connectTimeout = 15000
            readTimeout = 15000
        }

        val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
        val rootElement = json.parseToJsonElement(jsonString).jsonObject

        val packages = rootElement["packages"]?.jsonObject ?: emptyMap()
        val versions = rootElement["versions"]?.jsonObject ?: emptyMap()

        val parsedStoreData = mutableListOf<FdroidAppItem>()

        for ((pkgId, pkgElement) in packages) {
            val pkgMeta = pkgElement.jsonObject

            // Extract localized name (defaults to English or first available)
            val nameObj = pkgMeta["name"]?.jsonObject
            val appName = nameObj?.get("en-US")?.jsonPrimitive?.content
                ?: nameObj?.get("en")?.jsonPrimitive?.content
                ?: nameObj?.values?.firstOrNull()?.jsonPrimitive?.content
                ?: "Unknown App"

            // Extract localized summary
            val summaryObj = pkgMeta["summary"]?.jsonObject
            val summary = summaryObj?.get("en-US")?.jsonPrimitive?.content
                ?: summaryObj?.get("en")?.jsonPrimitive?.content
                ?: summaryObj?.values?.firstOrNull()?.jsonPrimitive?.content
                ?: ""

            val categories = pkgMeta["categories"]?.jsonArray?.mapNotNull { it.jsonPrimitive.content } ?: emptyList()
            val suggestedVersionCode = pkgMeta["suggestedVersionCode"]?.jsonPrimitive?.longOrNull

            // 1. Explicitly type the list
            val releasesList = mutableListOf<AppRelease>()

            // 2. Ensure we explicitly extract it as a JsonArray (or default to an empty list)
            val pkgVersions = versions[pkgId]?.jsonArray ?: emptyList()

            for (versionElement in pkgVersions) {
                val versionObj = versionElement.jsonObject
                val manifest = versionObj["manifest"]?.jsonObject
                val fileInfo = versionObj["file"]?.jsonObject

                val apkRelativePath = fileInfo?.get("name")?.jsonPrimitive?.content?.removePrefix("/") ?: ""
                val downloadUrl = if (apkRelativePath.isNotEmpty()) "$repoUrl/$apkRelativePath" else null

                releasesList.add(
                    AppRelease(
                        versionName = manifest?.get("versionName")?.jsonPrimitive?.content,
                        versionCode = manifest?.get("versionCode")?.jsonPrimitive?.longOrNull,
                        minSdk = manifest?.get("minSdkVersion")?.jsonPrimitive?.intOrNull,
                        targetSdk = manifest?.get("targetSdkVersion")?.jsonPrimitive?.intOrNull,
                        downloadUrl = downloadUrl,
                        sha256Hash = fileInfo?.get("sha256")?.jsonPrimitive?.content,
                        sizeBytes = fileInfo?.get("size")?.jsonPrimitive?.longOrNull
                    )
                )
            }

            parsedStoreData.add(
                FdroidAppItem(
                    packageId = pkgId,
                    name = appName,
                    summary = summary,
                    categories = categories,
                    suggestedVersionCode = suggestedVersionCode,
                    releases = releasesList
                )
            )
        }

        return@withContext parsedStoreData
    }
}

class FdroidRepositorySource : AppRepositorySource {
    private val parser = FdroidRepoParser()

    override suspend fun fetchApps(repoUrl: String): List<AppItem> {
        val fdroidApps = parser.parseRepo(repoUrl)
        return fdroidApps.map { fdroidApp ->
            val latestRelease = fdroidApp.releases.firstOrNull()
            val sizeMb = latestRelease?.sizeBytes?.let { "${it / (1024 * 1024)} MB" } ?: "15 MB"

            AppItem(
                id = fdroidApp.packageId,
                name = fdroidApp.name,
                developer = "F-Droid Developer",
                repo = repoUrl,
                size = sizeMb,
                version = latestRelease?.versionName ?: "1.0",
                category = fdroidApp.categories.firstOrNull() ?: "General",
                iconUrl = "",
                state = InstallState.Idle,
                hasUpdate = false
            )
        }
    }
}