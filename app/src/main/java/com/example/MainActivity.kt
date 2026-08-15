package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.auth.GoogleAuthManager
import com.example.auth.GoogleAccountRepository
import com.example.data.AppVersionInfo
import com.example.data.FirebaseManager
import com.example.data.CardItem
import com.example.data.DailyExpense
import com.example.data.ReminderItem
import com.example.data.PurchaseItem
import com.example.data.SubscriptionItem
import com.example.data.UpdateManager
import kotlinx.coroutines.launch
import com.example.data.JsonStorageManager
import com.example.data.StorageData
import com.example.navigation.Screen
import com.example.ui.components.AppBottomNavigation
import com.example.ui.screens.CardsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PurchasesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SubscriptionsScreen
import com.example.ui.theme.FinanceiroPessoalTheme
import com.example.ui.theme.TextSecondary

import com.example.data.CardPaymentItem
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import android.content.pm.PackageManager
import android.util.Base64
import android.util.Log
import java.security.MessageDigest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val info = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            }
            val signatures = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                info.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                info.signatures
            }
            signatures?.forEach { signature ->
                val md = MessageDigest.getInstance("SHA1")
                md.update(signature.toByteArray())
                val sha1 = md.digest().joinToString(":") { String.format("%02X", it) }
                Log.d("FIREBASE_SHA1", "SHA-1: $sha1")
            }
        } catch (e: Exception) {
            Log.e("FIREBASE_SHA1", "Erro ao obter SHA-1", e)
        }

        enableEdgeToEdge()
        setContent {
            MainAppStructure()
        }
    }
}

