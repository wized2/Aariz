package com.endroid.aariz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.endroid.aariz.data.ThemeMode
import com.endroid.aariz.ui.chat.ChatScreen
import com.endroid.aariz.ui.chat.ChatViewModel
import com.endroid.aariz.ui.theme.AarizTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: ChatViewModel = viewModel()
            val themeMode by vm.themeMode.collectAsState()
            val dark = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            AarizTheme(darkTheme = dark) {
                ChatScreen(vm)
            }
        }
    }
}
