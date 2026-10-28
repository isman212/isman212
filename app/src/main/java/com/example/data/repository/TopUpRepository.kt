package com.example.data.repository

import com.example.R
import com.example.data.local.SavedFavoriteEntity
import com.example.data.local.TopUpDao
import com.example.data.local.TransactionEntity
import com.example.data.local.WalletEntity
import com.example.data.model.CustomerPlnInfo
import com.example.data.model.Game
import com.example.data.model.GameDenomination
import com.example.data.model.PaketDataDenomination
import com.example.data.model.PaymentMethod
import com.example.data.model.PlnDenomination
import com.example.data.model.PromoBanner
import com.example.data.model.PulsaDenomination
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

class TopUpRepository(private val dao: TopUpDao) {

    val walletFlow: Flow<WalletEntity?> = dao.getWalletFlow()
    val transactionsFlow: Flow<List<TransactionEntity>> = dao.getAllTransactions()

    suspend fun ensureInitialWallet() {
        if (dao.getWallet() == null) {
            dao.updateWallet(
                WalletEntity(
                    id = 1,
                    balance = 250000L,
                    kilatCoins = 1250L,
                    userName = "Sobat Kilat"
                )
            )
            // Seed a sample initial completed transaction
            dao.insertTransaction(
                TransactionEntity(
                    id = "TRX-INIT-${System.currentTimeMillis() % 100000}",
                    type = TransactionType.GAME.name,
                    title = "Mobile Legends: Bang Bang",
                    subtitle = "Weekly Diamond Pass",
                    targetNumberOrId = "12839211 (2020)",
                    amountPrice = 27500,
                    adminFee = 0,
                    totalPayment = 27500,
                    paymentMethodName = PaymentMethod.SALDO.title,
                    status = TransactionStatus.BERHASIL.name,
                    serialNumberOrToken = "SN-MLBB-92817342918",
                    timestamp = System.currentTimeMillis() - 86400000L,
                    notes = "Akun: ProPlayer_ID"
                )
            )
        }
    }

    suspend fun topUpWalletBalance(amount: Long, paymentMethod: PaymentMethod): Result<TransactionEntity> {
        val currentWallet = dao.getWallet() ?: WalletEntity()
        val newBalance = currentWallet.balance + amount
        val bonusCoins = (amount / 1000) * 5
        dao.updateWallet(currentWallet.copy(
            balance = newBalance,
            kilatCoins = currentWallet.kilatCoins + bonusCoins
        ))

        val trx = TransactionEntity(
            id = "TOPUP-${UUID.randomUUID().toString().take(8).uppercase()}",
            type = TransactionType.TOPUP_SALDO.name,
            title = "Isi Saldo Kilat",
            subtitle = "Nominal: ${formatRupiah(amount)}",
            targetNumberOrId = "Wallet Kilat",
            amountPrice = amount,
            adminFee = paymentMethod.adminFee,
            totalPayment = amount + paymentMethod.adminFee,
            paymentMethodName = paymentMethod.title,
            status = TransactionStatus.BERHASIL.name,
            serialNumberOrToken = "TOPUP-${Random.nextInt(10000000, 99999999)}",
            timestamp = System.currentTimeMillis(),
            notes = "Bonus +$bonusCoins Koin Kilat"
        )
        dao.insertTransaction(trx)
        return Result.success(trx)
    }

