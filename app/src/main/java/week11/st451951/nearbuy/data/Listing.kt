package week11.st451951.nearbuy.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Listing(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val imageUrls: List<String> = emptyList(),
    val sellerId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)