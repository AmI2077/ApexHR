package com.example.apexhr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.apexhr.core.ui.navigation.AppNavigation
import com.example.apexhr.core.ui.navigation.BottomNavBar
import com.example.apexhr.core.ui.navigation.TopLevelRoute
import com.example.apexhr.core.ui.navigation.rememberNavigator
import com.example.apexhr.ui.theme.ApexHRTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ApexHRTheme {
                val navigator = rememberNavigator(TopLevelRoute.Home)

                Scaffold(
                    bottomBar = {
                        BottomNavBar(
                            selectedRoute = navigator.getCurrentTopLevelRoute(),
                            onClick = { route ->
                                navigator.navigate(route)
                            }
                        )
                    }
                ) { innerPadding ->
                    AppNavigation(
                        modifier = Modifier
                            .padding(innerPadding),
                        navigator = navigator
                    )
                }
            }
        }
    }
}
