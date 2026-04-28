package com.example.phoebestore.data.remote.storage

import com.example.phoebestore.data.sync.RemoteErrorHandler
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageUploader @Inject constructor(
    private val supabase: SupabaseClient,
    private val errorHandler: RemoteErrorHandler
) {
    suspend fun resolveUrl(localUri: String, bucket: String, remotePath: String): String {
        if (localUri.isBlank() || localUri.startsWith("https://")) return localUri
        return runCatching {
            val bytes = withContext(Dispatchers.IO) { File(URI.create(localUri)).readBytes() }
            supabase.storage.from(bucket).upload(remotePath, bytes) { upsert = true }
            supabase.storage.from(bucket).publicUrl(remotePath)
        }.onFailure { errorHandler.log("ImageUpload", it) }
         .getOrDefault(localUri)
    }
}
