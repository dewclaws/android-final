package week11.st451951.nearbuy.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import week11.st451951.nearbuy.auth.AuthViewModel
import week11.st451951.nearbuy.ui.screens.auth.AuthScreen
import week11.st451951.nearbuy.ui.screens.buy.BuyScreen
import week11.st451951.nearbuy.ui.screens.chat.ChatScreen
import week11.st451951.nearbuy.ui.screens.sell.CreateListingScreen
import week11.st451951.nearbuy.ui.screens.sell.ListingDetailScreen
import week11.st451951.nearbuy.ui.screens.sell.SellScreen

// Routes
sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Buy : Screen("buy")
    object Sell : Screen("sell")
    object Chat : Screen("chat")
    object CreateListing : Screen("sell/create_listing")
    object ListingDetail : Screen("sell/listing/{listingId}") {
        fun createRoute(listingId: String) = "sell/listing/$listingId"
    }
    object EditListing : Screen("sell/edit/{listingId}") {
        fun createRoute(listingId: String) = "sell/edit/$listingId"
    }
}

// Bottom nav menu item class
data class BottomNavItem(
    val screen: Screen,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

// Bottom nav menu items list
val bottomNavItems = listOf(
    BottomNavItem(
        screen = Screen.Buy,
        title = "Buy",
        selectedIcon = Icons.Filled.ShoppingCart,
        unselectedIcon = Icons.Outlined.ShoppingCart
    ),
    BottomNavItem(
        screen = Screen.Sell,
        title = "Sell",
        selectedIcon = Icons.Filled.Sell,
        unselectedIcon = Icons.Outlined.Sell
    ),
    BottomNavItem(
        screen = Screen.Chat,
        title = "Messages",
        selectedIcon = Icons.Filled.ChatBubble,
        unselectedIcon = Icons.Outlined.ChatBubbleOutline
    )
)

// Navigation graph
@Composable
fun NearBuyNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Auth.route,
    authViewModel: AuthViewModel = viewModel()
) {

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(authViewModel = authViewModel)
        }

        // ###############################
        // MAIN APP SCREENS
        //
        // Requires auth beyond this point
        // ###############################

        composable(Screen.Buy.route) {
            AuthGuard(
                authViewModel = authViewModel,
                navController = navController
            ) {
                BuyScreen()
            }
        }

        composable(Screen.Sell.route) {
            AuthGuard(
                authViewModel = authViewModel,
                navController = navController
            ) {
                SellScreen(
                    onNavigateToCreateListing = {
                        navController.navigate(Screen.CreateListing.route)
                    },
                    onListingClick = { listingId ->
                        navController.navigate(Screen.ListingDetail.createRoute(listingId))
                    }
                )
            }
        }

        composable(Screen.CreateListing.route) {
            AuthGuard(
                authViewModel = authViewModel,
                navController = navController
            ) {
                CreateListingScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onListingCreated = { listingId ->
                        navController.navigate(Screen.ListingDetail.createRoute(listingId)) {
                            popUpTo(Screen.Sell.route)
                        }
                    }
                )
            }
        }

        composable(
            route = Screen.ListingDetail.route,
            arguments = listOf(navArgument("listingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId") ?: return@composable
            AuthGuard(
                authViewModel = authViewModel,
                navController = navController
            ) {
                ListingDetailScreen(
                    listingId = listingId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onEditListing = { editListingId ->
                        navController.navigate(Screen.EditListing.createRoute(editListingId))
                    }
                )
            }
        }

        composable(
            route = Screen.EditListing.route,
            arguments = listOf(navArgument("listingId") { type = NavType.StringType })
        ) { backStackEntry ->
            backStackEntry.arguments?.getString("listingId") ?: return@composable
            AuthGuard(
                authViewModel = authViewModel,
                navController = navController
            ) {
                // For now, navigate back - TODO: Edit listing functionality
                navController.popBackStack()
            }
        }

        composable(Screen.Chat.route) {
            AuthGuard(
                authViewModel = authViewModel,
                navController = navController
            ) {
                ChatScreen()
            }
        }
    }
}

// Ensures authentication when required
@Composable
fun AuthGuard(
    authViewModel: AuthViewModel,
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (!authViewModel.isLoggedIn) {
            Toast.makeText(context, "Please sign in to continue", Toast.LENGTH_SHORT).show()
            navController.navigate(Screen.Auth.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    if (authViewModel.isLoggedIn) {
        content()
    }
}

// Main app scaffold
@Composable
fun MainScaffold(
    navController: NavHostController,
    content: @Composable (Modifier) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Hide the bottom nav bar on login screen
    val showBottomBar = currentDestination?.route != Screen.Auth.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    // Render nav buttons
                    // Runs for each button
                    bottomNavItems.forEach { item ->
                        // Check if current route belongs to this tab's navigation stack
                        val selected = currentDestination?.route?.startsWith(item.screen.route) == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            selected = selected,
                            onClick = {
                                // If already on this tab, pop to its root
                                if (selected) {
                                    navController.popBackStack(item.screen.route, inclusive = false)
                                } else {
                                    // Navigate to the tab
                                    navController.navigate(item.screen.route) {
                                        // Pop up to the start destination to avoid building up a large stack
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        // Avoid multiple copies of the same destination
                                        launchSingleTop = true
                                        // Restore state when navigating between segments
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        content(Modifier.padding(innerPadding))
    }
}