    suspend fun processPurchase(
        type: TransactionType,
        title: String,
        subtitle: String,
        targetNumberOrId: String,
        amount: Long,
        paymentMethod: PaymentMethod,
        generatedTokenOrSn: String,
        notes: String = ""
    ): Result<TransactionEntity> {
        val currentWallet = dao.getWallet() ?: WalletEntity()
        val totalPayment = amount + paymentMethod.adminFee

        if (paymentMethod == PaymentMethod.SALDO) {
            if (currentWallet.balance < totalPayment) {
                return Result.failure(Exception("Saldo tidak mencukupi. Silakan top up saldo terlebih dahulu."))
            }
            dao.updateWallet(currentWallet.copy(
                balance = currentWallet.balance - totalPayment,
                kilatCoins = currentWallet.kilatCoins + (totalPayment / 2000)
            ))
        }

        val trx = TransactionEntity(
            id = "TRX-${Random.nextInt(100, 999)}-${UUID.randomUUID().toString().take(6).uppercase()}",
            type = type.name,
            title = title,
            subtitle = subtitle,
            targetNumberOrId = targetNumberOrId,
            amountPrice = amount,
            adminFee = paymentMethod.adminFee,
            totalPayment = totalPayment,
            paymentMethodName = paymentMethod.title,
            status = TransactionStatus.BERHASIL.name,
            serialNumberOrToken = generatedTokenOrSn,
            timestamp = System.currentTimeMillis(),
            notes = notes
        )
        dao.insertTransaction(trx)
        return Result.success(trx)
    }

    fun getPromoBanners(): List<PromoBanner> {
        return listOf(
            PromoBanner(
                id = "promo_1",
                title = "Mega Flash Sale Diamond Game",
                subtitle = "Diskon s/d 30% Mobile Legends & Free Fire",
                tag = "FLASH SALE",
                drawableRes = R.drawable.banner_game_promo
            ),
            PromoBanner(
                id = "promo_2",
                title = "Cashback Token PLN & Pulsa",
                subtitle = "Ekstra Koin Kilat dan Diskon Admin Fee",
                tag = "HEMAT 24 JAM",
                drawableRes = R.drawable.banner_pulsa_pln
            )
        )
    }

