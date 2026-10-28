package com.example.data.model

enum class TransactionType(val title: String) {
    GAME("Top Up Game"),
    PLN("Token Listrik PLN"),
    PULSA("Pulsa Seluler"),
    DATA("Paket Data Internet"),
    TOPUP_SALDO("Isi Saldo Kilat")
}

enum class TransactionStatus(val displayName: String) {
    BERHASIL("Berhasil"),
    PENDING("Menunggu"),
    GAGAL("Gagal")
}

enum class PaymentMethod(
    val id: String,
    val title: String,
    val category: String,
    val adminFee: Long,
    val badge: String? = null
) {
    SALDO("saldo", "Saldo TopUpKilat", "Saldo Kilat", 0, "Bebas Biaya"),
    QRIS("qris", "QRIS (Semua Bank / e-Wallet)", "Instant Pay", 500, "Instan 24 Jam"),
    GOPAY("gopay", "GoPay", "E-Wallet", 1000, null),
    DANA("dana", "DANA", "E-Wallet", 1000, "Favorit"),
    OVO("ovo", "OVO", "E-Wallet", 1000, null),
    SHOPEEPAY("shopeepay", "ShopeePay", "E-Wallet", 1000, null),
    VA_BCA("va_bca", "BCA Virtual Account", "Virtual Account", 1500, null),
    VA_BRI("va_bri", "BRI Virtual Account (BRIVA)", "Virtual Account", 1500, null),
    VA_MANDIRI("va_mandiri", "Mandiri Virtual Account", "Virtual Account", 1500, null)
}

data class Game(
    val id: String,
    val name: String,
    val publisher: String,
    val tag: String,
    val userIdLabel: String = "User ID",
    val zoneIdLabel: String? = "Zone ID",
    val zoneIdHelper: String? = "Contoh: 12345678 (2020)",
    val isPopular: Boolean = false,
    val denominations: List<GameDenomination>
)

data class GameDenomination(
    val id: String,
    val name: String,
    val amountFormatted: String,
    val price: Long,
    val originalPrice: Long? = null,
    val bonus: String? = null,
    val isBestSeller: Boolean = false
)

data class PlnDenomination(
    val id: String,
    val nominal: Long,
    val price: Long,
    val adminFee: Long = 2500,
    val kwhEstimate: String,
    val isPopular: Boolean = false
)

data class PulsaDenomination(
    val id: String,
    val operator: String,
    val nominal: Long,
    val price: Long,
    val isPromo: Boolean = false
)

data class PaketDataDenomination(
    val id: String,
    val operator: String,
    val title: String,
    val quotaMain: String,
    val validity: String,
    val description: String,
    val price: Long,
    val isBestSeller: Boolean = false
)

data class CustomerPlnInfo(
    val idPelanggan: String,
    val nama: String,
    val tarifDaya: String,
    val isValid: Boolean = true
)

data class PromoBanner(
    val id: String,
    val title: String,
    val subtitle: String,
    val tag: String,
    val drawableRes: Int
)

data class AppUser(
    val uid: String,
    val displayName: String,
    val email: String? = null,
    val phoneNumber: String? = null,
    val photoUrl: String? = null,
    val authProvider: String = "google" // "google" or "phone"
)

