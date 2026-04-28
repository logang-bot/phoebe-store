package com.example.phoebestore.data.remote.source.impl

import com.example.phoebestore.data.remote.dto.StoreDto
import com.example.phoebestore.data.remote.source.StoreRemoteDataSource
import com.example.phoebestore.data.sync.DeviceIdProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject

class StoreRemoteDataSourceImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val deviceIdProvider: DeviceIdProvider
) : StoreRemoteDataSource {

    override suspend fun getAll(): List<StoreDto> =
        supabase.from("stores")
            .select { filter { eq("device_id", deviceIdProvider.id) } }
            .decodeList()

    override suspend fun getById(id: Long): StoreDto? =
        supabase.from("stores")
            .select { filter { eq("id", id); eq("device_id", deviceIdProvider.id) } }
            .decodeSingleOrNull()

    override suspend fun insert(dto: StoreDto) {
        supabase.from("stores").upsert(dto)
    }

    override suspend fun update(dto: StoreDto) {
        supabase.from("stores").update(dto) {
            filter { eq("id", dto.id) }
        }
    }

    override suspend fun delete(id: Long) {
        supabase.from("stores").delete {
            filter { eq("id", id) }
        }
    }
}
