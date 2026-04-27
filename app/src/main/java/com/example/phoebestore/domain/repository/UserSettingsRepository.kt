package com.example.phoebestore.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {
    val lastAccessedStoreId: Flow<Long?>
    suspend fun setLastAccessedStore(storeId: Long)
}
