package com.kkh.wallet.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kkh.wallet.data.local.converter.Converters
import com.kkh.wallet.data.local.dao.BudgetDao
import com.kkh.wallet.data.local.dao.CategoryDao
import com.kkh.wallet.data.local.dao.TransactionDao
import com.kkh.wallet.data.local.dao.WalletDao
import com.kkh.wallet.data.local.entity.BudgetEntity
import com.kkh.wallet.data.local.entity.CategoryEntity
import com.kkh.wallet.data.local.entity.TransactionEntity
import com.kkh.wallet.data.local.entity.WalletEntity

@Database(
    entities = [
        WalletEntity::class,
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class KKHDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        const val DB_NAME = "kkh_wallet.db"
    }
}
