package com.example.phoebestore.data.remote.source.impl

import com.example.phoebestore.data.remote.dto.ProductDto
import com.example.phoebestore.data.remote.source.ProductRemoteDataSource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject

class ProductRemoteDataSourceImpl @Inject constructor(
    private val supabase: SupabaseClient
) : ProductRemoteDataSource {

    override suspend fun getByStore(storeId: Long): List<ProductDto> =
        supabase.from("products")
            .select { filter { eq("store_id", storeId) } }
            .decodeList()

    override suspend fun getById(id: Long): ProductDto? =
        supabase.from("products")
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull()

    override suspend fun insert(dto: ProductDto) {
        supabase.from("products").insert(dto)
    }

    override suspend fun update(dto: ProductDto) {
        supabase.from("products").update(dto) {
            filter { eq("id", dto.id) }
        }
    }

    override suspend fun delete(id: Long) {
        supabase.from("products").delete {
            filter { eq("id", id) }
        }
    }
}
