package week11.st451951.nearbuy

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import week11.st451951.nearbuy.auth.AuthEvent
import week11.st451951.nearbuy.auth.AuthViewModel
import week11.st451951.nearbuy.navigation.MainScaffold
import week11.st451951.nearbuy.navigation.NearBuyNavGraph
import week11.st451951.nearbuy.navigation.Screen
import week11.st451951.nearbuy.ui.components.LocalDrawerState
import week11.st451951.nearbuy.ui.components.ProfileDrawer
import week11.st451951.nearbuy.ui.theme.NearBuyTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import week11.st451951.nearbuy.navigation.ScaffoldState

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
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val context = LocalContext.current

                val mainScaffoldState = remember { mutableStateOf(ScaffoldState()) }
                // ###################
                // AUTH EVENT OBSERVER
                // ###################
                LaunchedEffect(Unit) {
                    authViewModel.authEvents.collect { event ->
                        when (event) {
                            is AuthEvent.ShowToast -> {
                                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                            }
                            is AuthEvent.NavigateToLogin -> {
                                navController.navigate(Screen.Auth.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                            is AuthEvent.NavigateToMain -> {
                                navController.navigate(Screen.Buy.route) {
                                    popUpTo(Screen.Auth.route) { inclusive = true }
                                }
                            }
                            is AuthEvent.RegistrationSuccess -> {
                                // Handle registration success if needed
                            }
                        }
                    }
                }

                // Open app to correct destination based on auth state
                val startDestination = if (authViewModel.isLoggedIn) {
                    Screen.Buy.route
                } else {
                    Screen.Auth.route
                }

                // Only show drawer on main app screens (not auth)
                val showDrawer = currentRoute != Screen.Auth.route && currentRoute != null

                Surface(modifier = Modifier.fillMaxSize()) {
                    if (showDrawer) {
                        CompositionLocalProvider(LocalDrawerState provides drawerState) {
                            ModalNavigationDrawer(
                                drawerState = drawerState,
                                drawerContent = {
                                    ProfileDrawer(
                                        authViewModel = authViewModel,
                                        onLogout = {
                                            scope.launch {
                                                drawerState.close()
                                                authViewModel.signOut()
                                            }
                                        }
                                    )
                                }
                            ) {
                                MainScaffold(
                                    navController = navController,
                                    scaffoldState = mainScaffoldState.value
                                ) { modifier ->
                                    NearBuyNavGraph(
                                        navController = navController,
                                        startDestination = startDestination,
                                        authViewModel = authViewModel,
                                        onScaffoldStateChange = { newState ->
                                            mainScaffoldState.value = newState
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        NearBuyNavGraph(
                            navController = navController,
                            startDestination = startDestination,
                            authViewModel = authViewModel,
                            onScaffoldStateChange = { newState ->
                                mainScaffoldState.value = newState
                            }
                        )
                    }
                }
            }
        }
    }
}
