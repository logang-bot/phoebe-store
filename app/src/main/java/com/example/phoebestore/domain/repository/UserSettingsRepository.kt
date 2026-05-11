package com.example.phoebestore.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {
    val lastAccessedStoreId: Flow<String?>
    suspend fun setLastAccessedStore(storeId: String)
}
