package com.kkh.wallet.di

import com.kkh.wallet.data.repository.BudgetRepositoryImpl
import com.kkh.wallet.data.repository.CategoryRepositoryImpl
import com.kkh.wallet.data.repository.TransactionRepositoryImpl
import com.kkh.wallet.data.repository.WalletRepositoryImpl
import com.kkh.wallet.domain.repository.BudgetRepository
import com.kkh.wallet.domain.repository.CategoryRepository
import com.kkh.wallet.domain.repository.TransactionRepository
import com.kkh.wallet.domain.repository.WalletRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWalletRepository(impl: WalletRepositoryImpl): WalletRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository
}
