package com.example.phoebestore.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.phoebestore.data.local.dao.InventoryLogDao
import com.example.phoebestore.data.local.dao.ProductDao
import com.example.phoebestore.data.local.dao.SaleDao
import com.example.phoebestore.data.local.dao.StoreDao
import com.example.phoebestore.data.local.entity.InventoryLogEntity
import com.example.phoebestore.data.local.entity.ProductEntity
import com.example.phoebestore.data.local.entity.SaleEntity
import com.example.phoebestore.data.local.entity.StoreEntity

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE stores ADD COLUMN deviceId TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE products ADD COLUMN deviceId TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE sales ADD COLUMN deviceId TEXT NOT NULL DEFAULT ''")
    }
}

@Database(
    entities = [StoreEntity::class, ProductEntity::class, SaleEntity::class, InventoryLogEntity::class],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDao
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun inventoryLogDao(): InventoryLogDao
}
