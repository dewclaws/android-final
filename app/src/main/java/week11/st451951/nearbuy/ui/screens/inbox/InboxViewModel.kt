package week11.st451951.nearbuy.ui.screens.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import week11.st451951.nearbuy.ui.screens.chat.ChatThread

data class StartChatState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
class InboxViewModel : ViewModel() {

    private val repository = InboxRepository()
    val currentUserId: String? = FirebaseAuth.getInstance().currentUser?.uid

    private val _chatThreads = MutableStateFlow<List<ChatThread>>(emptyList())
    val chatThreads: StateFlow<List<ChatThread>> = _chatThreads.asStateFlow()

    private val _startChatState = MutableStateFlow(StartChatState())
    val startChatState = _startChatState.asStateFlow()

    init {
        // As soon as the ViewModel is created, start listening for the chat threads.
        viewModelScope.launch {
            repository.getChatThreads().collect { threads ->
                _chatThreads.value = threads
            }
        }
    }


    fun startChatWithUsername(username: String) {
        viewModelScope.launch {
            _startChatState.value = StartChatState(isLoading = true)

            try {
                if (repository.isCurrentUser(username)) {
                    _startChatState.value = StartChatState(error = "You cannot start a chat with yourself.")
                    return@launch
                }
                // 1. Find the user ID for the given username from the repository
                val otherUserId = repository.findUserByUsername(username)
                if (otherUserId == null) {
                    _startChatState.value = StartChatState(error = "User not found")
                    return@launch
                }

                // 2. Check if a chat with this user already exists
                val existingChat =
                    _chatThreads.value.find { it.participantIds.contains(otherUserId) }
                if (existingChat != null) {
                    _startChatState.value =
                        StartChatState(error = "A chat with this user already exists.")
                    return@launch
                }

                // 3. Create the new chat document in the repository
                repository.createChat(otherUserId)
                _startChatState.value = StartChatState(isSuccess = true)

            } catch (e: Exception) {
                _startChatState.value =
                    StartChatState(error = e.message ?: "An unknown error occurred")
            }
        }
    }

    fun resetStartChatState() {
        _startChatState.value = StartChatState()
    }
}



