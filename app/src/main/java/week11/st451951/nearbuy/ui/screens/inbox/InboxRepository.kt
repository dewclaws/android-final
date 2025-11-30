package week11.st451951.nearbuy.ui.screens.inbox

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await // Make sure you have this import
import week11.st451951.nearbuy.ui.screens.chat.ChatThread

class InboxRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getChatThreads(): Flow<List<ChatThread>> {
        // This creates a flow that safely handles the user state.
        return callbackFlow {
            // Note: It's better to use the class-level 'auth' instance
            val currentUserId = auth.currentUser?.uid

            if (currentUserId == null) {
                trySend(emptyList())
                close()
                return@callbackFlow
            }

            val listenerRegistration = firestore.collection("chats")
                .whereArrayContains("participantIds", currentUserId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val threads = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(ChatThread::class.java)?.copy(id = doc.id)
                        }
                        trySend(threads)
                    }
                }

            awaitClose {
                listenerRegistration.remove()
            }
        }
    }

    // --- ALL THE FUNCTIONS ARE NOW INSIDE THE CLASS ---

    suspend fun isCurrentUser(username: String): Boolean {
        return try {
            val foundUserId = findUserByUsername(username)
            // NO ERROR: 'auth' is now accessible from the class
            foundUserId != null && foundUserId == auth.currentUser?.uid
        } catch (e: Exception) {
            android.util.Log.e("InboxRepository", "Error in isCurrentUser: ${e.message}")
            false
        }
    }

    suspend fun findUserByUsername(username: String): String? {
        // NO ERROR: 'firestore' is now accessible from the class
        val querySnapshot = firestore.collection("users")
            .whereEqualTo("username", username.trim())
            .limit(1)
            .get()
            .await()

        if (querySnapshot.isEmpty) {
            return null
        }
        return querySnapshot.documents.firstOrNull()?.id
    }

    suspend fun createChat(otherUserId: String) {
        // NO ERROR: 'auth' and 'firestore' are now accessible from the class
        val currentUserId = auth.currentUser?.uid ?: return

        val participants = listOf(currentUserId, otherUserId)

        val chatData = mapOf(
            "participantIds" to participants,
            "lastMessage" to "Chat started!",
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("chats").add(chatData).await()
    }
}
