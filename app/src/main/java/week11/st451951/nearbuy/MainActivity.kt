package week11.st451951.nearbuy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import week11.st451951.nearbuy.auth.AuthViewModel
import week11.st451951.nearbuy.navigation.MainScaffold
import week11.st451951.nearbuy.navigation.NearBuyNavGraph
import week11.st451951.nearbuy.navigation.Screen
import week11.st451951.nearbuy.ui.theme.NearBuyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NearBuyTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Determine starting destination based on auth state
                val startDestination = if (authViewModel.isLoggedIn) {
                    Screen.Buy.route
                } else {
                    Screen.Auth.route
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    if (currentRoute == Screen.Auth.route || currentRoute == null) {
                        // Full screen login without scaffold
                        NearBuyNavGraph(
                            navController = navController,
                            startDestination = startDestination,
                            authViewModel = authViewModel
                        )
                    } else {
                        MainScaffold(
                            navController = navController,
                            authViewModel = authViewModel
                        ) { modifier ->
                            NearBuyNavGraph(
                                navController = navController,
                                startDestination = startDestination,
                                authViewModel = authViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
