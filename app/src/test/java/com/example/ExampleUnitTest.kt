package com.example

import com.example.data.repository.TopUpRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testOperatorDetection() {
        val repo = TopUpRepository(FakeTopUpDao())
        assertEquals("Telkomsel", repo.detectOperator("081234567890"))
        assertEquals("Telkomsel", repo.detectOperator("+6282112345678"))
        assertEquals("Indosat", repo.detectOperator("085712345678"))
        assertEquals("XL", repo.detectOperator("087812345678"))
        assertEquals("Tri (3)", repo.detectOperator("089612345678"))
        assertEquals("Smartfren", repo.detectOperator("088212345678"))
    }

    @Test
    fun testFormatRupiah() {
        val formatted = TopUpRepository.formatRupiah(50000L)
        assertTrue(formatted.contains("50.000"))
    }

    @Test
    fun testPlnTokenGeneration() {
        val repo = TopUpRepository(FakeTopUpDao())
        val token = repo.generatePlnToken()
        assertNotNull(token)
        // Format: 4-4-4-4-4 (total 20 digits + 4 dashes = 24 chars)
        assertEquals(24, token.length)
        assertEquals(4, token.count { it == '-' })
    }

    @Test
    fun testMockPlnCustomerValidation() {
        val repo = TopUpRepository(FakeTopUpDao())
        val info = repo.mockValidatePlnCustomer("14238765432")
        assertNotNull(info.nama)
        assertTrue(info.tarifDaya.contains("VA"))
    }
}

// Dummy Dao for unit test without database
private class FakeTopUpDao : com.example.data.local.TopUpDao {
    override fun getWalletFlow() = kotlinx.coroutines.flow.flowOf<com.example.data.local.WalletEntity?>(null)
    override suspend fun getWallet() = null
    override suspend fun updateWallet(wallet: com.example.data.local.WalletEntity) {}
    override fun getAllTransactions() = kotlinx.coroutines.flow.flowOf<List<com.example.data.local.TransactionEntity>>(emptyList())
    override suspend fun insertTransaction(transaction: com.example.data.local.TransactionEntity) {}
    override suspend fun getTransactionById(id: String) = null
    override fun getFavoritesByCategory(category: String) = kotlinx.coroutines.flow.flowOf<List<com.example.data.local.SavedFavoriteEntity>>(emptyList())
    override suspend fun insertFavorite(favorite: com.example.data.local.SavedFavoriteEntity) {}
    override suspend fun deleteFavorite(id: Long) {}
}
