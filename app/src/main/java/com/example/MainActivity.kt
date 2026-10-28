package com.example

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.DigitalReceiptDialog
import com.example.ui.components.LoginModal
import com.example.ui.components.TopBar
import com.example.ui.components.TopUpSaldoDialog
import com.example.ui.components.UserProfileSheet
import com.example.ui.screens.GameTopUpScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PlnScreen
import com.example.ui.screens.PulsaDataScreen
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.TopUpKilatTheme
import com.example.ui.viewmodel.TopUpViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TopUpKilatTheme {
                MainAppRoot()
            }
        }
    }
}

data class NavTabItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun MainAppRoot(viewModel: TopUpViewModel = viewModel()) {
    val context = LocalContext.current
    val activity = context as Activity
    val snackbarHostState = remember { SnackbarHostState() }

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val showLoginModal by viewModel.showLoginModal.collectAsStateWithLifecycle()
    val showProfileSheet by viewModel.showProfileSheet.collectAsStateWithLifecycle()
    val isGoogleLoading by viewModel.isGoogleLoading.collectAsStateWithLifecycle()
    val isPhoneLoading by viewModel.isPhoneLoading.collectAsStateWithLifecycle()
    val generatedOtp by viewModel.generatedOtp.collectAsStateWithLifecycle()

    val wallet by viewModel.walletState.collectAsStateWithLifecycle()
    val transactions by viewModel.transactionsState.collectAsStateWithLifecycle()
    val currentNavIndex by viewModel.currentNavIndex.collectAsStateWithLifecycle()

    // Game state
    val selectedGame by viewModel.selectedGame.collectAsStateWithLifecycle()
    val gameUserId by viewModel.gameUserId.collectAsStateWithLifecycle()
    val gameZoneId by viewModel.gameZoneId.collectAsStateWithLifecycle()
    val verifiedNickname by viewModel.verifiedGameNickname.collectAsStateWithLifecycle()
    val selectedGameDenom by viewModel.selectedGameDenom.collectAsStateWithLifecycle()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsStateWithLifecycle()

    // PLN state
    val plnIdPelanggan by viewModel.plnIdPelanggan.collectAsStateWithLifecycle()
    val customerPlnInfo by viewModel.customerPlnInfo.collectAsStateWithLifecycle()
    val selectedPlnDenom by viewModel.selectedPlnDenom.collectAsStateWithLifecycle()

    // Pulsa & Data state
    val phoneNumber by viewModel.phoneNumber.collectAsStateWithLifecycle()
    val detectedOperator by viewModel.detectedOperator.collectAsStateWithLifecycle()
    val pulsaTab by viewModel.pulsaTab.collectAsStateWithLifecycle()
    val selectedPulsaDenom by viewModel.selectedPulsaDenom.collectAsStateWithLifecycle()
    val selectedDataDenom by viewModel.selectedDataDenom.collectAsStateWithLifecycle()

    // Dialogs & Receipt
    val activeReceipt by viewModel.activeReceipt.collectAsStateWithLifecycle()
    val showTopUpBalanceDialog by viewModel.showTopUpBalanceDialog.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()

    // Handle back button for sub-screens
    BackHandler(enabled = currentNavIndex != 0) {
        viewModel.navigateTo(0)
    }

