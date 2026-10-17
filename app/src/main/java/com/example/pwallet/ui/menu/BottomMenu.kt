package com.example.pwallet.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.pwallet.ui.theme.Cream
import com.example.pwallet.ui.theme.Forest
import com.example.pwallet.ui.theme.Ink
import com.example.pwallet.ui.theme.Mint
import com.example.pwallet.ui.theme.Teal

enum class MainTab { HOME, AGENDA }

@Composable
fun BottomMenu(
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(Forest)
            .border(1.dp, Teal, CircleShape)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MenuButton(Icons.Filled.Home, "Inicio", selected = selected == MainTab.HOME) { onSelect(MainTab.HOME) }
        MenuButton(Icons.Filled.Add, "Nuevo cliente", isAction = true) { onAdd() }
        MenuButton(Icons.Filled.DateRange, "Agenda", selected = selected == MainTab.AGENDA) { onSelect(MainTab.AGENDA) }
    }
}

@Composable
private fun MenuButton(
    icon: ImageVector,
    description: String,
    selected: Boolean = false,
    isAction: Boolean = false,
    onClick: () -> Unit,
) {
    val background = when {
        isAction -> Mint
        selected -> Cream
        else -> Color.Transparent
    }
    val tint = if (isAction || selected) Ink else Mint
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = tint)
    }
}