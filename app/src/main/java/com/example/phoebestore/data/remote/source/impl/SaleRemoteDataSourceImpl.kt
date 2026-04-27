package com.example.phoebestore.data.remote.source.impl

import com.example.phoebestore.data.remote.dto.SaleDto
import com.example.phoebestore.data.remote.source.SaleRemoteDataSource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject

class SaleRemoteDataSourceImpl @Inject constructor(
    private val supabase: SupabaseClient
) : SaleRemoteDataSource {

    override suspend fun getByStore(storeId: Long): List<SaleDto> =
        supabase.from("sales")
            .select { filter { eq("store_id", storeId) } }
            .decodeList()

    override suspend fun getById(id: Long): SaleDto? =
        supabase.from("sales")
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull()

    override suspend fun insert(dto: SaleDto) {
        supabase.from("sales").insert(dto)
    }

    override suspend fun update(dto: SaleDto) {
        supabase.from("sales").update(dto) {
            filter { eq("id", dto.id) }
        }
    }

    override suspend fun delete(id: Long) {
        supabase.from("sales").delete {
            filter { eq("id", id) }
        }
    }
}
