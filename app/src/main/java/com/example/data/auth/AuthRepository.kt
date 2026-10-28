package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.local.TopUpDao
import com.example.data.local.WalletEntity
import com.example.data.model.AppUser
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class AuthRepository(
    private val context: Context,
    private val dao: TopUpDao
) {
    private val auth: FirebaseAuth = Firebase.auth
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    private val firestore: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    private val _currentUser = MutableStateFlow<AppUser?>(null)
    val currentUser: StateFlow<AppUser?> = _currentUser.asStateFlow()

    private val _generatedOtp = MutableStateFlow<String?>(null)
    val generatedOtp: StateFlow<String?> = _generatedOtp.asStateFlow()

    init {
        // Check if there is an active Firebase user or locally saved session
        val fbUser = auth.currentUser
        if (fbUser != null) {
            _currentUser.value = AppUser(
                uid = fbUser.uid,
                displayName = fbUser.displayName ?: "Sobat Kilat",
                email = fbUser.email,
                phoneNumber = fbUser.phoneNumber,
                photoUrl = fbUser.photoUrl?.toString(),
                authProvider = "google"
            )
        }
    }

    fun signInWithGoogle(
        activity: Activity,
        scope: CoroutineScope,
        onSuccess: (AppUser) -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit
    ) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onError("Konfigurasi Google Sign-In tidak ditemukan (default_web_client_id)")
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    val user = authResult.user

                    if (user != null) {
                        val appUser = AppUser(
                            uid = user.uid,
                            displayName = user.displayName ?: "Sobat Kilat",
                            email = user.email,
                            phoneNumber = user.phoneNumber,
                            photoUrl = user.photoUrl?.toString(),
                            authProvider = "google"
                        )
                        _currentUser.value = appUser
                        syncUserProfile(appUser)
                        onSuccess(appUser)
                    } else {
                        onError("Gagal mendapatkan profil pengguna")
                    }
                } else {
                    onError("Tipe kredensial tidak sesuai")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "Google Sign-In dibatalkan oleh pengguna: ${e.message}")
                onCancelled()
            } catch (e: Exception) {
                Log.e("Auth", "Google Sign-In gagal", e)
                onError(e.localizedMessage ?: "Gagal masuk dengan akun Google")
            }
        }
    }

    fun sendPhoneOtp(phoneNumber: String, onOtpSent: (String) -> Unit) {
        val cleanNumber = phoneNumber.trim()
        val otp = (100000..999999).random().toString()
        _generatedOtp.value = otp
        onOtpSent(otp)
    }

    suspend fun verifyPhoneOtp(
        phoneNumber: String,
        inputOtp: String
    ): Result<AppUser> = withContext(Dispatchers.IO) {
        val expectedOtp = _generatedOtp.value ?: "123456"
        // Accept generated OTP or fallback universal demo OTP 123456
        if (inputOtp == expectedOtp || inputOtp == "123456") {
            val phoneUid = "phone_" + phoneNumber.filter { it.isDigit() }
            val formattedPhone = if (phoneNumber.startsWith("0")) "+62" + phoneNumber.substring(1) else phoneNumber
            val appUser = AppUser(
                uid = phoneUid,
                displayName = "Sobat " + phoneNumber.takeLast(4),
                phoneNumber = formattedPhone,
                authProvider = "phone"
            )
            _currentUser.value = appUser
            syncUserProfile(appUser)
            Result.success(appUser)
        } else {
            Result.failure(Exception("Kode OTP salah. Silakan coba lagi."))
        }
    }

    private suspend fun syncUserProfile(appUser: AppUser) {
        try {
            val currentWallet = dao.getWallet() ?: WalletEntity()
            dao.updateWallet(
                currentWallet.copy(
                    userName = appUser.displayName
                )
            )

            // Try syncing with Firestore if online and authenticated
            if (auth.currentUser != null) {
                val userDoc = firestore.collection("users").document(appUser.uid)
                val data = mapOf(
                    "displayName" to appUser.displayName,
                    "authProvider" to appUser.authProvider,
                    "balance" to currentWallet.balance,
                    "kilatCoins" to currentWallet.kilatCoins,
                    "email" to (appUser.email ?: ""),
                    "phoneNumber" to (appUser.phoneNumber ?: "")
                )
                userDoc.set(data).await()
            }
        } catch (e: Exception) {
            Log.w("Auth", "Profile sync note: ${e.message}")
        }
    }

    fun signOut(scope: CoroutineScope, onComplete: () -> Unit) {
        auth.signOut()
        _currentUser.value = null
        _generatedOtp.value = null
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("Auth", "Failed to clear credential state", e)
            } finally {
                onComplete()
            }
        }
    }
}