    LaunchedEffect(Unit) {
        viewModel.messageFlow.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val navItems = listOf(
        NavTabItem("Beranda", Icons.Filled.Home, Icons.Outlined.Home, "nav_beranda"),
        NavTabItem("Game", Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports, "nav_game"),
        NavTabItem("PLN", Icons.Filled.FlashOn, Icons.Outlined.FlashOn, "nav_pln"),
        NavTabItem("Pulsa/Data", Icons.Filled.PhoneAndroid, Icons.Outlined.PhoneAndroid, "nav_pulsa"),
        NavTabItem("Riwayat", Icons.Filled.History, Icons.Outlined.History, "nav_riwayat")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopBar(
                userName = currentUser?.displayName ?: (wallet?.userName ?: "Sobat Kilat"),
                isLoggedIn = currentUser != null,
                onNotificationClick = {
                    Toast.makeText(context, "Promo Flash Sale Diamond Game & Diskon Token PLN sedang aktif!", Toast.LENGTH_LONG).show()
                },
                onProfileClick = {
                    viewModel.openProfileSheet()
                },
                onLoginClick = {
                    viewModel.openLoginModal()
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = currentNavIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.navigateTo(index) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandPrimary,
                            selectedTextColor = BrandPrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = BrandPrimary.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentNavIndex) {
                0 -> HomeScreen(
                    balance = wallet?.balance ?: 0L,
                    kilatCoins = wallet?.kilatCoins ?: 0L,
                    promoBanners = viewModel.promoBanners,
                    games = viewModel.games,
                    recentTransactions = transactions,
                    onTopUpSaldoClick = { viewModel.toggleTopUpBalanceDialog(true) },
                    onNavigateToGame = { game ->
                        if (game != null) viewModel.selectGame(game)
                        viewModel.navigateTo(1)
                    },
                    onNavigateToPln = { viewModel.navigateTo(2) },
                    onNavigateToPulsa = {
                        viewModel.setPulsaTab(0)
                        viewModel.navigateTo(3)
                    },
                    onNavigateToData = {
                        viewModel.setPulsaTab(1)
                        viewModel.navigateTo(3)
                    },
                    onNavigateToHistory = { viewModel.navigateTo(4) },
                    onOpenReceipt = { trx -> viewModel.openReceipt(trx) },
                    onPromoClick = {
                        Toast.makeText(context, "Kupon HEMATKILAT aktif: Diskon Biaya Admin 100%", Toast.LENGTH_SHORT).show()
                    }
                )

                1 -> GameTopUpScreen(
                    games = viewModel.games,
                    selectedGame = selectedGame,
                    userId = gameUserId,
                    zoneId = gameZoneId,
                    verifiedNickname = verifiedNickname,
                    selectedDenom = selectedGameDenom,
                    selectedPaymentMethod = selectedPaymentMethod,
                    userBalance = wallet?.balance ?: 0L,
                    isProcessing = isProcessing,
                    onSelectGame = { viewModel.selectGame(it) },
                    onUserIdChange = { viewModel.setGameUserId(it) },
                    onZoneIdChange = { viewModel.setGameZoneId(it) },
                    onSelectDenom = { viewModel.selectGameDenomination(it) },
                    onSelectPaymentMethod = { viewModel.selectPaymentMethod(it) },
                    onBuyClick = { viewModel.buyGameTopUp() }
                )

                2 -> PlnScreen(
                    idPelanggan = plnIdPelanggan,
                    customerInfo = customerPlnInfo,
                    denominations = viewModel.plnDenominations,
                    selectedDenom = selectedPlnDenom,
                    selectedPaymentMethod = selectedPaymentMethod,
                    userBalance = wallet?.balance ?: 0L,
                    isProcessing = isProcessing,
                    onIdPelangganChange = { viewModel.setPlnIdPelanggan(it) },
                    onSelectDenom = { viewModel.selectPlnDenomination(it) },
                    onSelectPaymentMethod = { viewModel.selectPaymentMethod(it) },
                    onBuyClick = { viewModel.buyPlnToken() }
                )

                3 -> PulsaDataScreen(
                    phoneNumber = phoneNumber,
                    detectedOperator = detectedOperator,
                    pulsaTab = pulsaTab,
                    pulsaList = viewModel.getPulsaList(),
                    dataList = viewModel.getDataList(),
                    selectedPulsa = selectedPulsaDenom,
                    selectedData = selectedDataDenom,
                    selectedPaymentMethod = selectedPaymentMethod,
                    userBalance = wallet?.balance ?: 0L,
                    isProcessing = isProcessing,
                    onPhoneNumberChange = { viewModel.setPhoneNumber(it) },
                    onPulsaTabChange = { viewModel.setPulsaTab(it) },
                    onSelectPulsa = { viewModel.selectPulsaDenomination(it) },
                    onSelectData = { viewModel.selectDataDenomination(it) },
                    onSelectPaymentMethod = { viewModel.selectPaymentMethod(it) },
                    onBuyClick = { viewModel.buyPulsaOrData() }
                )

                4 -> HistoryScreen(
                    transactions = transactions,
                    onSelectTransaction = { viewModel.openReceipt(it) }
                )
            }
        }
    }

    // Login Modal (Google Sign-In & Phone Number OTP)
    if (showLoginModal) {
        LoginModal(
            isGoogleLoading = isGoogleLoading,
            isPhoneLoading = isPhoneLoading,
            generatedOtp = generatedOtp,
            onDismiss = { viewModel.closeLoginModal() },
            onGoogleSignInClick = {
                viewModel.signInWithGoogle(activity)
            },
            onSendOtpClick = { phone ->
                viewModel.sendPhoneOtp(phone)
            },
            onVerifyOtpClick = { phone, otp ->
                viewModel.verifyPhoneOtp(phone, otp)
            }
        )
    }

    // User Profile Sheet
    if (showProfileSheet) {
        UserProfileSheet(
            currentUser = currentUser,
            balance = wallet?.balance ?: 0L,
            kilatCoins = wallet?.kilatCoins ?: 0L,
            onDismiss = { viewModel.closeProfileSheet() },
            onOpenLoginModal = {
                viewModel.openLoginModal()
            },
            onSignOutClick = {
                viewModel.signOut()
            }
        )
    }

    // Top Up Saldo Modal Dialog
    if (showTopUpBalanceDialog) {
        TopUpSaldoDialog(
            isProcessing = isProcessing,
            onDismiss = { viewModel.toggleTopUpBalanceDialog(false) },
            onConfirmTopUp = { amount, method ->
                viewModel.topUpSaldo(amount, method)
            }
        )
    }

    // Digital Receipt Modal Dialog
    activeReceipt?.let { receipt ->
        DigitalReceiptDialog(
            transaction = receipt,
            onDismiss = { viewModel.closeReceipt() }
        )
    }
}
