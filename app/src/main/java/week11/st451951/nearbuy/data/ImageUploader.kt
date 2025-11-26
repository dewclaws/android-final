package week11.st451951.nearbuy.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Utility class for uploading images to Firebase Storage
 */
class ImageUploader() {
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Upload a single image to Firebase Storage
     * @param imageUri The local URI of the image to upload
     * @return Result containing the download URL or an error
     */
    suspend fun uploadImage(imageUri: Uri): Result<String> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not authenticated"))

            // Generate a unique filename
            val filename = "${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference
                .child("listings")
                .child(userId)
                .child(filename)

            // Upload the image
            val uploadTask = storageRef.putFile(imageUri).await()

            // Get the download URL
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload multiple images to Firebase Storage
     * @param imageUris List of local URIs of images to upload
     * @return Result containing a list of download URLs or an error
     */
    suspend fun uploadImages(imageUris: List<Uri>): Result<List<String>> {
        return try {
            val downloadUrls = mutableListOf<String>()

            for (uri in imageUris) {
                val result = uploadImage(uri)
                if (result.isSuccess) {
                    downloadUrls.add(result.getOrThrow())
                } else {
                    return Result.failure(result.exceptionOrNull() ?: Exception("Upload failed"))
                }
            }

            Result.success(downloadUrls)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete an image from Firebase Storage given its download URL
     * @param downloadUrl The download URL of the image to delete
     */
    suspend fun deleteImage(downloadUrl: String): Result<Unit> {
        return try {
            val storageRef = storage.getReferenceFromUrl(downloadUrl)
            storageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
