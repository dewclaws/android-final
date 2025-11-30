// In app/src/main/java/week11/st451951/nearbuy/ui/screens/chat/ChatViewModel.kt

package week11.st451951.nearbuy.ui.screens.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import week11.st451951.nearbuy.data.User
import week11.st451951.nearbuy.data.UsersRepository

class ChatViewModel(
    savedStateHandle: SavedStateHandle,
    private val usersRepository: UsersRepository // Needs repository to fetch data
) : ViewModel() {

    // Get the user ID that was passed through navigation
    private val otherUserId: String = checkNotNull(savedStateHandle["otherUserId"])

    // A state variable to hold the full User object of the person we're chatting with
    var otherUser by mutableStateOf<User?>(null)
        private set

    // This block runs as soon as the ViewModel is created
    init {
        // Fetch the user details right away
        viewModelScope.launch {
            // Use the UsersRepository to get the user's data from Firestore
            val result = usersRepository.getUser(otherUserId)

            // Use onSuccess to safely get the user from the Result
            result.onSuccess { userFromDb ->
                // If the user is found, update our state variable, which will update the UI
                otherUser = userFromDb
            }.onFailure { exception ->
                // Handle the case where the user couldn't be found
                println("Failed to fetch user details: ${exception.message}")
            }
        }
    }
}