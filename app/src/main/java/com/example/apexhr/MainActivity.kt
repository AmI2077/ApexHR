package com.example.apexhr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.apexhr.core.ui.navigation.AppRoot
import com.example.apexhr.features.auth.ui.LoginViewModel
import com.example.apexhr.ui.theme.ApexHRTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ApexHRTheme {
                AppRoot(
                    viewModel = koinViewModel<LoginViewModel>()
                )
            }
        }
    }
}
