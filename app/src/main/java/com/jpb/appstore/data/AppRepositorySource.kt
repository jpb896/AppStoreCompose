package com.jpb.appstore.data

import com.jpb.appstore.utils.AppItem

interface AppRepositorySource {
    suspend fun fetchApps(repoUrl: String): List<AppItem>
}