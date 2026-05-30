package com.kkh.wallet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kkh.wallet.data.local.entity.WalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {

    @Query("SELECT * FROM wallets WHERE isArchived = 0 ORDER BY createdAt ASC")
    fun observeActive(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<WalletEntity?>

    @Query("SELECT * FROM wallets WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): WalletEntity?

    @Query("SELECT COUNT(*) FROM wallets")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(wallet: WalletEntity): Long

    @Update
    suspend fun update(wallet: WalletEntity)

    @Query("UPDATE wallets SET currentBalance = currentBalance + :delta WHERE id = :id")
    suspend fun adjustBalance(id: Long, delta: Double)

    @Query("UPDATE wallets SET usedLimit = usedLimit + :delta WHERE id = :id")
    suspend fun adjustUsedLimit(id: Long, delta: Double)

    @Query("DELETE FROM wallets WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM wallets")
    suspend fun listAllOnce(): List<WalletEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(wallets: List<WalletEntity>)

    @Query("DELETE FROM wallets")
    suspend fun deleteAll()
}
