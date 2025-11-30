package week11.st451951.nearbuy.ui.screens.chat

/**
 * A simple data class to hold information about a chat conversation.
 */
data class ChatThread(
    val id: String = "",
    val participantIds: List<String> = emptyList()
)
