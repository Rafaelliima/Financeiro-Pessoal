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

        // CÓDIGO PARA PEGAR O NOVO SHA-1 DE PRODUÇÃO (RELEASE)
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
                Log.d("FIREBASE_SHA1", "SEU NOVO SHA-1 DE PRODUÇÃO É: $sha1")
            }
        } catch (e: Exception) {
            Log.e("FIREBASE_SHA1", "Erro ao obter SHA-1", e)
        }

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
    val updateManager = remember { UpdateManager(context) }
    val firebaseManager = remember { FirebaseManager() }
    val googleAuthManager = remember(context) { GoogleAuthManager(context) }
    val googleAccountRepository = remember(storageManager) { GoogleAccountRepository(storageManager) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentScreenRoute by rememberSaveable { mutableStateOf(Screen.Dashboard.route) }
    var isLoading by remember { mutableStateOf(true) }
    var storageData by remember { mutableStateOf(StorageData()) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isErrorStatus by remember { mutableStateOf(false) }
    var isAuthenticating by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<AppVersionInfo?>(null) }

    // Launcher oficial do Google Sign-In
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
                        
                        // Sincronização inicial pós-login
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
            text = { 
                Text("Uma nova versão (${updateInfo?.latestVersionName}) está disponível. Deseja baixar e instalar?")
            },
            confirmButton = {
                Button(onClick = {
                    updateInfo?.let { updateManager.downloadAndInstallApk(it.apkUrl) }
                    updateInfo = null
                }) {
                    Text("Atualizar agora")
                }
            },
            dismissButton = {
                TextButton(onClick = { updateInfo = null }) {
                    Text("Depois", color = TextSecondary)
                }
            }
        )
    }

    // Carrega os dados (Local + Nuvem se conectado) ao abrir o app
    LaunchedEffect(Unit) {
        // 1. Tenta recuperar sessão existente no Google Play Services
        val lastAccount = googleAuthManager.getLastSignedInAccount()
        if (lastAccount != null) {
            storageData = storageData.copy(googleAccount = lastAccount)
        }

        // 2. Carrega dados locais
        val loadResult = storageManager.loadData()
        storageData = storageData.copy(
            cards = loadResult.data?.cards ?: storageData.cards,
            purchases = loadResult.data?.purchases ?: storageData.purchases,
            subscriptions = loadResult.data?.subscriptions ?: storageData.subscriptions,
            dailyExpenses = loadResult.data?.dailyExpenses ?: storageData.dailyExpenses
        )

        // 3. Se logado, sincroniza com Firebase
        val userId = lastAccount?.accountId
        if (lastAccount != null && !userId.isNullOrBlank()) {
            val cloudData = firebaseManager.loadDataFromCloud(userId)
            if (cloudData != null) {
                storageData = cloudData.copy(googleAccount = lastAccount)
                storageManager.saveData(storageData)
            }
        }
        
        // 4. Verifica atualizações em segundo plano
        updateInfo = updateManager.checkForUpdate()

        isLoading = false
    }

    // Função central para persistir alterações no JSON, Firestore e exibir feedback
    fun updateAndSaveData(newStorageData: StorageData) {
        storageData = newStorageData
        
        // Salva Localmente
        val saveResult = storageManager.saveData(newStorageData)
        statusMessage = saveResult.message
        isErrorStatus = !saveResult.isSuccess
        
        coroutineScope.launch {
            // Salva na Nuvem se estiver conectado
            val account = storageData.googleAccount ?: googleAccountRepository.getGoogleAccount()
            val userId = account?.accountId
            if (account?.isConnected == true && !userId.isNullOrBlank()) {
                val cloudSuccess = firebaseManager.syncDataToCloud(userId, newStorageData)
                if (!cloudSuccess) {
                    statusMessage = "Erro ao sincronizar com a nuvem (Salvo localmente)"
                }
            }

            snackbarHostState.showSnackbar(statusMessage ?: saveResult.message)
            delay(3000)
            if (statusMessage == saveResult.message || statusMessage?.contains("nuvem") == true) {
                statusMessage = null
            }
        }
    }

    fun registerInvoicePayment(card: CardItem) {
        val currentCal = Calendar.getInstance()
        val currentMonth = currentCal.get(Calendar.MONTH) + 1
        val currentYear = currentCal.get(Calendar.YEAR)

        // A contagem de parcelas de cada compra agora é 100% automática pelo
        // calendário (ver PurchaseItem.calculateInstallments) — não depende mais
        // de "Confirmar pagamento" ser clicado todo mês. Este botão serve apenas
        // para registrar no histórico do cartão que a fatura daquele mês foi paga.
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

        updateAndSaveData(
            storageData.copy(cardPayments = updatedCardPayments)
        )
    }

    val currentScreen = Screen.items.find { it.route == currentScreenRoute } ?: Screen.Dashboard

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    modifier = Modifier.testTag("loading_indicator"),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Carregando...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    } else if (!isUserLoggedIn) {
        LoginScreen(
            onLoginClick = {
                googleSignInLauncher.launch(googleAuthManager.getSignInIntent())
            },
            isLoading = isAuthenticating,
            errorMessage = if (isErrorStatus) statusMessage else null
        )
    } else {
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
                                storageData = storageData.copy(googleAccount = null)
                                statusMessage = "Conta Google desconectada com sucesso."
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
