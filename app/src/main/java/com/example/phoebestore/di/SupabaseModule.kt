package com.example.phoebestore.di

import com.example.phoebestore.BuildConfig
import com.example.phoebestore.data.remote.source.ProductRemoteDataSource
import com.example.phoebestore.data.remote.source.SaleRemoteDataSource
import com.example.phoebestore.data.remote.source.StoreRemoteDataSource
import com.example.phoebestore.data.remote.source.impl.ProductRemoteDataSourceImpl
import com.example.phoebestore.data.remote.source.impl.SaleRemoteDataSourceImpl
import com.example.phoebestore.data.remote.source.impl.StoreRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
        install(Storage)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteDataSourceModule {

    @Binds
    abstract fun bindStoreRemoteDataSource(impl: StoreRemoteDataSourceImpl): StoreRemoteDataSource

    @Binds
    abstract fun bindProductRemoteDataSource(impl: ProductRemoteDataSourceImpl): ProductRemoteDataSource

    @Binds
    abstract fun bindSaleRemoteDataSource(impl: SaleRemoteDataSourceImpl): SaleRemoteDataSource
}
