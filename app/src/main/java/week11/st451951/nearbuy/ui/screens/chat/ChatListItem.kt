// In the NEW file: app/.../ui/screens/chat/ChatListItem.kt

package week11.st451951.nearbuy.ui.screens.chat

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import week11.st451951.nearbuy.data.User
import week11.st451951.nearbuy.data.UsersRepository

@Composable
fun ChatListItem(
    otherUserId: String,
    modifier: Modifier = Modifier
) {
    // State to hold the other user's details. Default to null.
    var otherUser by remember { mutableStateOf<User?>(null) }
    val usersRepository = remember { UsersRepository() }

    // This runs when the component first appears. It fetches the user's data.
    LaunchedEffect(otherUserId) {
        val userResult = usersRepository.getUser(otherUserId)

        userResult.onSuccess { user ->
            otherUser = user
            // Handle the success case
        }.onFailure { exception ->
            // Handle the failure case
            println("Failed to fetch user details: ${exception.message}")
        }

    }

    // This is the UI of the list item
    ListItem(
        modifier = modifier,
        headlineContent = {
            // Display the user's name if loaded, otherwise show the ID as a fallback
            Text(
                "Chat with ${otherUser?.displayName ?: "User..."}",
                fontWeight = FontWeight.Bold
            )
        },
        supportingContent = { Text("Tap to open conversation") },
        leadingContent = {
            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat icon")
        }
    )
}
