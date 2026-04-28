package com.example.phoebestore.di

import android.content.Context
import androidx.room.Room
import com.example.phoebestore.data.local.AppDatabase
import com.example.phoebestore.data.local.MIGRATION_8_9
import com.example.phoebestore.data.local.dao.InventoryLogDao
import com.example.phoebestore.data.local.dao.ProductDao
import com.example.phoebestore.data.local.dao.SaleDao
import com.example.phoebestore.data.local.dao.StoreDao
import com.example.phoebestore.data.repository.impl.InventoryLogRepositoryImpl
import com.example.phoebestore.data.repository.impl.ProductRepositoryImpl
import com.example.phoebestore.data.repository.impl.SaleRepositoryImpl
import com.example.phoebestore.data.repository.impl.StoreRepositoryImpl
import com.example.phoebestore.data.repository.impl.UserSettingsRepositoryImpl
import com.example.phoebestore.domain.repository.InventoryLogRepository
import com.example.phoebestore.domain.repository.ProductRepository
import com.example.phoebestore.domain.repository.SaleRepository
import com.example.phoebestore.domain.repository.StoreRepository
import com.example.phoebestore.domain.repository.UserSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "phoebe_store.db")
            .addMigrations(MIGRATION_8_9)
            .build()

    @Provides
    fun provideStoreDao(db: AppDatabase): StoreDao = db.storeDao()

    @Provides
    fun provideProductDao(db: AppDatabase): ProductDao = db.productDao()

    @Provides
    fun provideSaleDao(db: AppDatabase): SaleDao = db.saleDao()

    @Provides
    fun provideInventoryLogDao(db: AppDatabase): InventoryLogDao = db.inventoryLogDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindStoreRepository(impl: StoreRepositoryImpl): StoreRepository

    @Binds
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds
    abstract fun bindSaleRepository(impl: SaleRepositoryImpl): SaleRepository

    @Binds
    abstract fun bindInventoryLogRepository(impl: InventoryLogRepositoryImpl): InventoryLogRepository

    @Binds
    abstract fun bindUserSettingsRepository(impl: UserSettingsRepositoryImpl): UserSettingsRepository
}
