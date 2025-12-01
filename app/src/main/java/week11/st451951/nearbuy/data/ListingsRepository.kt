package week11.st451951.nearbuy.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ListingsRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val listingsCollection = firestore.collection("listings")

    /**
     * Get all listings ordered by creation date (newest first)
     */
    fun getAllListings(): Flow<List<Listing>> = callbackFlow {
        val listener = listingsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val listings = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Listing::class.java)
                } ?: emptyList()

                trySend(listings)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get listings for the current user
     */
    fun getUserListings(): Flow<List<Listing>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = listingsCollection
            .whereEqualTo("sellerId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val listings = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Listing::class.java)
                } ?: emptyList()

                trySend(listings)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get a single listing by ID
     */
    suspend fun getListing(listingId: String): Result<Listing> {
        return try {
            val document = listingsCollection.document(listingId).get().await()
            val listing = document.toObject(Listing::class.java)

            if (listing != null) {
                Result.success(listing)
            } else {
                Result.failure(Exception("Listing not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create a new listing
     */
    suspend fun createListing(
        title: String,
        price: Double,
        description: String,
        imageUrls: List<String> = emptyList(),
        location: ListingLocation = ListingLocation()
    ): Result<String> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not authenticated"))

            val listing = Listing(
                title = title,
                price = price,
                description = description,
                imageUrls = imageUrls,
                sellerId = userId,
                location = location,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            val documentRef = listingsCollection.add(listing).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update an existing listing
     */
    suspend fun updateListing(
        listingId: String,
        title: String,
        price: Double,
        description: String,
        imageUrls: List<String>,
        location: ListingLocation? = null
    ): Result<Unit> {
        return try {
            val updates = hashMapOf(
                "title" to title,
                "price" to price,
                "description" to description,
                "imageUrls" to imageUrls,
                "updatedAt" to Timestamp.now()
            )

            // Only update location if provided
            if (location != null) {
                updates["location"] = location
            }

            listingsCollection.document(listingId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a listing and its associated images from Firebase Storage
     */
    suspend fun deleteListing(listingId: String): Result<Unit> {
        return try {
            // Get the listing to retrieve image URLs
            val document = listingsCollection.document(listingId).get().await()
            val listing = document.toObject(Listing::class.java)

            // Delete all associated images from Storage
            listing?.imageUrls?.forEach { imageUrl ->
                try {
                    val storageRef = storage.getReferenceFromUrl(imageUrl)
                    storageRef.delete().await()
                } catch (e: Exception) {
                    // Log but don't fail if image deletion fails,
                    // because it's not the end of the world
                    android.util.Log.w("ListingsRepository", "Failed to delete image: $imageUrl", e)
                }
            }

            // Delete the Firestore document
            listingsCollection.document(listingId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}