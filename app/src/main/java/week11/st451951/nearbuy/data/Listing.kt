package week11.st451951.nearbuy.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import week11.st451951.nearbuy.R

enum class Category(val displayName: String, val drawableRes: Int) {
    ELECTRONICS("Electronics", R.drawable.category_electronics),
    FURNITURE("Furniture", R.drawable.category_furniture),
    APPLIANCES("Appliances", R.drawable.category_appliances),
    FASHION("Fashion", R.drawable.category_fashion),
    BOOKS("Books", R.drawable.category_books),
    SPORTS("Sports", R.drawable.category_sports);

    companion object {
        fun fromString(value: String): Category? {
            return entries.find { it.displayName.equals(value, ignoreCase = true) }
        }
    }
}

data class ListingLocation(
    val postalCode: String = "",
    val city: String = "",
    val province: String = "",
    val country: String = "Canada",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class Listing(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val imageUrls: List<String> = emptyList(),
    val sellerId: String = "",
    val location: ListingLocation = ListingLocation(),
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)
