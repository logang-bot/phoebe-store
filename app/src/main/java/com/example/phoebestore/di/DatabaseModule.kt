package com.example.phoebestore.di

import android.content.Context
import androidx.room.Room
import com.example.phoebestore.data.local.AppDatabase
import com.example.phoebestore.data.local.dao.ProductDao
import com.example.phoebestore.data.local.dao.StoreDao
import com.example.phoebestore.data.repository.impl.ProductRepositoryImpl
import com.example.phoebestore.data.repository.impl.StoreRepositoryImpl
import com.example.phoebestore.domain.repository.ProductRepository
import com.example.phoebestore.domain.repository.StoreRepository
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
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideStoreDao(db: AppDatabase): StoreDao = db.storeDao()

    @Provides
    fun provideProductDao(db: AppDatabase): ProductDao = db.productDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindStoreRepository(impl: StoreRepositoryImpl): StoreRepository

    @Binds
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository
}
