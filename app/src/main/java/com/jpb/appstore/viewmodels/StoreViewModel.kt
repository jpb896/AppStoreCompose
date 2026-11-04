package com.jpb.appstore.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jpb.appstore.utils.AppItem
import com.jpb.appstore.utils.InstallState
import com.jpb.appstore.utils.RepoItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StoreViewModel : ViewModel() {

    // UI States
    private val _apps = MutableStateFlow<List<AppItem>>(emptyList())
    val apps: StateFlow<List<AppItem>> = _apps.asStateFlow()

    private val _repositories = MutableStateFlow<List<RepoItem>>(emptyList())
    val repositories: StateFlow<List<RepoItem>> = _repositories.asStateFlow()

    init {
        loadInitialMockData()
    }

    enum class CardLayoutType {
        HERO_BANNER,
        STANDARD_LIST,
        HORIZONTAL_GRID
    }

    private fun loadInitialMockData() {
        _apps.value = listOf(
            AppItem(
                id = "neoterm",
                name = "NeoTerm Linux Terminal",
                developer = "CustomOS Foundation",
                repo = "F-Droid Core",
                size = "24.5 MB",
                version = "v2.1.0",
                category = "Development",
                iconUrl = "https://placehold.co/120x120/6750A4/FFFFFF?text=NT",
                hasUpdate = true,
                newVersion = "v2.1.1"
            ),
            AppItem(
                id = "aurorafirewall",
                name = "NetGuard Pro Firewall",
                developer = "Privacy Tools Lab",
                repo = "Guardian Project",
                size = "8.1 MB",
                version = "v1.9.4",
                category = "Privacy",
                iconUrl = "https://placehold.co/120x120/21005D/FFFFFF?text=NG",
                hasUpdate = true,
                newVersion = "v1.9.6"
            ),
            AppItem(
                id = "vlcmedia",
                name = "VLC Media Player",
                developer = "VideoLAN",
                repo = "F-Droid Official",
                size = "42.0 MB",
                version = "v3.5.8",
                category = "Media",
                iconUrl = "https://placehold.co/120x120/FF7A00/FFFFFF?text=VLC",
                state = InstallState.Installed,
                progress = 1f
            )
        )

        _repositories.value = listOf(
            RepoItem("F-Droid Official Main", "mirror.fcix.net/fdroid/repo", 1840),
            RepoItem("Guardian Project", "guardianproject.info/fdroid/repo", 42),
            RepoItem("CustomOS System Repo", "repo.customos.org/v2/index.json", 120)
        )
    }

    // Handle App Installation / Download flow
    fun startDownload(appId: String) {
        viewModelScope.launch {
            updateAppState(appId, InstallState.Downloading(0.0f))

            // Simulate download progress loop (Replace this with actual Ktor/Retrofit + PackageInstaller logic)
            for (i in 1..5) {
                delay(300)
                val progress = i * 0.2f
                updateAppState(appId, InstallState.Downloading(progress))
            }

            // Move to Installing state
            updateAppState(appId, InstallState.Installing)
            delay(1000)

            // Finalize installation
            _apps.update { list ->
                list.map { app ->
                    if (app.id == appId) {
                        app.copy(
                            state = InstallState.Installed,
                            hasUpdate = false,
                            version = app.newVersion ?: app.version
                        )
                    } else app
                }
            }
        }
    }

    fun cancelAction(appId: String) {
        updateAppState(appId, InstallState.Idle, 0f)
    }

    private fun updateAppState(appId: String, newState: InstallState, newProgress: Float = 0f) {
        _apps.update { list ->
            list.map { app ->
                if (app.id == appId) {
                    app.copy(state = newState, progress = newProgress)
                } else app
            }
        }
    }

    // Handle Adding Custom Repositories
    fun addRepository(name: String, url: String) {
        val newRepo = RepoItem(name = name, url = url, appCount = (50..300).random())
        _repositories.update { it + newRepo }
    }

    fun removeRepository(index: Int) {
        _repositories.update { it.filterIndexed { idx, _ -> idx != index } }
    }
}