package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentMethod
import com.example.data.repository.TopUpRepository
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandGreen

@Composable
fun PaymentMethodSelector(
    selectedMethod: PaymentMethod,
    onSelectMethod: (PaymentMethod) -> Unit,
    userBalance: Long,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("payment_method_selector_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Metode Pembayaran",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Saldo Kilat (Primary)
            PaymentItemRow(
                method = PaymentMethod.SALDO,
                isSelected = selectedMethod == PaymentMethod.SALDO,
                subLabel = "Tersedia: ${TopUpRepository.formatRupiah(userBalance)}",
                onClick = { onSelectMethod(PaymentMethod.SALDO) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // QRIS
            PaymentItemRow(
                method = PaymentMethod.QRIS,
                isSelected = selectedMethod == PaymentMethod.QRIS,
                subLabel = "BCA, Mandiri, BRI, BNI, GoPay, OVO, DANA, ShopeePay",
                onClick = { onSelectMethod(PaymentMethod.QRIS) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // DANA
            PaymentItemRow(
                method = PaymentMethod.DANA,
                isSelected = selectedMethod == PaymentMethod.DANA,
                subLabel = "Biaya Admin: Rp 1.000",
                onClick = { onSelectMethod(PaymentMethod.DANA) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // GoPay
            PaymentItemRow(
                method = PaymentMethod.GOPAY,
                isSelected = selectedMethod == PaymentMethod.GOPAY,
                subLabel = "Biaya Admin: Rp 1.000",
                onClick = { onSelectMethod(PaymentMethod.GOPAY) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // BCA VA
            PaymentItemRow(
                method = PaymentMethod.VA_BCA,
                isSelected = selectedMethod == PaymentMethod.VA_BCA,
                subLabel = "Virtual Account Bank BCA",
                onClick = { onSelectMethod(PaymentMethod.VA_BCA) }
            )
        }
    }
}

@Composable
private fun PaymentItemRow(
    method: PaymentMethod,
    isSelected: Boolean,
    subLabel: String,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag("payment_method_${method.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when (method) {
                            PaymentMethod.SALDO -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            PaymentMethod.QRIS -> BrandGreen.copy(alpha = 0.2f)
                            PaymentMethod.VA_BCA, PaymentMethod.VA_BRI, PaymentMethod.VA_MANDIRI -> Color(0xFF005BAC).copy(alpha = 0.2f)
                            else -> BrandAccent.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (method) {
                        PaymentMethod.SALDO -> Icons.Default.AccountBalanceWallet
                        PaymentMethod.QRIS -> Icons.Default.QrCode2
                        PaymentMethod.VA_BCA, PaymentMethod.VA_BRI, PaymentMethod.VA_MANDIRI -> Icons.Default.AccountBalance
                        else -> Icons.Default.AccountBalanceWallet
                    },
                    contentDescription = method.title,
                    tint = when (method) {
                        PaymentMethod.SALDO -> MaterialTheme.colorScheme.primary
                        PaymentMethod.QRIS -> BrandGreen
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = method.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (method.badge != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (method == PaymentMethod.SALDO) BrandGreen.copy(alpha = 0.15f)
                                    else BrandAccent.copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = method.badge,
                                color = if (method == PaymentMethod.SALDO) BrandGreen else BrandAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    text = subLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }

        // Selection Checkmark radio
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                )
                .border(
                    2.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
