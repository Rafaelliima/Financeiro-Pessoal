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
import com.example.data.GoogleAccountData
import com.example.data.CardItem
import com.example.data.DailyExpense
import com.example.data.ReminderItem
import com.example.data.ReminderSyncUtils
import com.example.data.PurchaseItem
import com.example.data.SubscriptionItem
import com.example.data.UpdateManager
import kotlinx.coroutines.launch
import com.example.data.JsonStorageManager
import com.example.data.StorageData
import com.example.data.DataSyncUtils
import com.example.navigation.Screen
import com.example.ui.components.AppBottomNavigation
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PurchasesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.FinanceiroPessoalTheme
import com.example.ui.theme.VeroThemePalette
import com.example.ui.theme.TextSecondary

import com.example.data.CardPaymentItem
import com.example.data.PaymentMethod
import com.example.notification.ReminderScheduler
import android.Manifest
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.util.Log
import java.security.MessageDigest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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

    FinanceiroPessoalTheme(
        themeMode = storageData.themeMode,
        palette = VeroThemePalette.fromId(storageData.selectedPalette)
    ) {
        var currentScreenRoute by rememberSaveable { mutableStateOf(Screen.Dashboard.route) }
        var purchasesInitialTab by remember { mutableStateOf(0) }
        var isLoading by remember { mutableStateOf(true) }
        var statusMessage by remember { mutableStateOf<String?>(null) }
        var isErrorStatus by remember { mutableStateOf(false) }
        var isAuthenticating by remember { mutableStateOf(false) }
        var updateInfo by remember { mutableStateOf<AppVersionInfo?>(null) }

        val notificationPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { /* permissão tratada */ }

        LaunchedEffect(Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        val googleSignInLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            isAuthenticating = true
            coroutineScope.launch {
                val handleResult = googleAuthManager.handleSignInResult(result.data)
                handleResult.fold(
                    onSuccess = { newAccount ->
                        val currentAcc = storageData.googleAccount
                        val effectiveAccount = newAccount.copy(
                            customDisplayName = currentAcc?.customDisplayName ?: newAccount.customDisplayName,
                            customPhotoPath = currentAcc?.customPhotoPath ?: newAccount.customPhotoPath
                        )
                        googleAccountRepository.saveGoogleAccount(effectiveAccount) { isSuccess, saveMessage ->
                            storageData = storageData.copy(googleAccount = effectiveAccount)
                            statusMessage = "Conta conectada. Sincronizando..."
                            coroutineScope.launch {
                                val userId = effectiveAccount.accountId
                                if (!userId.isNullOrBlank()) {
                                    val cloudData = firebaseManager.loadDataFromCloud(userId)
                                    if (cloudData != null) {
                                        val (mergedData, shouldUpload) = DataSyncUtils.safeMergeStorageData(
                                            localData = storageData,
                                            cloudData = cloudData,
                                            account = effectiveAccount
                                        )
                                        storageData = mergedData
                                        storageManager.saveData(mergedData)
                                        if (shouldUpload) {
                                            firebaseManager.syncDataToCloud(userId, mergedData)
                                        }
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
            val loadResult = storageManager.loadData()
            val localData = loadResult.data
            val localAccount = localData?.googleAccount

            val lastAccount = googleAuthManager.getLastSignedInAccount()
            val effectiveAccount = if (lastAccount != null) {
                lastAccount.copy(
                    customDisplayName = localAccount?.customDisplayName ?: lastAccount.customDisplayName,
                    customPhotoPath = localAccount?.customPhotoPath ?: lastAccount.customPhotoPath
                )
            } else {
                localAccount
            }

            storageData = storageData.copy(
                cards = localData?.cards ?: storageData.cards,
                purchases = localData?.purchases ?: storageData.purchases,
                subscriptions = localData?.subscriptions ?: storageData.subscriptions,
                cardPayments = localData?.cardPayments ?: storageData.cardPayments,
                dailyExpenses = localData?.dailyExpenses ?: storageData.dailyExpenses,
                reminders = localData?.reminders ?: storageData.reminders,
                themeMode = localData?.themeMode ?: storageData.themeMode,
                selectedPalette = localData?.selectedPalette ?: storageData.selectedPalette,
                googleAccount = effectiveAccount
            )

            val userId = effectiveAccount?.accountId
            if (effectiveAccount != null && !userId.isNullOrBlank()) {
                val cloudData = firebaseManager.loadDataFromCloud(userId)
                if (cloudData != null) {
                    val (mergedData, shouldUpload) = DataSyncUtils.safeMergeStorageData(
                        localData = storageData,
                        cloudData = cloudData,
                        account = effectiveAccount
                    )
                    storageData = mergedData
                    storageManager.saveData(mergedData)
                    if (shouldUpload) {
                        firebaseManager.syncDataToCloud(userId, mergedData)
                    }
                } else {
                    firebaseManager.syncDataToCloud(userId, storageData)
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
            
            ReminderScheduler.cancelReminder(context, reminder.id)
            val updatedReminder = reminder.copy(isPaid = true)
            val pMethod = when (reminder.paymentMethod?.uppercase()) {
                "DINHEIRO", "ESPÉCIE", "ESPECIE" -> PaymentMethod.ESPECIE
                else -> PaymentMethod.CONTA
            }
            val recurrenceLabel = if (reminder.isRecurring) {
                " (${reminder.currentOccurrence}/${if (reminder.totalOccurrences > 0) reminder.totalOccurrences else "∞"})"
            } else ""
            val newExpense = DailyExpense(
                name = reminder.name,
                value = reminder.value,
                date = reminder.date,
                paymentMethod = pMethod,
                observation = "Pago via lembrete$recurrenceLabel",
                reminderId = reminder.id
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

        fun handleDeleteDailyExpense(expense: DailyExpense) {
            val updatedExpenses = storageData.dailyExpenses.filter { it.id != expense.id }
            val updatedReminders = if (!expense.reminderId.isNullOrBlank()) {
                storageData.reminders.map { reminder ->
                    if (reminder.id == expense.reminderId) {
                        val reopened = reminder.copy(isPaid = false)
                        ReminderScheduler.scheduleReminder(context, reopened)
                        reopened
                    } else {
                        reminder
                    }
                }
            } else {
                storageData.reminders
            }
            updateAndSaveData(storageData.copy(
                dailyExpenses = updatedExpenses,
                reminders = updatedReminders
            ))
        }

        fun registerInvoicePayment(card: CardItem, targetMonth: Int, targetYear: Int) {
            val currentCal = Calendar.getInstance()
            val newPayment = CardPaymentItem(
                cardId = card.id,
                month = targetMonth,
                year = targetYear,
                paid = true,
                paidAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(currentCal.time)
            )
            val updatedCardPayments = storageData.cardPayments.filterNot {
                it.cardId == card.id && it.month == targetMonth && it.year == targetYear
            } + newPayment
            val updatedPurchases = storageData.purchases.map { purchase ->
                val isSameCard = purchase.cardId == card.id || (purchase.cardId.isBlank() && purchase.cardName == card.name)
                if (isSameCard) {
                    val calc = purchase.calculateInstallments(targetMonth, targetYear)
                    if (purchase.isInstallment && calc.status == "Em andamento") {
                        val newPaidCount = calc.currentInstallment
                        val isNowQuitada = newPaidCount >= purchase.totalInstallments
                        val completedAt = if (isNowQuitada && purchase.completedAt == null) {
                            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(currentCal.time)
                        } else purchase.completedAt
                        purchase.copy(
                            paidInstallmentsCount = newPaidCount,
                            isQuitada = isNowQuitada || purchase.isQuitada,
                            completedAt = completedAt
                        )
                    } else if (!purchase.isInstallment && !purchase.isQuitada) {
                        val isCurrentOrPastMonth = (purchase.startYear < targetYear) ||
                            (purchase.startYear == targetYear && purchase.startMonth <= targetMonth)
                        if (isCurrentOrPastMonth) {
                            purchase.copy(
                                paidInstallmentsCount = 1,
                                isQuitada = true,
                                completedAt = purchase.completedAt ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(currentCal.time)
                            )
                        } else purchase
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
                val screenModifier = Modifier.padding(innerPadding)
                when (currentScreen) {
                    Screen.Dashboard -> DashboardScreen(
                        cards = storageData.cards,
                        purchases = storageData.purchases,
                        subscriptions = storageData.subscriptions,
                        dailyExpenses = storageData.dailyExpenses,
                        reminders = storageData.reminders,
                        googleAccount = storageData.googleAccount,
                        cardPayments = storageData.cardPayments,
                        onUpdateReminders = { newReminders ->
                            val deletedIds = storageData.reminders.map { it.id }.toSet() - newReminders.map { it.id }.toSet()
                            deletedIds.forEach { ReminderScheduler.cancelReminder(context, it) }
                            newReminders.filter { !it.isPaid }.forEach { rem ->
                                ReminderScheduler.scheduleReminder(context, rem)
                            }
                            val updatedExpenses = if (deletedIds.isNotEmpty()) {
                                storageData.dailyExpenses.filterNot { it.reminderId != null && deletedIds.contains(it.reminderId) }
                            } else {
                                storageData.dailyExpenses
                            }
                            updateAndSaveData(storageData.copy(
                                reminders = newReminders,
                                dailyExpenses = updatedExpenses
                            ))
                        },
                        onPayReminder = ::handlePayReminder,
                        onAddPurchase = { newPurchase -> updateAndSaveData(storageData.copy(purchases = storageData.purchases + newPurchase)) },
                        onAddDailyExpense = { newExpense -> updateAndSaveData(storageData.copy(dailyExpenses = storageData.dailyExpenses + newExpense)) },
                        onAddSubscription = { newSub -> updateAndSaveData(storageData.copy(subscriptions = storageData.subscriptions + newSub)) },
                        onNavigateToPurchases = { tabIndex ->
                            purchasesInitialTab = tabIndex
                            currentScreenRoute = Screen.Purchases.route
                        },
                        modifier = screenModifier
                    )
                    Screen.Purchases -> PurchasesScreen(
                        purchases = storageData.purchases,
                        cards = storageData.cards,
                        cardPayments = storageData.cardPayments,
                        dailyExpenses = storageData.dailyExpenses,
                        subscriptions = storageData.subscriptions,
                        onUpdatePurchases = { newPurchases -> updateAndSaveData(storageData.copy(purchases = newPurchases)) },
                        onUpdateDailyExpenses = { newExpenses ->
                            val deletedExpenses = storageData.dailyExpenses.filter { old -> newExpenses.none { it.id == old.id } }
                            val reopenedReminderIds = deletedExpenses.mapNotNull { it.reminderId }.toSet()
                            val updatedReminders = if (reopenedReminderIds.isNotEmpty()) {
                                storageData.reminders.map { reminder ->
                                    if (reopenedReminderIds.contains(reminder.id)) {
                                        val reopened = reminder.copy(isPaid = false)
                                        ReminderScheduler.scheduleReminder(context, reopened)
                                        reopened
                                    } else {
                                        reminder
                                    }
                                }
                            } else {
                                storageData.reminders
                            }
                            updateAndSaveData(storageData.copy(
                                dailyExpenses = newExpenses,
                                reminders = updatedReminders
                            ))
                        },
                        onDeleteDailyExpense = ::handleDeleteDailyExpense,
                        onUpdateSubscriptions = { newSubs -> updateAndSaveData(storageData.copy(subscriptions = newSubs)) },
                        onUpdateCards = { newCards -> updateAndSaveData(storageData.copy(cards = newCards)) },
                        onRegisterInvoicePayment = ::registerInvoicePayment,
                        initialTabIndex = purchasesInitialTab,
                        modifier = screenModifier
                    )
                    Screen.Settings -> SettingsScreen(
                        googleAccount = storageData.googleAccount,
                        themeMode = storageData.themeMode,
                        onThemeModeChange = { newMode -> updateAndSaveData(storageData.copy(themeMode = newMode)) },
                        selectedPalette = storageData.selectedPalette,
                        onPaletteChange = { newPalette -> updateAndSaveData(storageData.copy(selectedPalette = newPalette)) },
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
                        onUpdateProfile = { customName, customPhotoPath ->
                            val currentAccount = storageData.googleAccount ?: GoogleAccountData()
                            val updatedAccount = currentAccount.copy(
                                customDisplayName = customName,
                                customPhotoPath = customPhotoPath ?: currentAccount.customPhotoPath
                            )
                            updateAndSaveData(storageData.copy(googleAccount = updatedAccount))
                        },
                        onSendFeedback = { type, message, contactEmail ->
                            val email = contactEmail ?: storageData.googleAccount?.userEmail
                            val name = storageData.googleAccount?.customDisplayName ?: storageData.googleAccount?.userName
                            val appVersion = try {
                                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "2.5"
                            } catch (e: Exception) {
                                "2.5"
                            }
                            val success = firebaseManager.sendFeedback(
                                type = type,
                                message = message,
                                userEmail = email,
                                userName = name,
                                appVersion = appVersion
                            )
                            if (success) {
                                statusMessage = "Feedback enviado com sucesso! Obrigado pela colaboração."
                                isErrorStatus = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Feedback enviado com sucesso!")
                                }
                            } else {
                                statusMessage = "Erro ao enviar feedback. Verifique sua conexão e tente novamente."
                                isErrorStatus = true
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Erro ao enviar feedback.")
                                }
                            }
                            success
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