    fun getGames(): List<Game> {
        return listOf(
            Game(
                id = "mlbb",
                name = "Mobile Legends",
                publisher = "Moonton",
                tag = "MOBA",
                userIdLabel = "User ID",
                zoneIdLabel = "Zone ID",
                zoneIdHelper = "Contoh: 12345678 (2020)",
                isPopular = true,
                denominations = listOf(
                    GameDenomination("ml_86", "86 Diamonds", "86 \uD83D\uDC8E", 19500, 22000, "+8 Bonus"),
                    GameDenomination("ml_172", "172 Diamonds", "172 \uD83D\uDC8E", 38000, 42000, "+16 Bonus"),
                    GameDenomination("ml_257", "257 Diamonds", "257 \uD83D\uDC8E", 57000, 65000, "+27 Bonus", isBestSeller = true),
                    GameDenomination("ml_wdp", "Weekly Diamond Pass", "WDP (7 Hari)", 27500, 32000, "210 Total \uD83D\uDC8E", isBestSeller = true),
                    GameDenomination("ml_344", "344 Diamonds", "344 \uD83D\uDC8E", 76000, 85000, "+36 Bonus"),
                    GameDenomination("ml_514", "514 Diamonds", "514 \uD83D\uDC8E", 114000, 128000, "+54 Bonus"),
                    GameDenomination("ml_706", "706 Diamonds", "706 \uD83D\uDC8E", 155000, 175000, "+94 Bonus"),
                    GameDenomination("ml_twilight", "Twilight Pass", "Pass Spesial", 140000, 155000, "Skin Eksklusif")
                )
            ),
            Game(
                id = "ff",
                name = "Free Fire",
                publisher = "Garena",
                tag = "Battle Royale",
                userIdLabel = "Player ID",
                zoneIdLabel = null,
                isPopular = true,
                denominations = listOf(
                    GameDenomination("ff_70", "70 Diamonds", "70 \uD83D\uDC8E", 9500, 11000),
                    GameDenomination("ff_140", "140 Diamonds", "140 \uD83D\uDC8E", 18500, 22000),
                    GameDenomination("ff_355", "355 Diamonds", "355 \uD83D\uDC8E", 46000, 52000, "+35 Bonus", isBestSeller = true),
                    GameDenomination("ff_720", "720 Diamonds", "720 \uD83D\uDC8E", 92000, 105000, "+72 Bonus"),
                    GameDenomination("ff_1450", "1450 Diamonds", "1450 \uD83D\uDC8E", 185000, 210000, "+150 Bonus"),
                    GameDenomination("ff_member_week", "Membership Mingguan", "Mingguan", 29000, 35000, "450 \uD83D\uDC8E Klaim"),
                    GameDenomination("ff_member_month", "Membership Bulanan", "Bulanan", 115000, 130000, "2600 \uD83D\uDC8E Klaim")
                )
            ),
            Game(
                id = "pubgm",
                name = "PUBG Mobile",
                publisher = "Level Infinite",
                tag = "Battle Royale",
                userIdLabel = "Player ID (UID)",
                zoneIdLabel = null,
                isPopular = true,
                denominations = listOf(
                    GameDenomination("pubg_60", "60 UC", "60 UC", 14500, 16000),
                    GameDenomination("pubg_325", "325 UC", "325 UC", 72000, 80000, "+25 UC Bonus", isBestSeller = true),
                    GameDenomination("pubg_660", "660 UC", "660 UC (RP)", 145000, 165000, "Cukup Royale Pass"),
                    GameDenomination("pubg_1800", "1800 UC", "1800 UC", 360000, 400000, "+300 Bonus")
                )
            ),
            Game(
                id = "genshin",
                name = "Genshin Impact",
                publisher = "HoYoverse",
                tag = "RPG",
                userIdLabel = "UID",
                zoneIdLabel = "Server",
                zoneIdHelper = "Asia / America / Europe / TW,HK,MO",
                isPopular = true,
                denominations = listOf(
                    GameDenomination("gi_welkin", "Blessing of Welkin Moon", "Welkin Moon", 65000, 79000, "3000 Primogems Total", isBestSeller = true),
                    GameDenomination("gi_60", "60 Genesis Crystals", "60 Crystals", 15000, 18000),
                    GameDenomination("gi_300", "300+30 Genesis Crystals", "330 Crystals", 75000, 89000),
                    GameDenomination("gi_980", "980+110 Genesis Crystals", "1090 Crystals", 230000, 260000),
                    GameDenomination("gi_1980", "1980+260 Genesis Crystals", "2240 Crystals", 450000, 510000)
                )
            ),
            Game(
                id = "hok",
                name = "Honor of Kings",
                publisher = "Level Infinite",
                tag = "MOBA",
                userIdLabel = "User ID (UID)",
                zoneIdLabel = null,
                isPopular = false,
                denominations = listOf(
                    GameDenomination("hok_80", "80 Tokens", "80 Tokens", 14000, 16000),
                    GameDenomination("hok_240", "240+17 Tokens", "257 Tokens", 42000, 48000, "+17 Bonus", isBestSeller = true),
                    GameDenomination("hok_400", "400+32 Tokens", "432 Tokens", 69000, 78000),
                    GameDenomination("hok_week", "Weekly Card Plus", "Weekly Card", 15000, 18000)
                )
            ),
            Game(
                id = "roblox",
                name = "Roblox",
                publisher = "Roblox Corporation",
                tag = "Sandbox",
                userIdLabel = "Username Roblox",
                zoneIdLabel = null,
                isPopular = false,
                denominations = listOf(
                    GameDenomination("rbx_80", "80 Robux", "80 R$", 16000, 18000),
                    GameDenomination("rbx_400", "400 Robux", "400 R$", 75000, 85000, isBestSeller = true),
                    GameDenomination("rbx_800", "800 Robux", "800 R$", 148000, 165000)
                )
            )
        )
    }

    fun getPlnDenominations(): List<PlnDenomination> {
        return listOf(
            PlnDenomination("pln_20", 20000, 20500, 2500, "Est. 13.5 kWh"),
            PlnDenomination("pln_50", 50000, 50500, 2500, "Est. 33.8 kWh", isPopular = true),
            PlnDenomination("pln_100", 100000, 100500, 2500, "Est. 67.5 kWh", isPopular = true),
            PlnDenomination("pln_200", 200000, 200500, 2500, "Est. 135.2 kWh"),
            PlnDenomination("pln_500", 500000, 500500, 2500, "Est. 338.0 kWh"),
            PlnDenomination("pln_1000", 1000000, 1000500, 2500, "Est. 676.0 kWh")
        )
    }

