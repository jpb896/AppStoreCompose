package com.jpb.appstore.utils

data class AppItem(
    val id: String,
    val name: String,
    val developer: String,
    val repo: String,
    val size: String,
    val version: String,
    val category: String,
    val iconUrl: String,
    val state: InstallState = InstallState.Idle,
    val progress: Float = 0f,
    val hasUpdate: Boolean = false,
    val newVersion: String? = null
)

sealed interface InstallState {
    object Idle : InstallState
    data class Downloading(val progress: Float) : InstallState
    object Installing : InstallState
    object Installed : InstallState
}

data class RepoItem(
    val name: String,
    val url: String,
    val appCount: Int,
    val isActive: Boolean = true
)