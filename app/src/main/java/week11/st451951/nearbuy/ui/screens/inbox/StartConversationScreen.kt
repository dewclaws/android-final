// week11/st451951/nearbuy/ui/screens/inbox/StartConversationScreen.kt
package week11.st451951.nearbuy.ui.screens.inbox

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.error
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartConversationScreen(
    navController: NavController,
    viewModel: InboxViewModel // We will use the same ViewModel
) {
    var username by remember { mutableStateOf("") }
    val startChatState by viewModel.startChatState.collectAsState()

    LaunchedEffect(startChatState) {
        if (startChatState.isSuccess) {
            // When chat is created, pop this screen off the backstack
            // and go back to the inbox.
            navController.popBackStack()
            viewModel.resetStartChatState() // Reset state for next time
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Start a new conversation") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Enter username") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (username.isNotBlank()) {
                        viewModel.startChatWithUsername(username)
                    }
                },
                enabled = !startChatState.isLoading // Disable button while loading
            ) {
                if (startChatState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Start Chat")
                }
            }

            if (startChatState.error != null) {
                Text(
                    text = startChatState.error ?: "An unknown error occured",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
