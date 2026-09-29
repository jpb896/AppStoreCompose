package com.jpb.appstore.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jpb.appstore.data.FdroidRepoParser
import com.jpb.appstore.utils.AppItem
import com.jpb.appstore.utils.InstallState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Define RepositoryItem if not already defined in your utils package
data class RepositoryItem(
    val name: String,
    val url: String,
    val appCount: Int = 0
)

class StoreViewModel : ViewModel() {
    private val parser = FdroidRepoParser()

    private val _apps = MutableStateFlow<List<AppItem>>(emptyList())
    val apps: StateFlow<List<AppItem>> = _apps.asStateFlow()

    private val _repositories = MutableStateFlow<List<RepositoryItem>>(
        listOf(RepositoryItem("F-Droid Official", "https://f-droid.org/repo", 0))
    )
    val repositories: StateFlow<List<RepositoryItem>> = _repositories.asStateFlow()

    init {
        fetchRealApps("https://f-droid.org/repo")
    }

    fun fetchRealApps(repoUrl: String) {
        viewModelScope.launch {
            try {
                val fdroidApps = parser.parseRepo(repoUrl)

                val mappedApps = fdroidApps.map { fdroidApp ->
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

                if (mappedApps.isNotEmpty()) {
                    _apps.value = mappedApps
                    // Update repository item app count dynamically
                    _repositories.value = listOf(
                        RepositoryItem("F-Droid Official", repoUrl, mappedApps.size)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startDownload(appId: String) {
        viewModelScope.launch {
            _apps.value = _apps.value.map { app ->
                if (app.id == appId) {
                    app.copy(state = InstallState.Downloading(0.1f))
                } else {
                    app
                }
            }
            // You can add your actual download logic or progress simulation here
        }
    }

    fun cancelAction(appId: String) {
        viewModelScope.launch {
            _apps.value = _apps.value.map { app ->
                if (app.id == appId) {
                    app.copy(state = InstallState.Idle)
                } else {
                    app
                }
            }
        }
    }
}