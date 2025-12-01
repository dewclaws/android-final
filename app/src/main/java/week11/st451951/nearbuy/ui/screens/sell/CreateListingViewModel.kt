package week11.st451951.nearbuy.ui.screens.sell

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import week11.st451951.nearbuy.data.ImageUploader
import week11.st451951.nearbuy.data.ListingsRepository

/**
 * Create listing UI state
 */
data class CreateListingUIState(
    val title: String = "",
    val priceText: String = "",
    val description: String = "",
    val selectedImages: List<Uri> = emptyList(),
    val titleError: String? = null,
    val priceError: String? = null,
    val descriptionError: String? = null,
    val imageError: String? = null,
    val isLoading: Boolean = false
)

/**
 * Create listing events
 */
sealed class CreateListingEvent {
    data class ShowToast(val message: String) : CreateListingEvent()
    data class ListingCreated(val listingId: String) : CreateListingEvent()
}

/**
 * ViewModel for CreateListingScreen
 *
 * Handles form state, validation, image upload, and listing creation
 */
class CreateListingViewModel(
    private val repository: ListingsRepository = ListingsRepository(),
    private val imageUploader: ImageUploader = ImageUploader()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateListingUIState())
    val uiState: StateFlow<CreateListingUIState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CreateListingEvent>()
    val events = _events.asSharedFlow()

    companion object {
        private const val MAX_IMAGES = 4
    }

    // #############
    // FIELD UPDATES
    // #############
    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title, titleError = null)
    }

    fun updatePrice(priceText: String) {
        _uiState.value = _uiState.value.copy(priceText = priceText, priceError = null)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description, descriptionError = null)
    }

    fun addImage(uri: Uri) {
        val currentImages = _uiState.value.selectedImages
        if (currentImages.size < MAX_IMAGES) {
            _uiState.value = _uiState.value.copy(
                selectedImages = currentImages + uri,
                imageError = null
            )
        } else {
            viewModelScope.launch {
                _events.emit(CreateListingEvent.ShowToast("Maximum $MAX_IMAGES images allowed"))
            }
        }
    }

    // ##########
    // VALIDATION
    // ##########
    private fun validate(): Boolean {
        val state = _uiState.value
        var hasError = false

        if (state.title.isBlank()) {
            _uiState.value = _uiState.value.copy(titleError = "Please enter a title")
            hasError = true
        }

        if (state.priceText.isBlank()) {
            _uiState.value = _uiState.value.copy(priceError = "Please enter a price")
            hasError = true
        } else {
            val price = state.priceText.toDoubleOrNull()
            if (price == null || price < 0) {
                _uiState.value = _uiState.value.copy(priceError = "Please enter a non-negative price")
                hasError = true
            }
        }

        if (state.description.isBlank()) {
            _uiState.value = _uiState.value.copy(descriptionError = "Please enter a description")
            hasError = true
        }

        if (state.selectedImages.isEmpty()) {
            _uiState.value = _uiState.value.copy(imageError = "Please add at least one image")
            hasError = true
        }

        return !hasError
    }

    // ###############
    // CREATE LISTING
    // ###############
    fun createListing() {
        if (!validate()) {
            viewModelScope.launch {
                _events.emit(CreateListingEvent.ShowToast("One or more fields are invalid"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Upload images
                val uploadResult = imageUploader.uploadImages(_uiState.value.selectedImages)
                if (uploadResult.isFailure) {
                    _events.emit(
                        CreateListingEvent.ShowToast(
                            "Failed to upload images: ${uploadResult.exceptionOrNull()?.message}"
                        )
                    )
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    return@launch
                }

                val imageUrls = uploadResult.getOrThrow()

                // Create listing
                val createResult = repository.createListing(
                    title = _uiState.value.title,
                    price = _uiState.value.priceText.toDouble(),
                    description = _uiState.value.description,
                    imageUrls = imageUrls
                )

                if (createResult.isSuccess) {
                    _events.emit(CreateListingEvent.ShowToast("Listing created!"))
                    _events.emit(CreateListingEvent.ListingCreated(createResult.getOrThrow()))
                } else {
                    _events.emit(
                        CreateListingEvent.ShowToast(
                            "Failed to create listing: ${createResult.exceptionOrNull()?.message}"
                        )
                    )
                }
            } catch (e: Exception) {
                _events.emit(CreateListingEvent.ShowToast("Error: ${e.message}"))
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
