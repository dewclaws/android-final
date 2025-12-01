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
import week11.st451951.nearbuy.data.Category
import week11.st451951.nearbuy.data.ImageUploader
import week11.st451951.nearbuy.data.ListingLocation
import week11.st451951.nearbuy.data.ListingsRepository
import week11.st451951.nearbuy.data.LocationManager

/**
 * Create listing UI state
 */
data class CreateListingUIState(
    val title: String = "",
    val category: Category? = null,
    val priceText: String = "",
    val description: String = "",
    val selectedImages: List<Uri> = emptyList(),
    val location: ListingLocation? = null,
    val isLoadingLocation: Boolean = false,
    val titleError: String? = null,
    val categoryError: String? = null,
    val priceError: String? = null,
    val descriptionError: String? = null,
    val imageError: String? = null,
    val locationError: String? = null,
    val isLoading: Boolean = false
)

/**
 * Create listing events
 */
sealed class CreateListingEvent {
    data class ShowToast(val message: String) : CreateListingEvent()
    data class ListingCreated(val listingId: String) : CreateListingEvent()
    object RequestLocationPermission : CreateListingEvent()
}

/**
 * ViewModel for CreateListingScreen
 */
class CreateListingViewModel(
    private val repository: ListingsRepository = ListingsRepository(),
    private val imageUploader: ImageUploader = ImageUploader(),
    private val locationManager: LocationManager? = null
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

    fun updateCategory(category: Category) {
        _uiState.value = _uiState.value.copy(category = category, categoryError = null)
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

    // ###############
    // LOCATION UPDATE
    // ###############
    fun requestLocation() {
        if (locationManager == null) {
            viewModelScope.launch {
                _events.emit(CreateListingEvent.ShowToast("Location services not available"))
            }
            return
        }

        if (!locationManager.hasLocationPermission()) {
            viewModelScope.launch {
                _events.emit(CreateListingEvent.RequestLocationPermission)
            }
            return
        }

        fetchLocation()
    }

    fun fetchLocation() {
        if (locationManager == null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingLocation = true, locationError = null)

            val result = locationManager.getCurrentLocation()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    location = result.getOrNull(),
                    isLoadingLocation = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoadingLocation = false,
                    locationError = "Unable to get location"
                )
                _events.emit(
                    CreateListingEvent.ShowToast(
                        "Failed to get location: ${result.exceptionOrNull()?.message}"
                    )
                )
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

        if (state.category == null) {
            _uiState.value = _uiState.value.copy(categoryError = "Please select a category")
            hasError = true
        }

        if (state.priceText.isBlank()) {
            _uiState.value = _uiState.value.copy(priceError = "Please enter a price")
            hasError = true
        } else {
            val price = state.priceText.toDoubleOrNull()
            if (price == null || price < 0) {
                _uiState.value = _uiState.value.copy(priceError = "Please enter a valid non-negative price")
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

                // Create listing with category
                val createResult = repository.createListing(
                    title = _uiState.value.title,
                    category = _uiState.value.category!!.displayName,
                    price = _uiState.value.priceText.toDouble(),
                    description = _uiState.value.description,
                    imageUrls = imageUrls,
                    location = _uiState.value.location ?: ListingLocation()
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