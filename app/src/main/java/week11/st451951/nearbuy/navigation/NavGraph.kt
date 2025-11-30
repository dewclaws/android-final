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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import week11.st451951.nearbuy.ui.screens.inbox.InboxScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import week11.st451951.nearbuy.auth.AuthViewModel
import week11.st451951.nearbuy.ui.screens.auth.AuthScreen
import week11.st451951.nearbuy.ui.screens.buy.BuyListingDetailScreen
import week11.st451951.nearbuy.ui.screens.buy.BuyScreen
import week11.st451951.nearbuy.ui.screens.chat.ChatListScreen
import week11.st451951.nearbuy.ui.screens.inbox.InboxViewModel
import week11.st451951.nearbuy.ui.screens.inbox.StartConversationScreen
import week11.st451951.nearbuy.ui.screens.sell.CreateListingScreen
import week11.st451951.nearbuy.ui.screens.sell.EditListingScreen
import week11.st451951.nearbuy.ui.screens.sell.ListingDetailScreen
import week11.st451951.nearbuy.ui.screens.sell.SellScreen
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import week11.st451951.nearbuy.data.UsersRepository
import week11.st451951.nearbuy.ui.screens.chat.ChatScreen
import week11.st451951.nearbuy.ui.screens.chat.ChatViewModel

data class ScaffoldState(
    val isFabVisible: Boolean = false,
    val onFabClick: () -> Unit = {}
)
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
    navController: NavHostController,
    startDestination: String = Screen.Auth.route,
    authViewModel: AuthViewModel = viewModel(),
    onScaffoldStateChange: (ScaffoldState) -> Unit

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
                BuyScreen(
                    onListingClick = { listingId ->
                        navController.navigate(Screen.BuyListingDetail.createRoute(listingId))
                    }
                )
            }
        }

        composable(
            route = Screen.BuyListingDetail.route,
            arguments = listOf(navArgument("listingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId") ?: return@composable
            AuthGuard(
                authViewModel = authViewModel,
                navController = navController
            ) {
                BuyListingDetailScreen(
                    listingId = listingId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
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
            val listingId = backStackEntry.arguments?.getString("listingId") ?: return@composable
            AuthGuard(
                authViewModel = authViewModel,
                navController = navController
            ) {
                EditListingScreen(
                    listingId = listingId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onListingUpdated = { updatedListingId ->
                        navController.navigate(Screen.ListingDetail.createRoute(updatedListingId)) {
                            popUpTo(Screen.ListingDetail.createRoute(updatedListingId)) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }

        // The "chat" route now shows the list of all conversations (the inbox).
        composable(Screen.Chat.route) {
            AuthGuard(
                authViewModel = authViewModel,
                navController = navController
            ) {
                // It now calls InboxScreen instead of ChatScreen
                val inboxViewModel: InboxViewModel = viewModel()

                LaunchedEffect(Unit) {
                    onScaffoldStateChange(
                        ScaffoldState(
                            isFabVisible = true,
                            onFabClick = { navController.navigate(Screen.StartConversation.route) }
                        )
                    )
                }

                // Clean up when leaving the screen
                DisposableEffect(Unit) {
                    onDispose {
                        onScaffoldStateChange(ScaffoldState(isFabVisible = false))
                    }
                }

                InboxScreen(
                    navController = navController,
                    viewModel = inboxViewModel
                )
            }
        }


        composable(Screen.StartConversation.route) {
            AuthGuard(
                authViewModel = authViewModel,
                navController = navController
            ) {
                val inboxViewModel: InboxViewModel = viewModel() // Use the same ViewModel instance
                StartConversationScreen(
                    navController = navController,
                    viewModel = inboxViewModel
                )
            }
        }
        // This is the new destination for a specific one-on-one chat.
        composable(
            route = Screen.ChatDetail.route,
            arguments = listOf(navArgument("otherUserId") { type = NavType.StringType })
        ) { backStackEntry ->

            AuthGuard(
                authViewModel = authViewModel,
                navController = navController
            ) {
                // --- THIS IS THE ONLY PART THAT CHANGES ---

                // 1. Create a UsersRepository instance
                val usersRepository = remember { UsersRepository() }

                // 2. Create the ChatViewModel, telling it how to get the UsersRepository
                val chatViewModel: ChatViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return ChatViewModel(
                                savedStateHandle = backStackEntry.savedStateHandle,
                                usersRepository = usersRepository
                            ) as T
                        }
                    }
                )

                // 3. Call your new ChatScreen UI, passing it the data from the ViewModel
                ChatScreen(otherUser = chatViewModel.otherUser)
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
    scaffoldState: ScaffoldState,
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
                    bottomNavItems.forEach { item ->
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
                                if (selected) {
                                    navController.popBackStack(item.screen.route, inclusive = false)
                                } else {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            // Only show the FAB if the current screen has requested it
            if (scaffoldState.isFabVisible) {
                FloatingActionButton(onClick = scaffoldState.onFabClick) {
                    Icon(Icons.Filled.Add, "Start new conversation")
                }
            }
        }
    ) { innerPadding ->
        content(Modifier.padding(innerPadding))
    }
}