@Composable
fun MainAppStructure() {
    val context = LocalContext.current
    val storageManager = remember { JsonStorageManager(context) }
    val updateManager = remember { UpdateManager(context) }
    val firebaseManager = remember { FirebaseManager() }
    val googleAuthManager = remember(context) { GoogleAuthManager(context) }
    val googleAccountRepository = remember(storageManager) { GoogleAccountRepository(storageManager) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var storageData by remember { mutableStateOf(StorageData()) }

    FinanceiroPessoalTheme(themeMode = storageData.themeMode) {
        var currentScreenRoute by rememberSaveable { mutableStateOf(Screen.Dashboard.route) }
        var isLoading by remember { mutableStateOf(true) }
        var statusMessage by remember { mutableStateOf<String?>(null) }
        var isErrorStatus by remember { mutableStateOf(false) }
        var isAuthenticating by remember { mutableStateOf(false) }
        var updateInfo by remember { mutableStateOf<AppVersionInfo?>(null) }

        val googleSignInLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            isAuthenticating = true
            coroutineScope.launch {
                val handleResult = googleAuthManager.handleSignInResult(result.data)
                handleResult.fold(
                    onSuccess = { newAccount ->
                        googleAccountRepository.saveGoogleAccount(newAccount) { isSuccess, saveMessage ->
                            storageData = storageData.copy(googleAccount = newAccount)
                            statusMessage = "Conta conectada. Sincronizando..."
                            coroutineScope.launch {
                                val userId = newAccount.accountId
                                if (!userId.isNullOrBlank()) {
                                    val cloudData = firebaseManager.loadDataFromCloud(userId)
                                    if (cloudData != null) {
                                        storageData = cloudData.copy(googleAccount = newAccount)
                                        storageManager.saveData(storageData)
                                    } else {
                                        firebaseManager.syncDataToCloud(userId, storageData)
                                    }
                                }
                                isAuthenticating = false
                            }
                        }
                    },
                    onFailure = { error ->
                        statusMessage = error.message ?: "Falha ao conectar com o Google."
                        isErrorStatus = true
                        isAuthenticating = false
                    }
                )
            }
        }

        val isUserLoggedIn = storageData.googleAccount?.isConnected == true

        if (updateInfo != null) {
            AlertDialog(
                onDismissRequest = { updateInfo = null },
                title = { Text("Nova Atualização!") },
                text = { Text("Uma nova versão (${updateInfo?.latestVersionName}) está disponível.") },
                confirmButton = {
                    Button(onClick = {
                        updateInfo?.let { updateManager.downloadAndInstallApk(it.apkUrl) }
                        updateInfo = null
                    }) { Text("Atualizar agora") }
                },
                dismissButton = {
                    TextButton(onClick = { updateInfo = null }) { Text("Depois", color = TextSecondary) }
                }
            )
        }

        LaunchedEffect(Unit) {
            val lastAccount = googleAuthManager.getLastSignedInAccount()
            if (lastAccount != null) {
                storageData = storageData.copy(googleAccount = lastAccount)
            }
            val loadResult = storageManager.loadData()
            storageData = storageData.copy(
                cards = loadResult.data?.cards ?: storageData.cards,
                purchases = loadResult.data?.purchases ?: storageData.purchases,
                subscriptions = loadResult.data?.subscriptions ?: storageData.subscriptions,
                dailyExpenses = loadResult.data?.dailyExpenses ?: storageData.dailyExpenses,
                reminders = loadResult.data?.reminders ?: storageData.reminders,
                themeMode = loadResult.data?.themeMode ?: storageData.themeMode
            )
            val userId = lastAccount?.accountId
            if (lastAccount != null && !userId.isNullOrBlank()) {
                val cloudData = firebaseManager.loadDataFromCloud(userId)
                if (cloudData != null) {
                    storageData = cloudData.copy(googleAccount = lastAccount)
                    storageManager.saveData(storageData)
                }
            }
            updateInfo = updateManager.checkForUpdate()
            isLoading = false
        }

        fun updateAndSaveData(newStorageData: StorageData) {
            storageData = newStorageData
            val saveResult = storageManager.saveData(newStorageData)
            statusMessage = saveResult.message
            isErrorStatus = !saveResult.isSuccess
            coroutineScope.launch {
                val account = storageData.googleAccount ?: googleAccountRepository.getGoogleAccount()
                val userId = account?.accountId
                if (account?.isConnected == true && !userId.isNullOrBlank()) {
                    firebaseManager.syncDataToCloud(userId, newStorageData)
                }
                snackbarHostState.showSnackbar(statusMessage ?: saveResult.message)
                delay(3000)
                statusMessage = null
            }
        }

        fun handlePayReminder(reminder: ReminderItem) {
            if (reminder.isPaid) return
            
            val updatedReminder = reminder.copy(isPaid = true)
            val newExpense = DailyExpense(
                name = reminder.name,
                value = reminder.value,
                date = reminder.date,
                observation = "Pago via lembrete"
            )
            
            val updatedReminders = storageData.reminders.map {
                if (it.id == reminder.id) updatedReminder else it
            }
            val updatedExpenses = storageData.dailyExpenses + newExpense
            
            updateAndSaveData(storageData.copy(
                reminders = updatedReminders,
                dailyExpenses = updatedExpenses
            ))
        }

        fun registerInvoicePayment(card: CardItem) {
            val currentCal = Calendar.getInstance()
            val currentMonth = currentCal.get(Calendar.MONTH) + 1
            val currentYear = currentCal.get(Calendar.YEAR)
            val newPayment = CardPaymentItem(
                cardId = card.id,
                month = currentMonth,
                year = currentYear,
                paid = true,
                paidAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(currentCal.time)
            )
            val updatedCardPayments = storageData.cardPayments.filterNot {
                it.cardId == card.id && it.month == currentMonth && it.year == currentYear
            } + newPayment
            val updatedPurchases = storageData.purchases.map { purchase ->
                val isSameCard = purchase.cardId == card.id || (purchase.cardId.isBlank() && purchase.cardName == card.name)
                if (isSameCard) {
                    val calc = purchase.calculateInstallments(currentMonth, currentYear)
                    if (purchase.isInstallment && calc.status == "Em andamento") {
                        purchase.copy(paidInstallmentsCount = calc.currentInstallment)
                    } else purchase
                } else purchase
            }
            updateAndSaveData(storageData.copy(cardPayments = updatedCardPayments, purchases = updatedPurchases))
        }

        val currentScreen = Screen.items.find { it.route == currentScreenRoute } ?: Screen.Dashboard

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Carregando...", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else if (!isUserLoggedIn) {
            LoginScreen(
                onLoginClick = { googleSignInLauncher.launch(googleAuthManager.getSignInIntent()) },
                isLoading = isAuthenticating,
                errorMessage = if (isErrorStatus) statusMessage else null
            )
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    AppBottomNavigation(
                        currentScreen = currentScreen,
                        onScreenSelected = { screen -> currentScreenRoute = screen.route }
                    )
                },
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
            ) { innerPadding ->
                val screenModifier = Modifier.padding(innerPadding).statusBarsPadding()
                when (currentScreen) {
                    Screen.Dashboard -> DashboardScreen(
                        cards = storageData.cards,
                        purchases = storageData.purchases,
                        subscriptions = storageData.subscriptions,
                        dailyExpenses = storageData.dailyExpenses,
                        reminders = storageData.reminders,
                        onUpdateReminders = { newReminders -> updateAndSaveData(storageData.copy(reminders = newReminders)) },
                        onPayReminder = ::handlePayReminder,
                        modifier = screenModifier
                    )
                    Screen.Purchases -> PurchasesScreen(
                        purchases = storageData.purchases,
                        cards = storageData.cards,
                        cardPayments = storageData.cardPayments,
                        dailyExpenses = storageData.dailyExpenses,
                        subscriptions = storageData.subscriptions,
                        onUpdatePurchases = { newPurchases -> updateAndSaveData(storageData.copy(purchases = newPurchases)) },
                        onUpdateDailyExpenses = { newExpenses -> updateAndSaveData(storageData.copy(dailyExpenses = newExpenses)) },
                        onUpdateSubscriptions = { newSubs -> updateAndSaveData(storageData.copy(subscriptions = newSubs)) },
                        onUpdateCards = { newCards -> updateAndSaveData(storageData.copy(cards = newCards)) },
                        onRegisterInvoicePayment = ::registerInvoicePayment,
                        modifier = screenModifier
                    )
                    Screen.Settings -> SettingsScreen(
                        googleAccount = storageData.googleAccount,
                        themeMode = storageData.themeMode,
                        onThemeModeChange = { newMode -> updateAndSaveData(storageData.copy(themeMode = newMode)) },
                        onConnectClick = { googleSignInLauncher.launch(googleAuthManager.getSignInIntent()) },
                        onDisconnectClick = {
                            googleAuthManager.signOut { _ ->
                                googleAccountRepository.clearGoogleAccount { _, _ ->
                                    storageData = storageData.copy(googleAccount = null)
                                    statusMessage = "Conta desconectada."
                                    isErrorStatus = false
                                }
                            }
                        },
                        statusMessage = statusMessage,
                        isErrorStatus = isErrorStatus,
                        modifier = screenModifier
                    )
                }
            }
        }
    }
}
