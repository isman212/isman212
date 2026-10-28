package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSecondary

@Composable
fun CategoryGrid(
    onSelectGame: () -> Unit,
    onSelectPln: () -> Unit,
    onSelectPulsa: () -> Unit,
    onSelectData: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Layanan Utama",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CategoryItem(
                    title = "Top Up Game",
                    badge = "Favorit",
                    badgeColor = BrandAccent,
                    icon = Icons.Default.SportsEsports,
                    brush = Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))),
                    testTag = "category_game",
                    onClick = onSelectGame
                )

                CategoryItem(
                    title = "Token PLN",
                    badge = "Instan",
                    badgeColor = BrandGreen,
                    icon = Icons.Default.FlashOn,
                    brush = Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF0369A1))),
                    testTag = "category_pln",
                    onClick = onSelectPln
                )

                CategoryItem(
                    title = "Pulsa",
                    badge = null,
                    badgeColor = BrandAccent,
                    icon = Icons.Default.PhoneAndroid,
                    brush = Brush.linearGradient(listOf(Color(0xFFF97316), Color(0xFFEA580C))),
                    testTag = "category_pulsa",
                    onClick = onSelectPulsa
                )

                CategoryItem(
                    title = "Paket Data",
                    badge = "Hemat",
                    badgeColor = BrandSecondary,
                    icon = Icons.Default.Wifi,
                    brush = Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))),
                    testTag = "category_data",
                    onClick = onSelectData
                )
            }
        }
    }
}

@Composable
private fun CategoryItem(
    title: String,
    badge: String?,
    badgeColor: Color,
    icon: ImageVector,
    brush: Brush,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(brush),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
