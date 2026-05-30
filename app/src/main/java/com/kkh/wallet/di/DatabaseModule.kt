package com.kkh.wallet.di

import android.content.Context
import androidx.room.Room
import com.kkh.wallet.data.local.KKHDatabase
import com.kkh.wallet.data.local.dao.BudgetDao
import com.kkh.wallet.data.local.dao.CategoryDao
import com.kkh.wallet.data.local.dao.TransactionDao
import com.kkh.wallet.data.local.dao.WalletDao
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
    fun provideDatabase(@ApplicationContext context: Context): KKHDatabase {
        return Room.databaseBuilder(
            context,
            KKHDatabase::class.java,
            KKHDatabase.DB_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideWalletDao(db: KKHDatabase): WalletDao = db.walletDao()

    @Provides
    fun provideTransactionDao(db: KKHDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideCategoryDao(db: KKHDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideBudgetDao(db: KKHDatabase): BudgetDao = db.budgetDao()
}
