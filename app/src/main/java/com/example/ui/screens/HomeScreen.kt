package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TransactionEntity
import com.example.data.model.Game
import com.example.data.model.PromoBanner
import com.example.data.model.TransactionType
import com.example.data.repository.TopUpRepository
import com.example.ui.components.CategoryGrid
import com.example.ui.components.PromoBannerCarousel
import com.example.ui.components.SaldoCard
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandPrimary

@Composable
fun HomeScreen(
    balance: Long,
    kilatCoins: Long,
    promoBanners: List<PromoBanner>,
    games: List<Game>,
    recentTransactions: List<TransactionEntity>,
    onTopUpSaldoClick: () -> Unit,
    onNavigateToGame: (Game?) -> Unit,
    onNavigateToPln: () -> Unit,
    onNavigateToPulsa: () -> Unit,
    onNavigateToData: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onOpenReceipt: (TransactionEntity) -> Unit,
    onPromoClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_lazy_column"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Saldo Wallet Card
        item {
            Box(modifier = Modifier.padding(16.dp)) {
                SaldoCard(
                    balance = balance,
                    kilatCoins = kilatCoins,
                    onTopUpClick = onTopUpSaldoClick,
                    onHistoryClick = onNavigateToHistory,
                    onPromoClick = onPromoClick
                )
            }
        }

        // Promo Banner Carousel
        item {
            PromoBannerCarousel(
                banners = promoBanners,
                onBannerClick = { banner ->
                    if (banner.id == "promo_1") onNavigateToGame(games.first()) else onNavigateToPln()
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Quick Categories
        item {
            CategoryGrid(
                onSelectGame = { onNavigateToGame(null) },
                onSelectPln = onNavigateToPln,
                onSelectPulsa = onNavigateToPulsa,
                onSelectData = onNavigateToData
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Popular Games Section
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Game Terpopuler \uD83C\uDFAE",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Semua Game",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onNavigateToGame(null) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(games) { game ->
                        GameQuickItem(game = game, onClick = { onNavigateToGame(game) })
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Recent Transactions Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Aktivitas Terkini",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Lihat Riwayat",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onNavigateToHistory() }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (recentTransactions.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = "Belum ada transaksi. Coba top up game atau token listrik sekarang!",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    recentTransactions.take(3).forEach { trx ->
                        RecentTransactionRow(transaction = trx, onClick = { onOpenReceipt(trx) })
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun GameQuickItem(
    game: Game,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick)
            .testTag("popular_game_${game.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            when (game.id) {
                                "mlbb" -> listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
                                "ff" -> listOf(Color(0xFFEA580C), Color(0xFFC2410C))
                                "pubgm" -> listOf(Color(0xFFCA8A04), Color(0xFFA16207))
                                else -> listOf(Color(0xFF7C3AED), Color(0xFF6D28D9))
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = game.name,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = game.name,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = game.publisher,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RecentTransactionRow(
    transaction: TransactionEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("recent_trx_${transaction.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            when (transaction.type) {
                                TransactionType.GAME.name -> BrandPrimary.copy(alpha = 0.15f)
                                TransactionType.PLN.name -> BrandGreen.copy(alpha = 0.15f)
                                else -> BrandAccent.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (transaction.type) {
                            TransactionType.GAME.name -> Icons.Default.SportsEsports
                            TransactionType.PLN.name -> Icons.Default.FlashOn
                            TransactionType.PULSA.name, TransactionType.DATA.name -> Icons.Default.PhoneAndroid
                            else -> Icons.Default.ReceiptLong
                        },
                        contentDescription = transaction.title,
                        tint = when (transaction.type) {
                            TransactionType.GAME.name -> BrandPrimary
                            TransactionType.PLN.name -> BrandGreen
                            else -> BrandAccent
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = transaction.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = TopUpRepository.formatRupiah(transaction.totalPayment),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Lihat Struk >",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
