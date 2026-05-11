package com.example.phoebestore.data.sync

interface EntitySyncer {
    suspend fun syncWrite(entityId: String)
    suspend fun syncDelete(entityId: String)
}
