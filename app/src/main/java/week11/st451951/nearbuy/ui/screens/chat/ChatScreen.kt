// In the NEW file: app/.../ui/screens/chat/ChatScreen.kt

package week11.st451951.nearbuy.ui.screens.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import week11.st451951.nearbuy.data.User

// This is the UI for the one-on-one chat
@Composable
fun ChatScreen(
    otherUser: User? // It receives the full User object from the ViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // This Text will now display the user's name, or "Loading..."
        Text(
            text = "Chat with ${otherUser?.displayName ?: "Loading..."}",
            style = MaterialTheme.typography.headlineSmall
        )

        // Your message list, text input field, and send button will go here
    }
}
