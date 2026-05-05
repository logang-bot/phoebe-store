package com.example.phoebestore.data.sync

interface EntitySyncer {
    suspend fun syncWrite(entityId: Long)
    suspend fun syncDelete(entityId: Long)
}
