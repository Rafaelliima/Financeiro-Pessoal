package com.example.auth

import android.content.Context
import android.content.Intent
import com.example.data.EmailSyncState
import com.example.data.GoogleAccountData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gerenciador oficial do fluxo Google OAuth 2.0.
 * Utiliza o SDK oficial Play Services Auth configurado com o menor escopo necessário:
 * https://www.googleapis.com/auth/gmail.readonly para leitura de e-mails do Mercado Pago.
 */
class GoogleAuthManager(private val context: Context) {

    companion object {
        val GMAIL_READONLY_SCOPE = Scope("https://www.googleapis.com/auth/gmail.readonly")
    }

    private val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestProfile()
        .requestScopes(GMAIL_READONLY_SCOPE)
        .build()

    private val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)

    /**
     * Retorna se a conta atualmente autenticada possui o escopo Gmail concedido.
     */
    fun hasGmailScope(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return false
        return GoogleSignIn.hasPermissions(account, GMAIL_READONLY_SCOPE)
    }

    /**
     * Retorna a Intent oficial de autenticação para ser lançada pelo ActivityResultLauncher.
     */
    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    /**
     * Processa o resultado retornado pela Intent oficial de login do Google.
     */
    fun handleSignInResult(data: Intent?): Result<GoogleAccountData> {
        if (data == null) {
            return Result.failure(Exception("Login cancelado pelo usuário."))
        }
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        return try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            if (account != null) {
                val scopeGranted = GoogleSignIn.hasPermissions(account, GMAIL_READONLY_SCOPE)
                val formattedDate = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR")).format(Date())
                val initialSyncState = EmailSyncState(
                    scopeGranted = scopeGranted,
                    statusMessage = if (scopeGranted) "Permissão Gmail concedida." else "Permissão Gmail não concedida pelo usuário."
                )
                val accountData = GoogleAccountData(
                    isConnected = true,
                    userEmail = account.email,
                    userName = account.displayName ?: "Usuário Google",
                    photoUrl = account.photoUrl?.toString(),
                    connectedAt = formattedDate,
                    accountId = account.id,
                    idToken = account.idToken,
                    emailSyncState = initialSyncState
                )
                Result.success(accountData)
            } else {
                Result.failure(Exception("Não foi possível obter os dados da conta Google."))
            }
        } catch (e: ApiException) {
            val errorMessage = when (e.statusCode) {
                CommonStatusCodes.CANCELED, 12501 -> "Login cancelado pelo usuário."
                CommonStatusCodes.NETWORK_ERROR, 7 -> "Falha de conexão com os serviços do Google. Verifique a internet."
                CommonStatusCodes.INVALID_ACCOUNT, 5 -> "Conta Google inválida ou não selecionada."
                CommonStatusCodes.SIGN_IN_REQUIRED, 4 -> "Credenciais expiradas ou login necessário."
                else -> "Falha na autenticação Google (Código: ${e.statusCode})."
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retorna os dados da conta se o usuário já estiver conectado no Google Play Services.
     */
    fun getLastSignedInAccount(): GoogleAccountData? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
        val scopeGranted = GoogleSignIn.hasPermissions(account, GMAIL_READONLY_SCOPE)
        val formattedDate = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR")).format(Date())
        return GoogleAccountData(
            isConnected = true,
            userEmail = account.email,
            userName = account.displayName ?: "Usuário Google",
            photoUrl = account.photoUrl?.toString(),
            connectedAt = formattedDate,
            accountId = account.id,
            idToken = account.idToken,
            emailSyncState = EmailSyncState(
                scopeGranted = scopeGranted,
                statusMessage = if (scopeGranted) "Permissão Gmail concedida." else "Permissão Gmail pendente."
            )
        )
    }

    /**
     * Desconecta a conta Google e revoga o acesso da sessão oficial do Google.
     */
    fun signOut(onComplete: (Boolean) -> Unit) {
        googleSignInClient.signOut().addOnCompleteListener { task ->
            googleSignInClient.revokeAccess().addOnCompleteListener {
                onComplete(task.isSuccessful)
            }
        }
    }
}
