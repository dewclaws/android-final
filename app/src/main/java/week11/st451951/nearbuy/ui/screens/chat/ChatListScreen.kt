// This file is now our INBOX screen.
// We've simplified its package name for clarity.
package week11.st451951.nearbuy.ui.screens.chat

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import week11.st451951.nearbuy.navigation.Screen

/**
 * THIS IS THE INBOX SCREEN.
 * It fetches and displays a list of all your chat conversations.
 * All logic is inside this file for simplicity.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(navController: NavController) { // Renamed the function to be clear

    // State to hold the list of chat threads.
    var chatThreads by remember { mutableStateOf<List<ChatThread>>(emptyList()) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    // This block runs once and fetches the data from Firestore.
    // This block runs once and fetches the data from Firestore WITHOUT KTX.
    LaunchedEffect(key1 = currentUserId) {
        if (currentUserId == null) return@LaunchedEffect

        // Get the Firebase instances without .ktx
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("chats")
            .whereArrayContains("participantIds", auth.currentUser!!.uid) // Use the instance here
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.w("ChatListScreen", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val threads = snapshot.documents.mapNotNull { doc ->
                        // Manually create the ChatThread object from the document
                        val participantIds = doc.get("participantIds") as? List<String> ?: emptyList()
                        ChatThread(id = doc.id, participantIds = participantIds)
                    }
                    chatThreads = threads
                }
            }
    }


    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Messages") })
        }
    ) { paddingValues ->
        if (chatThreads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("You have no conversations yet.")
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
                        ChatListItem(
                            otherUserId = otherUserId,
                            modifier = Modifier.clickable {
                                // Your navigation logic was missing here, so we'll add it back.
                                navController.navigate(Screen.ChatDetail.createRoute(otherUserId))        }
                        )

                    }
                }
            }
        }
    }
}
