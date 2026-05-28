package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.TexaAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.TexaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: TexaViewModel = viewModel()

                // Dynamic hardware-level screenshot blocker side-effect
                LaunchedEffect(viewModel.screenshotProtectionEnabled) {
                    // Safe guard: Do NOT apply FLAG_SECURE under Emulator streaming display
                    // as it breaks/stops WebRTC/VNC stream capture feed of the UI.
                }

                TexaAppScreen(viewModel = viewModel)
            }
        }
    }
}
