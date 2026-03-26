package com.example.phoebestore.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.phoebestore.data.local.dao.ProductDao
import com.example.phoebestore.data.local.dao.SaleDao
import com.example.phoebestore.data.local.dao.StoreDao
import com.example.phoebestore.data.local.entity.ProductEntity
import com.example.phoebestore.data.local.entity.SaleEntity
import com.example.phoebestore.data.local.entity.StoreEntity

@Database(
    entities = [StoreEntity::class, ProductEntity::class, SaleEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDao
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
}
