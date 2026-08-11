package com.example.auth

import android.content.Context
import android.content.Intent
import com.example.data.GoogleAccountData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gerenciador oficial do fluxo Google OAuth 2.0.
 * Utiliza o SDK oficial Play Services Auth apenas para autenticação (login) e
 * para habilitar o backup/sincronização dos dados na nuvem via Firebase Firestore.
 */
class GoogleAuthManager(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestProfile()
        .requestIdToken(context.getString(com.example.R.string.default_web_client_id))
        .build()

    private val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)

    /**
     * Retorna a Intent oficial de autenticação para ser lançada pelo ActivityResultLauncher.
     */
    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    /**
     * Processa o resultado retornado pela Intent oficial de login do Google
     * e realiza a autenticação no Firebase.
     */
    suspend fun handleSignInResult(data: Intent?): Result<GoogleAccountData> {
        if (data == null) {
            return Result.failure(Exception("Login cancelado pelo usuário."))
        }
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        return try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            if (account != null) {
                // 1. Autentica no Firebase usando o ID Token do Google
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential).await()

                // 2. Coleta dados para o repositório local
                val formattedDate = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR")).format(Date())
                val accountData = GoogleAccountData(
                    isConnected = true,
                    userEmail = account.email,
                    userName = account.displayName ?: "Usuário Google",
                    photoUrl = account.photoUrl?.toString(),
                    connectedAt = formattedDate,
                    accountId = auth.currentUser?.uid ?: account.id,
                    idToken = account.idToken
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
        val formattedDate = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR")).format(Date())
        return GoogleAccountData(
            isConnected = true,
            userEmail = account.email,
            userName = account.displayName ?: "Usuário Google",
            photoUrl = account.photoUrl?.toString(),
            connectedAt = formattedDate,
            accountId = auth.currentUser?.uid ?: account.id,
            idToken = account.idToken
        )
    }

    /**
     * Desconecta a conta Google e Firebase, revogando o acesso da sessão oficial.
     */
    fun signOut(onComplete: (Boolean) -> Unit) {
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener { task ->
            googleSignInClient.revokeAccess().addOnCompleteListener {
                onComplete(task.isSuccessful)
            }
        }
    }
}
