package com.example.pwallet.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.pwallet.domain.model.User
import com.example.pwallet.ui.client.CreateClientScreen
import com.example.pwallet.ui.client.CreateClientViewModel
import com.example.pwallet.ui.home.HomeScreen
import com.example.pwallet.ui.menu.BottomMenu
import com.example.pwallet.ui.menu.MainTab

private enum class Route { HOME, AGENDA, CREATE }

@Composable
fun AppRoot(
    user: User,
    createClientViewModel: CreateClientViewModel,
    onLogout: () -> Unit,
) {
    var route by rememberSaveable { mutableStateOf(Route.HOME) }
    val context = LocalContext.current
    val createState by createClientViewModel.state.collectAsState()

    BackHandler(enabled = route != Route.HOME) { route = Route.HOME }

    Box(Modifier.fillMaxSize()) {
        when (route) {
            Route.HOME -> HomeScreen(user, onLogout)
            Route.AGENDA -> AgendaPlaceholder()
            Route.CREATE -> CreateClientScreen(
                state = createState,
                currency = user.currency,
                onChange = createClientViewModel::onChange,
                onSave = {
                    createClientViewModel.save {
                        Toast.makeText(context, "Cliente guardado", Toast.LENGTH_SHORT).show()
                        route = Route.AGENDA
                    }
                },
                onBack = { route = Route.HOME },
            )
        }

        if (route != Route.CREATE) {
            BottomMenu(
                selected = if (route == Route.AGENDA) MainTab.AGENDA else MainTab.HOME,
                onSelect = { route = if (it == MainTab.AGENDA) Route.AGENDA else Route.HOME },
                onAdd = { route = Route.CREATE },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
            )
        }
    }
}

@Composable
private fun AgendaPlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Agenda: próximo paso", color = MaterialTheme.colorScheme.onBackground)
    }
}