package com.example.tango.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.tango.BuildConfig
import com.example.tango.TAG
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class GoogleSignInUtils(
    val scope: CoroutineScope,
    val context: Context,
    val activity: Activity,
    val onResult: (FirebaseUser?) -> Unit
) {
    fun signIn() {
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(BuildConfig.SERVER_CLIENT_ID)
            .setFilterByAuthorizedAccounts(false).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
        scope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = context, request = request
                )
                handleSignIn(result.credential)
            } catch (e: GetCredentialException) {
                onResult(null)
                Log.e(TAG, "Couldn't retrieve user's credentials: ${e.localizedMessage}")
            }
        }
    }

    private fun handleSignIn(credential: Credential) {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            Log.w(TAG, "Credential is not of type Google ID!")
            onResult(null)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val firebaseAuth = Firebase.auth
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(activity) { task ->
            if (task.isSuccessful) {
                val user = firebaseAuth.currentUser
                onResult(user)
            } else {
                onResult(null)
                Log.w(TAG, "signInWithCredential:failure", task.exception)
            }
        }
    }
}
