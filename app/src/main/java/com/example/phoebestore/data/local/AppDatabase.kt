package com.example.phoebestore.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.phoebestore.data.local.dao.InventoryLogDao
import com.example.phoebestore.data.local.dao.ProductDao
import com.example.phoebestore.data.local.dao.SaleDao
import com.example.phoebestore.data.local.dao.StoreDao
import com.example.phoebestore.data.local.dao.SyncOperationDao
import com.example.phoebestore.data.local.entity.InventoryLogEntity
import com.example.phoebestore.data.local.entity.ProductEntity
import com.example.phoebestore.data.local.entity.SaleEntity
import com.example.phoebestore.data.local.entity.StoreEntity
import com.example.phoebestore.data.local.entity.SyncOperationEntity

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE stores ADD COLUMN deviceId TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE products ADD COLUMN deviceId TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE sales ADD COLUMN deviceId TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS pending_sync_ops (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                entityType TEXT NOT NULL,
                entityId INTEGER NOT NULL,
                operation TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `pending_sync_ops`")
        db.execSQL("DROP TABLE IF EXISTS `inventory_logs`")
        db.execSQL("DROP TABLE IF EXISTS `sales`")
        db.execSQL("DROP TABLE IF EXISTS `products`")
        db.execSQL("DROP TABLE IF EXISTS `stores`")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `stores` (
                `id` TEXT NOT NULL PRIMARY KEY,
                `name` TEXT NOT NULL,
                `description` TEXT NOT NULL DEFAULT '',
                `currency` TEXT NOT NULL DEFAULT 'USD',
                `logoUrl` TEXT NOT NULL DEFAULT '',
                `photoUrl` TEXT NOT NULL DEFAULT '',
                `createdAt` INTEGER NOT NULL,
                `lastAccessedAt` INTEGER NOT NULL DEFAULT 0,
                `deviceId` TEXT NOT NULL DEFAULT ''
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `products` (
                `id` TEXT NOT NULL PRIMARY KEY,
                `storeId` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT NOT NULL DEFAULT '',
                `price` REAL NOT NULL,
                `costPrice` REAL NOT NULL DEFAULT 0.0,
                `stock` INTEGER NOT NULL DEFAULT 0,
                `imageUrl` TEXT NOT NULL DEFAULT '',
                `createdAt` INTEGER NOT NULL,
                `deviceId` TEXT NOT NULL DEFAULT '',
                FOREIGN KEY(`storeId`) REFERENCES `stores`(`id`) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_storeId` ON `products` (`storeId`)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `sales` (
                `id` TEXT NOT NULL PRIMARY KEY,
                `storeId` TEXT NOT NULL,
                `productId` TEXT,
                `productName` TEXT NOT NULL,
                `quantity` INTEGER NOT NULL,
                `unitPrice` REAL NOT NULL,
                `unitCost` REAL NOT NULL DEFAULT 0.0,
                `totalAmount` REAL NOT NULL,
                `saleType` TEXT NOT NULL DEFAULT 'STANDARD',
                `profitOutcome` TEXT NOT NULL DEFAULT 'NORMAL_PROFIT',
                `notes` TEXT NOT NULL DEFAULT '',
                `onCredit` INTEGER NOT NULL DEFAULT 0,
                `creditPersonName` TEXT NOT NULL DEFAULT '',
                `soldAt` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `deviceId` TEXT NOT NULL DEFAULT '',
                FOREIGN KEY(`storeId`) REFERENCES `stores`(`id`) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sales_storeId` ON `sales` (`storeId`)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `inventory_logs` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `storeId` TEXT NOT NULL,
                `productId` TEXT NOT NULL,
                `productName` TEXT NOT NULL,
                `previousStock` INTEGER NOT NULL,
                `newStock` INTEGER NOT NULL,
                `loggedAt` INTEGER NOT NULL,
                FOREIGN KEY(`storeId`) REFERENCES `stores`(`id`) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_inventory_logs_storeId` ON `inventory_logs` (`storeId`)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `pending_sync_ops` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `entityType` TEXT NOT NULL,
                `entityId` TEXT NOT NULL,
                `operation` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL
            )
        """.trimIndent())
    }
}

@Database(
    entities = [StoreEntity::class, ProductEntity::class, SaleEntity::class, InventoryLogEntity::class, SyncOperationEntity::class],
    version = 11,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDao
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun inventoryLogDao(): InventoryLogDao
    abstract fun syncOperationDao(): SyncOperationDao
}
