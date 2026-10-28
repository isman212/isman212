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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaketDataDenomination
import com.example.data.model.PaymentMethod
import com.example.data.model.PulsaDenomination
import com.example.data.repository.TopUpRepository
import com.example.ui.components.PaymentMethodSelector
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandGreen

@Composable
fun PulsaDataScreen(
    phoneNumber: String,
    detectedOperator: String,
    pulsaTab: Int, // 0 = Pulsa, 1 = Paket Data
    pulsaList: List<PulsaDenomination>,
    dataList: List<PaketDataDenomination>,
    selectedPulsa: PulsaDenomination?,
    selectedData: PaketDataDenomination?,
    selectedPaymentMethod: PaymentMethod,
    userBalance: Long,
    isProcessing: Boolean,
    onPhoneNumberChange: (String) -> Unit,
    onPulsaTabChange: (Int) -> Unit,
    onSelectPulsa: (PulsaDenomination) -> Unit,
    onSelectData: (PaketDataDenomination) -> Unit,
    onSelectPaymentMethod: (PaymentMethod) -> Unit,
    onBuyClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("pulsa_data_screen_lazy_column"),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Input Phone Number Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nomor Ponsel",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Operator Badge
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SignalCellularAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = detectedOperator,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = onPhoneNumberChange,
                            label = { Text("Contoh: 08123456789") },
                            placeholder = { Text("08xxxxxxxxxx") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = "Nomor HP",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                if (phoneNumber.isNotEmpty()) {
                                    IconButton(onClick = { onPhoneNumberChange("") }) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pulsa_phone_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Number Chips
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            QuickPhoneChip("Nomor Saya", "081234567890", onSelect = onPhoneNumberChange)
                            QuickPhoneChip("Kerabat", "085712345678", onSelect = onPhoneNumberChange)
                        }
                    }
                }
            }

            // Tabs: Pulsa Reguler vs Paket Data
            item {
                TabRow(
                    selectedTabIndex = pulsaTab,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = pulsaTab == 0,
                        onClick = { onPulsaTabChange(0) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pulsa Reguler", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = pulsaTab == 1,
                        onClick = { onPulsaTabChange(1) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Paket Data Internet", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Products Section: Pulsa vs Paket Data
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (pulsaTab == 0) "Pilih Nominal Pulsa $detectedOperator" else "Pilih Paket Internet $detectedOperator",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (pulsaTab == 0) {
                            // Pulsa Grid
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.height(300.dp)
                            ) {
                                items(pulsaList) { pulsa ->
                                    val isSelected = selectedPulsa?.id == pulsa.id
                                    PulsaDenomCardItem(
                                        denom = pulsa,
                                        isSelected = isSelected,
                                        onClick = { onSelectPulsa(pulsa) }
                                    )
                                }
                            }
                        } else {
                            // Paket Data List
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                dataList.forEach { dataPkg ->
                                    val isSelected = selectedData?.id == dataPkg.id
                                    DataPkgCardItem(
                                        pkg = dataPkg,
                                        isSelected = isSelected,
                                        onClick = { onSelectData(dataPkg) }
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Payment Method Selector
            item {
                PaymentMethodSelector(
                    selectedMethod = selectedPaymentMethod,
                    onSelectMethod = onSelectPaymentMethod,
                    userBalance = userBalance,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Sticky Bottom Checkout Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Pembayaran",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val price = if (pulsaTab == 0) (selectedPulsa?.price ?: 0L) else (selectedData?.price ?: 0L)
                    val total = if (price > 0) price + selectedPaymentMethod.adminFee else 0L
                    Text(
                        text = TopUpRepository.formatRupiah(total),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                val hasSelection = if (pulsaTab == 0) selectedPulsa != null else selectedData != null
                Button(
                    onClick = onBuyClick,
                    enabled = phoneNumber.length >= 9 && hasSelection && !isProcessing,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("buy_pulsa_data_button")
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (pulsaTab == 0) "Beli Pulsa Sekarang" else "Beli Paket Data",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickPhoneChip(label: String, number: String, onSelect: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable { onSelect(number) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "$label: $number", fontSize = 10.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun PulsaDenomCardItem(
    denom: PulsaDenomination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
            .testTag("pulsa_item_${denom.nominal}")
    ) {
        Column {
            if (denom.isPromo) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(BrandAccent)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "HEMAT",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = TopUpRepository.formatRupiah(denom.nominal),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Bayar: ${TopUpRepository.formatRupiah(denom.price)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DataPkgCardItem(
    pkg: PaketDataDenomination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag("data_pkg_${pkg.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pkg.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (pkg.isBestSeller) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(BrandGreen)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "BEST SELLER",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${pkg.quotaMain} • Masa Aktif: ${pkg.validity}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = pkg.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = TopUpRepository.formatRupiah(pkg.price),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