    fun detectOperator(phone: String): String {
        val clean = phone.replace("-", "").replace(" ", "").replace("+62", "0")
        if (clean.length < 4) return "Umum"
        val prefix = clean.take(4)
        return when (prefix) {
            "0811", "0812", "0813", "0821", "0822", "0823", "0851", "0852", "0853" -> "Telkomsel"
            "0814", "0815", "0816", "0855", "0856", "0857", "0858" -> "Indosat"
            "0817", "0818", "0819", "0859", "0877", "0878" -> "XL"
            "0831", "0832", "0833", "0838" -> "Axis"
            "0895", "0896", "0897", "0898", "0899" -> "Tri (3)"
            "0881", "0882", "0883", "0884", "0885", "0886", "0887", "0888", "0889" -> "Smartfren"
            else -> "Telkomsel"
        }
    }

    fun getPulsaDenominations(operator: String): List<PulsaDenomination> {
        val baseMultiplier = when (operator) {
            "Telkomsel" -> 1.05
            "Indosat" -> 1.02
            "XL" -> 1.03
            "Axis" -> 1.01
            "Tri (3)" -> 1.01
            else -> 1.02
        }
        val nominals = listOf(5000L, 10000L, 15000L, 20000L, 25000L, 50000L, 100000L, 150000L, 200000L)
        return nominals.map { nom ->
            val price = (nom * baseMultiplier).toLong()
            PulsaDenomination(
                id = "${operator.lowercase()}_$nom",
                operator = operator,
                nominal = nom,
                price = price,
                isPromo = nom == 25000L || nom == 50000L
            )
        }
    }

    fun getPaketDataDenominations(operator: String): List<PaketDataDenomination> {
        return when (operator) {
            "Telkomsel" -> listOf(
                PaketDataDenomination("tsel_d1", operator, "InternetMAX 15GB", "15 GB", "30 Hari", "11GB Utama + 4GB Nonton VideoMax", 42000),
                PaketDataDenomination("tsel_d2", operator, "InternetMAX 25GB", "25 GB", "30 Hari", "20GB Utama + 5GB Nonton Disney+/YouTube", 58000, isBestSeller = true),
                PaketDataDenomination("tsel_d3", operator, "Combo Sakti 45GB", "45 GB", "30 Hari", "Full 24 Jam Semua Jaringan + 100 Mnt Telpon", 89000, isBestSeller = true),
                PaketDataDenomination("tsel_d4", operator, "Super Seru 75GB", "75 GB", "30 Hari", "Full Kuota Utama 24 Jam Anti FUP", 125000),
                PaketDataDenomination("tsel_d5", operator, "Harian 5GB", "5 GB", "3 Hari", "Kuota Kilat Harian Siap Pakai", 15000)
            )
            "Indosat" -> listOf(
                PaketDataDenomination("isat_d1", operator, "Freedom Internet 12GB", "12 GB", "30 Hari", "Kuota Utama 24 Jam dengan Pulsa Safe", 35000),
                PaketDataDenomination("isat_d2", operator, "Freedom Internet 30GB", "30 GB", "30 Hari", "Full Kuota Utama Tanpa Pembagian Waktu", 65000, isBestSeller = true),
                PaketDataDenomination("isat_d3", operator, "Freedom Internet 50GB", "50 GB", "30 Hari", "Puas Streaming & Gaming Seharian", 89000, isBestSeller = true),
                PaketDataDenomination("isat_d4", operator, "Freedom U 15GB + Apps", "15 GB + Apps", "30 Hari", "Unlimited Instagram, TikTok, YouTube", 72000),
                PaketDataDenomination("isat_d5", operator, "Yellow 3GB", "3 GB", "3 Hari", "Cocok untuk darurat kuota", 12000)
            )
            "XL" -> listOf(
                PaketDataDenomination("xl_d1", operator, "Xtra Combo Flex 18GB", "18 GB", "30 Hari", "Kuota Utama + Bonus Kuota Area s/d 10GB", 45000),
                PaketDataDenomination("xl_d2", operator, "Xtra Combo Flex 35GB", "35 GB", "30 Hari", "Pilihan Favorit Pelajar & Gamers", 68000, isBestSeller = true),
                PaketDataDenomination("xl_d3", operator, "Xtra Combo Flex 60GB", "60 GB", "30 Hari", "Bebas Nelpon Semua Operator & Bonus YouTube", 105000, isBestSeller = true),
                PaketDataDenomination("xl_d4", operator, "Xtra Kuota Harian 6GB", "6 GB", "3 Hari", "Super Kilat Tanpa Buffering", 16000)
            )
            "Tri (3)" -> listOf(
                PaketDataDenomination("tri_d1", operator, "AlwaysOn (AON) 16GB", "16 GB", "Aktif Selamanya", "Masa aktif kuota mengikuti masa aktif kartu", 40000, isBestSeller = true),
                PaketDataDenomination("tri_d2", operator, "Happy 30GB", "30 GB", "30 Hari", "Full kuota 24 jam tanpa batas jam malam", 55000, isBestSeller = true),
                PaketDataDenomination("tri_d3", operator, "Happy 52GB", "52 GB", "30 Hari", "Streaming & TikTok sepuasnya 24 jam", 79000),
                PaketDataDenomination("tri_d4", operator, "AON 6GB", "6 GB", "Aktif Selamanya", "Simpan kuota cadangan", 22000)
            )
            else -> listOf(
                PaketDataDenomination("gen_d1", operator, "Paket Internet 15GB", "15 GB", "30 Hari", "Full 24 Jam Semua Jaringan", 38000),
                PaketDataDenomination("gen_d2", operator, "Paket Internet 30GB", "30 GB", "30 Hari", "Super Kilat Kuota Utama", 60000, isBestSeller = true),
                PaketDataDenomination("gen_d3", operator, "Paket Internet 50GB", "50 GB", "30 Hari", "Gaming & Streaming Puas", 90000)
            )
        }
    }

