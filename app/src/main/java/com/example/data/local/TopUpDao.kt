package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TopUpDao {
    @Query("SELECT * FROM wallet WHERE id = 1 LIMIT 1")
    fun getWalletFlow(): Flow<WalletEntity?>

    @Query("SELECT * FROM wallet WHERE id = 1 LIMIT 1")
    suspend fun getWallet(): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateWallet(wallet: WalletEntity)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Query("SELECT * FROM saved_favorites WHERE category = :category ORDER BY id DESC")
    fun getFavoritesByCategory(category: String): Flow<List<SavedFavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: SavedFavoriteEntity)

    @Query("DELETE FROM saved_favorites WHERE id = :id")
    suspend fun deleteFavorite(id: Long)
}
