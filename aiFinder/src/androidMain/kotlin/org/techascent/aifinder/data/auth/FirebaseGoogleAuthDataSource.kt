package org.techascent.aifinder.data.auth

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import org.techascent.aifinder.R
import org.techascent.aifinder.domain.auth.AuthError
import org.techascent.aifinder.domain.auth.AuthException
import org.techascent.aifinder.domain.auth.AuthUser

class FirebaseGoogleAuthDataSource(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val activityProvider: () -> android.app.Activity? = { CurrentActivityHolder.activity },
) : GoogleAuthDataSource {

    override val currentUser: AuthUser? get() = auth.currentUser?.toAuthUser()

    override suspend fun signIn(): AuthUser {
        val activity = activityProvider() ?: throw AuthException(AuthError.Unavailable)
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(activity.getString(R.string.default_web_client_id))
                    .setAutoSelectEnabled(true)
                    .build()
            )
            .build()

        val credential = try {
            CredentialManager.create(activity).getCredential(activity, request).credential
        } catch (e: GetCredentialCancellationException) {
            throw AuthException(AuthError.Cancelled, e)
        } catch (e: NoCredentialException) {
            throw AuthException(AuthError.NoAccount, e)
        } catch (e: GetCredentialException) {
            throw AuthException(AuthError.Unknown(e.message), e)
        }
        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            throw AuthException(AuthError.Unknown("Unexpected credential type: ${credential.type}"))
        }

        val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val user = try {
            auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await().user
        } catch (e: FirebaseNetworkException) {
            throw AuthException(AuthError.Network, e)
        }
        return user?.toAuthUser() ?: throw AuthException(AuthError.Unknown("Firebase returned no user"))
    }

    override suspend fun signOut() {
        auth.signOut()
        activityProvider()?.let {
            CredentialManager.create(it).clearCredentialState(ClearCredentialStateRequest())
        }
    }

    private fun FirebaseUser.toAuthUser() = AuthUser(
        id = uid,
        displayName = displayName,
        email = email,
        photoUrl = photoUrl?.toString(),
    )
}
