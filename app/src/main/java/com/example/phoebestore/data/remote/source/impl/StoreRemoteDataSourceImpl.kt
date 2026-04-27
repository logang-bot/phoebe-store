package com.example.phoebestore.data.remote.source.impl

import com.example.phoebestore.data.remote.dto.StoreDto
import com.example.phoebestore.data.remote.source.StoreRemoteDataSource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject

class StoreRemoteDataSourceImpl @Inject constructor(
    private val supabase: SupabaseClient
) : StoreRemoteDataSource {

    override suspend fun getAll(): List<StoreDto> =
        supabase.from("stores").select().decodeList()

    override suspend fun getById(id: Long): StoreDto? =
        supabase.from("stores")
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull()

    override suspend fun insert(dto: StoreDto) {
        supabase.from("stores").insert(dto)
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