    fun mockValidatePlnCustomer(idPelanggan: String): CustomerPlnInfo {
        val lastDigit = idPelanggan.lastOrNull()?.digitToIntOrNull() ?: 1
        val names = listOf(
            "BUDI SANTOSO", "SRI WAHYUNI", "AHMAD FAUZI", "DEWI LESTARI",
            "I KETUT ARYA", "HENDRA WIJAYA", "RATNA SUSANTI"
        )
        val name = names[lastDigit % names.size]
        val tarifDaya = when (lastDigit % 3) {
            0 -> "R1M / 900 VA"
            1 -> "R1 / 1300 VA"
            else -> "R1 / 2200 VA"
        }
        return CustomerPlnInfo(
            idPelanggan = idPelanggan,
            nama = name,
            tarifDaya = tarifDaya,
            isValid = true
        )
    }

    fun generatePlnToken(): String {
        val part1 = Random.nextInt(1000, 9999)
        val part2 = Random.nextInt(1000, 9999)
        val part3 = Random.nextInt(1000, 9999)
        val part4 = Random.nextInt(1000, 9999)
        val part5 = Random.nextInt(1000, 9999)
        return "$part1-$part2-$part3-$part4-$part5"
    }

    fun mockValidateGameAccount(gameId: String, userId: String, zoneId: String?): String {
        if (userId.isBlank()) return "Akun Tidak Ditemukan"
        val nicknames = listOf(
            "Kilat_Pro_Gamer", "ShadowHunter99", "Garuda_ID", "MythicGlory_Ace",
            "StarSlayer", "WibuSultan", "DragonKnight_X"
        )
        val hash = (userId.hashCode() + (zoneId?.hashCode() ?: 0)).let { if (it < 0) -it else it }
        return nicknames[hash % nicknames.size]
    }

    companion object {
        fun formatRupiah(amount: Long): String {
            val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            format.maximumFractionDigits = 0
            return format.format(amount).replace("Rp", "Rp ")
        }
    }
}
