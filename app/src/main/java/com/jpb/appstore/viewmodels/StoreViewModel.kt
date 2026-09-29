package com.jpb.appstore.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jpb.appstore.data.CustomRepositorySource
import com.jpb.appstore.data.FdroidRepositorySource
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
    private val fdroidSource = FdroidRepositorySource()
    private val customSource = CustomRepositorySource()

    private val _apps = MutableStateFlow<List<AppItem>>(emptyList())
    val apps: StateFlow<List<AppItem>> = _apps.asStateFlow()

    private val _repositories = MutableStateFlow<List<RepositoryItem>>(
        listOf(
            RepositoryItem("F-Droid Official", "https://f-droid.org/repo", 0),
            RepositoryItem("Custom Repository", "https://my-custom-repo.com/index.json", 0)
        )
    )
    val repositories: StateFlow<List<RepositoryItem>> = _repositories.asStateFlow()

    init {
        loadAllRepositories()
    }

    fun loadAllRepositories() {
        viewModelScope.launch {
            try {
                // Fetch from both F-Droid and your custom repo concurrently or sequentially
                val fdroidApps = fdroidSource.fetchApps("https://f-droid.org/repo")
                val customApps = customSource.fetchApps("https://my-custom-repo.com/index.json")

                val combinedApps = fdroidApps + customApps
                _apps.value = combinedApps

                // Update repository item app counts dynamically
                _repositories.value = listOf(
                    RepositoryItem("CaesiumOS/VouloirOS System Apps Repo", "https://my-custom-repo.com/index.json", customApps.size),
                    RepositoryItem("F-Droid Official", "https://f-droid.org/repo", fdroidApps.size)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchRealApps(repoUrl: String) {
        viewModelScope.launch {
            try {
                val fetchedApps = if (repoUrl.contains("f-droid.org")) {
                    fdroidSource.fetchApps(repoUrl)
                } else {
                    customSource.fetchApps(repoUrl)
                }

                if (fetchedApps.isNotEmpty()) {
                    val currentApps = _apps.value.filter { it.repo != repoUrl }
                    _apps.value = currentApps + fetchedApps

                    _repositories.value = _repositories.value.map { repo ->
                        if (repo.url == repoUrl) repo.copy(appCount = fetchedApps.size) else repo
                    }
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