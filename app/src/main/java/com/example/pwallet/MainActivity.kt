package com.example.pwallet

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pwallet.ui.AppRoot
import com.example.pwallet.ui.auth.AuthMode
import com.example.pwallet.ui.auth.AuthViewModel
import com.example.pwallet.ui.auth.AuthViewModelFactory
import com.example.pwallet.ui.auth.LoginScreen
import com.example.pwallet.ui.auth.RegisterScreen
import com.example.pwallet.ui.auth.RecoverPinScreen
import com.example.pwallet.ui.client.CreateClientViewModel
import com.example.pwallet.ui.theme.PWalletTheme

class MainActivity : ComponentActivity() {

    private val container by lazy { AppContainer.get(applicationContext) }

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(container)
    }

    private val createClientViewModel: CreateClientViewModel by viewModels {
        viewModelFactory {
            initializer { CreateClientViewModel(container.createClientWithLoan, container.session) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            PWalletTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val state by authViewModel.state.collectAsState()
                    val user = state.sessionUser
                    when {
                        user != null -> AppRoot(user, createClientViewModel, onLogout = authViewModel::logout)

                        state.mode == AuthMode.LOGIN ->
                            LoginScreen(
                                state,
                                authViewModel::onLoginChange,
                                authViewModel::submitLogin,
                                authViewModel::startRecovery
                            )

                        state.mode == AuthMode.REGISTER ->
                            RegisterScreen(
                                state,
                                authViewModel::onRegisterChange,
                                authViewModel::submitRegister
                            )

                        state.mode == AuthMode.RECOVER ->
                            RecoverPinScreen(
                                state,
                                authViewModel::onRecoverChange,
                                authViewModel::verifyAnswer,
                                authViewModel::submitNewPin,
                                authViewModel::cancelRecovery
                            )

                        else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}