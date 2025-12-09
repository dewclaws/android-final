package week11.st451951.nearbuy.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UsersRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    /**
     * Create or update a user document
     */
    suspend fun saveUser(userId: String, displayName: String, email: String): Result<Unit> {
        return try {
            val user = mapOf(
                "displayName" to displayName,
                "username" to email,
                "email" to email
            )
            usersCollection.document(userId).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get a user by their ID
     */
    suspend fun getUser(userId: String): Result<User> {
        return try {
            val document = usersCollection.document(userId).get().await()
            val user = document.toObject(User::class.java)

            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}