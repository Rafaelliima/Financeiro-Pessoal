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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.example.gmail.GmailRepository
import com.example.gmail.GmailService
import kotlinx.coroutines.launch
import com.example.data.JsonStorageManager
import com.example.data.StorageData
import com.example.navigation.Screen
import com.example.ui.components.AppBottomNavigation
import com.example.ui.screens.CardsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PurchasesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SubscriptionsScreen
import com.example.ui.theme.FinanceiroPessoalTheme
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

import com.example.data.CardPaymentItem
import com.example.ui.screens.CardItem
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinanceiroPessoalTheme {
                MainAppStructure()
            }
        }
    }
}

@Composable
fun MainAppStructure() {
    val context = LocalContext.current
    val storageManager = remember { JsonStorageManager(context) }
    val googleAuthManager = remember(context) { GoogleAuthManager(context) }
    val googleAccountRepository = remember(storageManager) { GoogleAccountRepository(storageManager) }
    val gmailService = remember(context) { GmailService(context) }
    val gmailRepository = remember(gmailService, googleAccountRepository) { GmailRepository(gmailService, googleAccountRepository) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentScreenRoute by rememberSaveable { mutableStateOf(Screen.Dashboard.route) }
    var isLoading by remember { mutableStateOf(true) }
    var storageData by remember { mutableStateOf(StorageData()) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isErrorStatus by remember { mutableStateOf(false) }
    var isTestingGmail by remember { mutableStateOf(false) }

    // Launcher oficial do Google Sign-In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val handleResult = googleAuthManager.handleSignInResult(result.data)
        handleResult.fold(
            onSuccess = { newAccount ->
                googleAccountRepository.saveGoogleAccount(newAccount) { isSuccess, saveMessage ->
                    storageData = storageData.copy(googleAccount = newAccount)
                    statusMessage = "Conta Google conectada com sucesso (${newAccount.userEmail})."
                    isErrorStatus = false
                }
            },
            onFailure = { error ->
                statusMessage = error.message ?: "Falha ao conectar com o Google."
                isErrorStatus = true
            }
        )
    }

    // Carrega os dados do arquivo JSON UMA ÚNICA VEZ ao abrir o app
    LaunchedEffect(Unit) {
        val loadResult = storageManager.loadData()
        storageData = loadResult.data ?: StorageData()
        statusMessage = loadResult.message
        isErrorStatus = !loadResult.isSuccess
        isLoading = false

        if (loadResult.isSuccess) {
            delay(3000)
            if (statusMessage == loadResult.message) {
                statusMessage = null
            }
        }
    }

    // Sincronização automática de e-mails de Pix e faturas (ao iniciar o app e a cada 1 hora)
    val userEmail = storageData.googleAccount?.userEmail
    val isGoogleConnected = storageData.googleAccount?.isConnected == true
    LaunchedEffect(userEmail, isGoogleConnected) {
        if (isGoogleConnected && !userEmail.isNullOrBlank()) {
            while (true) {
                try {
                    val syncResult = gmailRepository.syncAndImportMercadoPagoExpenses(
                        currentData = storageData,
                        saveStorageData = { newStorageData ->
                            storageManager.saveData(newStorageData)
                        }
                    )
                    if (syncResult.updatedStorageData != null) {
                        storageData = syncResult.updatedStorageData
                    }
                } catch (_: Exception) {
                    // Ignora erros temporários em plano de fundo sem travar a interface
                }
                // Aguarda 1 hora até a próxima sincronização automática
                delay(3600_000L)
            }
        }
    }

    // Função central para persistir alterações no JSON e exibir feedback
    fun updateAndSaveData(newStorageData: StorageData) {
        storageData = newStorageData
        val saveResult = storageManager.saveData(newStorageData)
        statusMessage = saveResult.message
        isErrorStatus = !saveResult.isSuccess
        coroutineScope.launch {
            snackbarHostState.showSnackbar(saveResult.message)
            delay(3000)
            if (statusMessage == saveResult.message) {
                statusMessage = null
            }
        }
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
            val isLinkedCard = purchase.cardId == card.id || (purchase.cardId.isBlank() && purchase.cardName == card.name)
            if (isLinkedCard && !purchase.isQuitada) {
                val newPaidCount = purchase.paidInstallmentsCount + 1
                val nowQuitada = newPaidCount >= purchase.totalInstallments
                val completedDate = if (nowQuitada && purchase.completedAt == null) {
                    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(currentCal.time)
                } else purchase.completedAt
                purchase.copy(
                    paidInstallmentsCount = newPaidCount,
                    isQuitada = nowQuitada,
                    completedAt = completedDate
                )
            } else {
                purchase
            }
        }

        updateAndSaveData(
            storageData.copy(
                purchases = updatedPurchases,
                cardPayments = updatedCardPayments
            )
        )
    }

    val currentScreen = Screen.items.find { it.route == currentScreenRoute } ?: Screen.Dashboard

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("main_scaffold"),
        bottomBar = {
            AppBottomNavigation(
                currentScreen = currentScreen,
                onScreenSelected = { screen ->
                    currentScreenRoute = screen.route
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        val screenModifier = Modifier
            .padding(innerPadding)
            .statusBarsPadding()

        if (isLoading) {
            Box(
                modifier = screenModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.testTag("loading_indicator"),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Carregando dados financeiros...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        } else {
            when (currentScreen) {
                Screen.Dashboard -> DashboardScreen(
                    cards = storageData.cards,
                    purchases = storageData.purchases,
                    subscriptions = storageData.subscriptions,
                    dailyExpenses = storageData.dailyExpenses,
                    modifier = screenModifier
                )
                Screen.Cards -> CardsScreen(
                    cards = storageData.cards,
                    cardPayments = storageData.cardPayments,
                    onUpdateCards = { newCards ->
                        updateAndSaveData(storageData.copy(cards = newCards))
                    },
                    onRegisterInvoicePayment = ::registerInvoicePayment,
                    modifier = screenModifier
                )
                Screen.Purchases -> PurchasesScreen(
                    purchases = storageData.purchases,
                    cards = storageData.cards,
                    cardPayments = storageData.cardPayments,
                    dailyExpenses = storageData.dailyExpenses,
                    onUpdatePurchases = { newPurchases ->
                        updateAndSaveData(storageData.copy(purchases = newPurchases))
                    },
                    onUpdateDailyExpenses = { newExpenses ->
                        updateAndSaveData(storageData.copy(dailyExpenses = newExpenses))
                    },
                    modifier = screenModifier
                )
                Screen.Subscriptions -> SubscriptionsScreen(
                    subscriptions = storageData.subscriptions,
                    cards = storageData.cards,
                    onUpdateSubscriptions = { newSubs ->
                        updateAndSaveData(storageData.copy(subscriptions = newSubs))
                    },
                    modifier = screenModifier
                )
                Screen.Settings -> SettingsScreen(
                    googleAccount = storageData.googleAccount,
                    onConnectClick = {
                        googleSignInLauncher.launch(googleAuthManager.getSignInIntent())
                    },
                    onDisconnectClick = {
                        googleAuthManager.signOut { _ ->
                            googleAccountRepository.clearGoogleAccount { _, _ ->
                                storageData = storageData.copy(googleAccount = null, emailSyncState = null)
                                statusMessage = "Conta Google desconectada com sucesso."
                                isErrorStatus = false
                            }
                        }
                    },
                    onTestGmailClick = {
                        isTestingGmail = true
                        coroutineScope.launch {
                            val syncResult = gmailRepository.syncAndImportMercadoPagoExpenses(
                                currentData = storageData,
                                saveStorageData = { newStorageData ->
                                    storageManager.saveData(newStorageData)
                                }
                            )
                            if (syncResult.updatedStorageData != null) {
                                storageData = syncResult.updatedStorageData
                            } else {
                                val reloadedAccount = googleAccountRepository.getGoogleAccount()
                                storageData = storageData.copy(
                                    googleAccount = reloadedAccount,
                                    emailSyncState = reloadedAccount?.emailSyncState
                                )
                            }
                            statusMessage = syncResult.statusMessage
                            isErrorStatus = !syncResult.isSuccess
                            isTestingGmail = false
                        }
                    },
                    isTestingGmail = isTestingGmail,
                    statusMessage = statusMessage,
                    isErrorStatus = isErrorStatus,
                    modifier = screenModifier
                )
            }
        }
    }
}
