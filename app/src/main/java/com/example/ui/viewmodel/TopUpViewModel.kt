package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthRepository
import com.example.data.local.AppDatabase
import com.example.data.local.TransactionEntity
import com.example.data.local.WalletEntity
import com.example.data.model.AppUser
import com.example.data.model.CustomerPlnInfo
import com.example.data.model.Game
import com.example.data.model.GameDenomination
import com.example.data.model.PaketDataDenomination
import com.example.data.model.PaymentMethod
import com.example.data.model.PlnDenomination
import com.example.data.model.PromoBanner
import com.example.data.model.PulsaDenomination
import com.example.data.model.TransactionType
import com.example.data.repository.TopUpRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class TopUpViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TopUpRepository
    private val authRepository: AuthRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TopUpRepository(database.topUpDao())
        authRepository = AuthRepository(application, database.topUpDao())
        viewModelScope.launch {
            repository.ensureInitialWallet()
        }
    }

    // AUTH STATE
    val currentUser: StateFlow<AppUser?> = authRepository.currentUser
    val generatedOtp: StateFlow<String?> = authRepository.generatedOtp

    private val _showLoginModal = MutableStateFlow(false)
    val showLoginModal: StateFlow<Boolean> = _showLoginModal.asStateFlow()

    private val _showProfileSheet = MutableStateFlow(false)
    val showProfileSheet: StateFlow<Boolean> = _showProfileSheet.asStateFlow()

    private val _isGoogleLoading = MutableStateFlow(false)
    val isGoogleLoading: StateFlow<Boolean> = _isGoogleLoading.asStateFlow()

    private val _isPhoneLoading = MutableStateFlow(false)
    val isPhoneLoading: StateFlow<Boolean> = _isPhoneLoading.asStateFlow()

    fun openLoginModal() {
        _showLoginModal.value = true
    }

    fun closeLoginModal() {
        _showLoginModal.value = false
    }

    fun openProfileSheet() {
        _showProfileSheet.value = true
    }

    fun closeProfileSheet() {
        _showProfileSheet.value = false
    }

    fun signInWithGoogle(activity: Activity) {
        _isGoogleLoading.value = true
        authRepository.signInWithGoogle(
            activity = activity,
            scope = viewModelScope,
            onSuccess = { user ->
                _isGoogleLoading.value = false
                _showLoginModal.value = false
                emitMessage("Berhasil masuk sebagai ${user.displayName}!")
            },
            onError = { error ->
                _isGoogleLoading.value = false
                emitMessage(error)
            },
            onCancelled = {
                _isGoogleLoading.value = false
            }
        )
    }

    fun sendPhoneOtp(phoneNumber: String) {
        authRepository.sendPhoneOtp(phoneNumber) { otp ->
            emitMessage("Kode OTP terkirim ke $phoneNumber: $otp")
        }
    }

    fun verifyPhoneOtp(phoneNumber: String, otp: String) {
        viewModelScope.launch {
            _isPhoneLoading.value = true
            val result = authRepository.verifyPhoneOtp(phoneNumber, otp)
            _isPhoneLoading.value = false
            result.onSuccess { user ->
                _showLoginModal.value = false
                emitMessage("Berhasil masuk dengan nomor ${user.phoneNumber ?: phoneNumber}!")
            }.onFailure { err ->
                emitMessage(err.message ?: "Verifikasi OTP gagal")
            }
        }
    }

    fun signOut() {
        authRepository.signOut(viewModelScope) {
            emitMessage("Anda telah keluar dari akun")
        }
    }

    // WALLET & TRANSACTIONS STATE
    val walletState: StateFlow<WalletEntity?> = repository.walletFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WalletEntity()
    )

    val transactionsState: StateFlow<List<TransactionEntity>> = repository.transactionsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _messageFlow = MutableSharedFlow<String>()
    val messageFlow: SharedFlow<String> = _messageFlow.asSharedFlow()

    // Banners & Catalogs
    val promoBanners: List<PromoBanner> = repository.getPromoBanners()
    val games: List<Game> = repository.getGames()
    val plnDenominations: List<PlnDenomination> = repository.getPlnDenominations()

    // Active Screen Tab
    // 0 = Beranda, 1 = Game, 2 = PLN Listrik, 3 = Pulsa & Data, 4 = Riwayat
    private val _currentNavIndex = MutableStateFlow(0)
    val currentNavIndex: StateFlow<Int> = _currentNavIndex.asStateFlow()

    fun navigateTo(index: Int) {
        _currentNavIndex.value = index
    }

    // GAME TOPUP STATE
    private val _selectedGame = MutableStateFlow<Game>(games.first())
    val selectedGame: StateFlow<Game> = _selectedGame.asStateFlow()

    private val _gameUserId = MutableStateFlow("")
    val gameUserId: StateFlow<String> = _gameUserId.asStateFlow()

    private val _gameZoneId = MutableStateFlow("")
    val gameZoneId: StateFlow<String> = _gameZoneId.asStateFlow()

    private val _verifiedGameNickname = MutableStateFlow<String?>(null)
    val verifiedGameNickname: StateFlow<String?> = _verifiedGameNickname.asStateFlow()

    private val _selectedGameDenom = MutableStateFlow<GameDenomination?>(null)
    val selectedGameDenom: StateFlow<GameDenomination?> = _selectedGameDenom.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow<PaymentMethod>(PaymentMethod.SALDO)
    val selectedPaymentMethod: StateFlow<PaymentMethod> = _selectedPaymentMethod.asStateFlow()

    fun selectGame(game: Game) {
        _selectedGame.value = game
        _selectedGameDenom.value = null
        _verifiedGameNickname.value = null
    }

    fun setGameUserId(userId: String) {
        _gameUserId.value = userId
        if (userId.length >= 5) {
            _verifiedGameNickname.value = repository.mockValidateGameAccount(_selectedGame.value.id, userId, _gameZoneId.value)
        } else {
            _verifiedGameNickname.value = null
        }
    }

    fun setGameZoneId(zoneId: String) {
        _gameZoneId.value = zoneId
        if (_gameUserId.value.length >= 5) {
            _verifiedGameNickname.value = repository.mockValidateGameAccount(_selectedGame.value.id, _gameUserId.value, zoneId)
        }
    }

    fun selectGameDenomination(denom: GameDenomination) {
        _selectedGameDenom.value = denom
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _selectedPaymentMethod.value = method
    }

    // PLN STATE
    private val _plnIdPelanggan = MutableStateFlow("14238765432")
    val plnIdPelanggan: StateFlow<String> = _plnIdPelanggan.asStateFlow()

    private val _customerPlnInfo = MutableStateFlow<CustomerPlnInfo?>(
        repository.mockValidatePlnCustomer("14238765432")
    )
    val customerPlnInfo: StateFlow<CustomerPlnInfo?> = _customerPlnInfo.asStateFlow()

    private val _selectedPlnDenom = MutableStateFlow<PlnDenomination?>(plnDenominations[1]) // Default 50k
    val selectedPlnDenom: StateFlow<PlnDenomination?> = _selectedPlnDenom.asStateFlow()

    fun setPlnIdPelanggan(id: String) {
        _plnIdPelanggan.value = id
        if (id.length >= 10) {
            _customerPlnInfo.value = repository.mockValidatePlnCustomer(id)
        } else {
            _customerPlnInfo.value = null
        }
    }

    fun selectPlnDenomination(denom: PlnDenomination) {
        _selectedPlnDenom.value = denom
    }

    // PULSA & DATA STATE
    private val _phoneNumber = MutableStateFlow("081234567890")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _detectedOperator = MutableStateFlow("Telkomsel")
    val detectedOperator: StateFlow<String> = _detectedOperator.asStateFlow()

    // 0 = Pulsa, 1 = Paket Data
    private val _pulsaTab = MutableStateFlow(0)
    val pulsaTab: StateFlow<Int> = _pulsaTab.asStateFlow()

    private val _selectedPulsaDenom = MutableStateFlow<PulsaDenomination?>(null)
    val selectedPulsaDenom: StateFlow<PulsaDenomination?> = _selectedPulsaDenom.asStateFlow()

    private val _selectedDataDenom = MutableStateFlow<PaketDataDenomination?>(null)
    val selectedDataDenom: StateFlow<PaketDataDenomination?> = _selectedDataDenom.asStateFlow()

    fun setPhoneNumber(number: String) {
        _phoneNumber.value = number
        val operator = repository.detectOperator(number)
        _detectedOperator.value = operator
    }

    fun setPulsaTab(tab: Int) {
        _pulsaTab.value = tab
    }

    fun selectPulsaDenomination(denom: PulsaDenomination) {
        _selectedPulsaDenom.value = denom
        _selectedDataDenom.value = null
    }

    fun selectDataDenomination(denom: PaketDataDenomination) {
        _selectedDataDenom.value = denom
        _selectedPulsaDenom.value = null
    }

    fun getPulsaList(): List<PulsaDenomination> = repository.getPulsaDenominations(_detectedOperator.value)
    fun getDataList(): List<PaketDataDenomination> = repository.getPaketDataDenominations(_detectedOperator.value)

    // DIALOGS & RECEIPT
    private val _activeReceipt = MutableStateFlow<TransactionEntity?>(null)
    val activeReceipt: StateFlow<TransactionEntity?> = _activeReceipt.asStateFlow()

    private val _showTopUpBalanceDialog = MutableStateFlow(false)
    val showTopUpBalanceDialog: StateFlow<Boolean> = _showTopUpBalanceDialog.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    fun openReceipt(trx: TransactionEntity) {
        _activeReceipt.value = trx
    }

    fun closeReceipt() {
        _activeReceipt.value = null
    }

    fun toggleTopUpBalanceDialog(show: Boolean) {
        _showTopUpBalanceDialog.value = show
    }

    // CHECKOUT ACTIONS
    fun buyGameTopUp() {
        val game = _selectedGame.value
        val denom = _selectedGameDenom.value ?: return
        val userId = _gameUserId.value
        val zoneId = _gameZoneId.value

        if (userId.isBlank()) {
            emitMessage("Masukkan ${game.userIdLabel} terlebih dahulu")
            return
        }

        val targetStr = if (!game.zoneIdLabel.isNullOrBlank() && zoneId.isNotBlank()) {
            "$userId ($zoneId)"
        } else {
            userId
        }

        val nickname = _verifiedGameNickname.value ?: repository.mockValidateGameAccount(game.id, userId, zoneId)
        val sn = "SN-${game.id.uppercase()}-${Random.nextInt(10000000, 99999999)}"

        executeTransaction(
            type = TransactionType.GAME,
            title = game.name,
            subtitle = "${denom.name} (${denom.amountFormatted})",
            targetNumberOrId = targetStr,
            amount = denom.price,
            paymentMethod = _selectedPaymentMethod.value,
            snOrToken = sn,
            notes = "ID Akun: $targetStr • Nickname: $nickname"
        )
    }

    fun buyPlnToken() {
        val idMeter = _plnIdPelanggan.value
        val denom = _selectedPlnDenom.value ?: return
        if (idMeter.length < 10) {
            emitMessage("Masukkan No Meter / ID Pelanggan yang valid (min 10 digit)")
            return
        }
        val customerInfo = _customerPlnInfo.value ?: repository.mockValidatePlnCustomer(idMeter)
        val token20 = repository.generatePlnToken()

        executeTransaction(
            type = TransactionType.PLN,
            title = "Token Listrik PLN Prabayar",
            subtitle = "Nominal ${TopUpRepository.formatRupiah(denom.nominal)} (${denom.kwhEstimate})",
            targetNumberOrId = idMeter,
            amount = denom.price,
            paymentMethod = _selectedPaymentMethod.value,
            snOrToken = token20,
            notes = "Pelanggan: ${customerInfo.nama} • Tarif: ${customerInfo.tarifDaya}"
        )
    }

    fun buyPulsaOrData() {
        val phone = _phoneNumber.value
        if (phone.length < 9) {
            emitMessage("Masukkan nomor telepon yang valid (min 9 digit)")
            return
        }
        val isPulsa = _pulsaTab.value == 0
        if (isPulsa) {
            val denom = _selectedPulsaDenom.value
            if (denom == null) {
                emitMessage("Pilih nominal pulsa yang ingin dibeli")
                return
            }
            val sn = "SN-${denom.operator.uppercase()}-${Random.nextInt(10000000, 99999999)}"
            executeTransaction(
                type = TransactionType.PULSA,
                title = "Pulsa ${denom.operator}",
                subtitle = "Nominal ${TopUpRepository.formatRupiah(denom.nominal)}",
                targetNumberOrId = phone,
                amount = denom.price,
                paymentMethod = _selectedPaymentMethod.value,
                snOrToken = sn,
                notes = "Operator: ${denom.operator} • Status Pulsa Masuk"
            )
        } else {
            val denom = _selectedDataDenom.value
            if (denom == null) {
                emitMessage("Pilih paket data yang ingin dibeli")
                return
            }
            val sn = "SN-DATA-${denom.operator.uppercase()}-${Random.nextInt(10000000, 99999999)}"
            executeTransaction(
                type = TransactionType.DATA,
                title = "Paket Data ${denom.operator}",
                subtitle = "${denom.title} (${denom.validity})",
                targetNumberOrId = phone,
                amount = denom.price,
                paymentMethod = _selectedPaymentMethod.value,
                snOrToken = sn,
                notes = "${denom.quotaMain} • ${denom.description}"
            )
        }
    }

    fun topUpSaldo(amount: Long, paymentMethod: PaymentMethod) {
        viewModelScope.launch {
            _isProcessing.value = true
            val result = repository.topUpWalletBalance(amount, paymentMethod)
            _isProcessing.value = false
            _showTopUpBalanceDialog.value = false
            result.onSuccess { trx ->
                emitMessage("Isi saldo ${TopUpRepository.formatRupiah(amount)} berhasil!")
                _activeReceipt.value = trx
            }.onFailure { err ->
                emitMessage("Gagal isi saldo: ${err.message}")
            }
        }
    }

    private fun executeTransaction(
        type: TransactionType,
        title: String,
        subtitle: String,
        targetNumberOrId: String,
        amount: Long,
        paymentMethod: PaymentMethod,
        snOrToken: String,
        notes: String
    ) {
        viewModelScope.launch {
            _isProcessing.value = true
            val result = repository.processPurchase(
                type = type,
                title = title,
                subtitle = subtitle,
                targetNumberOrId = targetNumberOrId,
                amount = amount,
                paymentMethod = paymentMethod,
                generatedTokenOrSn = snOrToken,
                notes = notes
            )
            _isProcessing.value = false
            result.onSuccess { trx ->
                emitMessage("Transaksi berhasil!")
                _activeReceipt.value = trx
            }.onFailure { error ->
                emitMessage(error.message ?: "Transaksi gagal diproses")
            }
        }
    }

    private fun emitMessage(msg: String) {
        viewModelScope.launch {
            _messageFlow.emit(msg)
        }
    }
}
