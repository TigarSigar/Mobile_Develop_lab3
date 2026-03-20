package com.example.todotime

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todotime.auth.AuthManager
import com.example.todotime.ui.TodoScreen
import com.example.todotime.ui.TodoViewModel
import com.example.todotime.ui.TodoViewModelFactory
import com.example.todotime.ui.theme.ToDoTimeTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        authManager = AuthManager(this)
        super.onCreate(savedInstanceState)

        setContent {
            ToDoTimeTheme {
                val scope = rememberCoroutineScope()
                var isUserLoggedIn by remember { mutableStateOf(authManager.currentUser != null) }
                var isGuestMode by remember { mutableStateOf(false) }

                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    try {
                        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        val account = task.getResult(ApiException::class.java)
                        account?.idToken?.let { token ->
                            scope.launch {
                                val success = authManager.signInWithGoogle(token)
                                if (success) {
                                    isUserLoggedIn = true
                                } else {
                                    Toast.makeText(this@MainActivity, "Firebase Error", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("AUTH_DEBUG", "Google sign-in error: ${e.message}")
                        Toast.makeText(this@MainActivity, "Ошибка входа: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }

                if (isUserLoggedIn || isGuestMode) {
                    val app = application as TodoApplication
                    val viewModel: TodoViewModel = viewModel(
                        factory = TodoViewModelFactory(app.repository)
                    )

                    TodoScreen(
                        viewModel = viewModel,
                        onAccountClick = {
                            authManager.signOut()
                            isUserLoggedIn = false
                            isGuestMode = false
                        }
                    )
                } else {
                    LoginScreen(
                        onGoogleClick = { launcher.launch(authManager.getSignInIntent()) },
                        onGuestClick = { isGuestMode = true }
                    )
                }
            }
        }
    }

    @Composable
    private fun LoginScreen(onGoogleClick: () -> Unit, onGuestClick: () -> Unit) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = onGoogleClick) {
                Text("Войти через Google")
            }
            TextButton(onClick = onGuestClick) {
                Text("Зайти без аккаунта", color = Color.Gray)
            }
        }
    }
}
