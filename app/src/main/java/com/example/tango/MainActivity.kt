package com.example.tango

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.tango.composables.DeprecatedView
import com.example.tango.composables.OnLifecycleEvent
import com.example.tango.composables.TangoGrid
import com.example.tango.composables.Timer
import com.example.tango.ui.theme.TangoTheme
import com.example.tango.viewmodels.TangoActivityViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Angle
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.Spread
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var viewModel: TangoActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = Firebase.auth

        enableEdgeToEdge()
        setContent {
            MainActivityView()
        }
    }


    @Preview(showSystemUi = true)
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
    @Composable
    fun MainActivityView(
    ) {
        viewModel = viewModel()
        val isDeprecated by viewModel.isDeprecated.collectAsStateWithLifecycle()
        val currentUser by viewModel.currentUser.collectAsState()
        val started by viewModel.started.collectAsStateWithLifecycle()
        val loading by viewModel.loading.collectAsStateWithLifecycle()
        val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
        val completed by viewModel.completed.collectAsStateWithLifecycle()
        val grid by viewModel.grid.collectAsState()
        val ticks by viewModel.ticks.collectAsState()
        val config by viewModel.config.collectAsState()

        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        val snackbarHostState = remember { SnackbarHostState() }

        TangoTheme {
            if (isDeprecated) {
                DeprecatedView(config!!)
            } else {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(navigationIcon = {
                            IconButton(onClick = { }) {
                                Icon(
                                    imageVector = Icons.Filled.Menu, contentDescription = "Menu"
                                )
                            }
                        }, title = {
                            Text(
                                text = "Tango",
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Cursive,
                                fontSize = 32.sp
                            )
                        }, actions = {
                            IconButton(onClick = {}) {
                                if (currentUser?.photoUrl != null) {
                                    GlideImage(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(shape = RoundedCornerShape(18.dp)),
                                        model = currentUser!!.photoUrl,
                                        contentDescription = null
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Settings,
                                        contentDescription = "Settings"
                                    )
                                }
                            }
                        })
                    },
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (!loading && started) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Button(modifier = Modifier.width(100.dp), onClick = {}) {
                                        Timer(running = !completed, ticks = ticks) {
                                            viewModel.onTick()
                                        }
                                    }
                                    Button(modifier = Modifier.width(100.dp), onClick = {
                                        viewModel.resetGrid()
                                    }) {
                                        Text(if (completed) "Reset" else "Clear")
                                    }
                                }
                                TangoGrid(grid!!, completed) {
                                    viewModel.onComplete()
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Button(
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            scope.launch {
                                                snackbarHostState.currentSnackbarData?.dismiss()
                                                snackbarHostState.showSnackbar(
                                                    "banega banega",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }) { Text("Undo") }
                                    Spacer(modifier = Modifier.size(24.dp))
                                    Button(modifier = Modifier.weight(1f), onClick = {
                                        scope.launch {
                                            snackbarHostState.currentSnackbarData?.dismiss()
                                            snackbarHostState.showSnackbar(
                                                "Ye to nhi banega",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }) { Text("Stuck?") }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .align(Alignment.BottomStart)
                                    .padding(bottom = 16.dp)
                            ) {
                                ElevatedButton(
                                    modifier = Modifier
                                        .fillMaxWidth(), onClick = {
                                        if (isLoggedIn) {
                                            context.startActivity(
                                                Intent(
                                                    context, LeaderboardActivity::class.java
                                                ).apply {
                                                    putExtra("gridId", viewModel.gridId)
                                                })
                                        } else {
                                            signIn(scope, context)
                                        }
                                    }) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Image(
                                            ImageVector.vectorResource(R.drawable.leaderboard),
                                            contentDescription = "",
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "View Leaderboard  ->",
                                            fontFamily = FontFamily.SansSerif
                                        )
                                    }
                                }
                            }
                        }
                        if (started && completed) {
                            KonfettiView(
                                modifier = Modifier.fillMaxSize(), parties = listOf(
                                    Party(
                                        speed = 15f,
                                        maxSpeed = 25f,
                                        damping = 0.9f,
                                        angle = Angle.BOTTOM,
                                        spread = Spread.ROUND,
                                        emitter = Emitter(
                                            duration = 3, TimeUnit.SECONDS
                                        ).perSecond(100),
                                        position = Position.Relative(0.0, 0.3)
                                            .between(Position.Relative(1.0, 0.3))
                                    )
                                )
                            )
                        }
                        if (loading || !started) {
                            Box(
                                modifier = Modifier.matchParentSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.preview),
                                    contentDescription = "",
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier
                                        .matchParentSize()
                                        .blur(
                                            radiusX = 30.dp,
                                            radiusY = 30.dp,
                                            edgeTreatment = BlurredEdgeTreatment.Rectangle
                                        )
                                )
                                if (loading) {
                                    CircularProgressIndicator()
                                } else {
                                    Button(onClick = {
                                        viewModel.onStart()
                                    }) { Text(if (ticks == 0) "Start" else "Resume") }
                                }
                            }
                        }
                    }
                }
            }

        }

        OnLifecycleEvent { owner, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    println("On Pause")
                    viewModel.saveState()
                }

                else -> {}
            }
        }
    }

    private fun signIn(scope: CoroutineScope, context: Context) {
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
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = firebaseAuth.currentUser
                viewModel.onSignUpCompleted(user!!)
            } else {
                Log.w(TAG, "signInWithCredential:failure", task.exception)
            }
        }
    }


}
