// In week11/st451951/nearbuy/ui/screens/inbox/InboxScreen.kt

package week11.st451951.nearbuy.ui.screens.inbox

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add // IMPORT THIS
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import week11.st451951.nearbuy.navigation.Screen // IMPORT THIS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    navController: NavController,
    viewModel: InboxViewModel
) {
    val chatThreads by viewModel.chatThreads.collectAsState()
    val currentUserId = viewModel.currentUserId

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Messages") })
        },
        // THIS IS THE NEW CODE YOU NEED TO ADD
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Navigate to the screen for starting a new chat
                    navController.navigate(Screen.StartConversation.route)
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Start new conversation")
            }
        }
        // END OF NEW CODE
    ) { paddingValues ->
        if (chatThreads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No conversations yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(chatThreads) { thread ->
                    val otherUserId = thread.participantIds.firstOrNull { it != currentUserId }

                    if (otherUserId != null) {
                        InboxItem(
                            // To show a real name, you'd need another repository call.
                            // For now, the ID is fine for testing.
                            userName = "Chat with User ID: $otherUserId",
                            onClick = {
                                navController.navigate(Screen.ChatDetail.createRoute(otherUserId))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InboxItem(userName: String, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(userName, fontWeight = FontWeight.Bold) },
        supportingContent = { Text("Tap to open conversation") },
        leadingContent = {
            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat icon")
        }
    )
}
