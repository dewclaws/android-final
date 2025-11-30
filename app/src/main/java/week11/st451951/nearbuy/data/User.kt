package week11.st451951.nearbuy.data

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val id: String = "",
    val displayName: String = "",
    val email: String = ""
)