package week11.st451951.nearbuy.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import week11.st451951.nearbuy.data.UsersRepository

/**
 * Handles backend authentication tasks such as
 *   - Tracking the current user
 *   - Signing a user in, given credentials
 *   - Registering a user, given credentials and a name
 *   - Resetting a user's password, given their email
 *   - Signing the user out
 */
class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isLoggedIn: Boolean
        get() = currentUser != null

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()

            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Sign in failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()

            result.user?.let { user ->
                // Update display name
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()

                user.updateProfile(profileUpdates).await()

                // Save user to Firestore
                val usersRepository = UsersRepository()
                val saveResult = usersRepository.saveUser(
                    userId = user.uid,
                    displayName = name,
                    email = email
                )

                // Log the result
                if (saveResult.isSuccess) {
                    android.util.Log.d("AuthRepository", "User saved to Firestore successfully")
                } else {
                    android.util.Log.e("AuthRepository", "Failed to save user: ${saveResult.exceptionOrNull()?.message}")
                }

                Result.success(user)
            } ?: Result.failure(Exception("Registration failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}