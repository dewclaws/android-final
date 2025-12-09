package week11.st451951.nearbuy.ui.screens.sell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import week11.st451951.nearbuy.data.Listing
import week11.st451951.nearbuy.data.ListingsRepository

/**
 * Edit listing UI state
 */
data class EditListingUIState(
    val listing: Listing? = null,
    val title: String = "",
    val priceText: String = "",
    val description: String = "",
    val titleError: String? = null,
    val priceError: String? = null,
    val descriptionError: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false
)

/**
 * Edit listing events
 */
sealed class EditListingEvent {
    data class ShowToast(val message: String) : EditListingEvent()
    data class ListingUpdated(val listingId: String) : EditListingEvent()
}

/**
 * ViewModel for EditListingScreen
 *
 * Handles fetching existing listing, form state, validation, and updating
 */
class EditListingViewModel(
    private val repository: ListingsRepository = ListingsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditListingUIState())
    val uiState: StateFlow<EditListingUIState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EditListingEvent>()
    val events = _events.asSharedFlow()

    // #############
    // LOAD LISTING
    // #############
    fun loadListing(listingId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = repository.getListing(listingId)
            if (result.isSuccess) {
                val listing = result.getOrNull()
                _uiState.value = _uiState.value.copy(
                    listing = listing,
                    title = listing?.title ?: "",
                    priceText = listing?.price?.toString() ?: "",
                    description = listing?.description ?: "",
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _events.emit(
                    EditListingEvent.ShowToast(
                        "Failed to load listing: ${result.exceptionOrNull()?.message}"
                    )
                )
            }
        }
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
                _uiState.value = _uiState.value.copy(priceError = "Please enter a valid non-negative price")
                hasError = true
            }
        }

        if (state.description.isBlank()) {
            _uiState.value = _uiState.value.copy(descriptionError = "Please enter a description")
            hasError = true
        }

        return !hasError
    }

    // ##############
    // UPDATE LISTING
    // ##############
    fun updateListing(listingId: String) {
        if (!validate()) {
            viewModelScope.launch {
                _events.emit(EditListingEvent.ShowToast("One or more fields are invalid"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            try {
                val updateResult = repository.updateListing(
                    listingId = listingId,
                    title = _uiState.value.title,
                    price = _uiState.value.priceText.toDouble(),
                    description = _uiState.value.description,
                    imageUrls = _uiState.value.listing?.imageUrls ?: emptyList()
                )

                if (updateResult.isSuccess) {
                    _events.emit(EditListingEvent.ShowToast("Listing edited"))
                    _events.emit(EditListingEvent.ListingUpdated(listingId))
                } else {
                    _events.emit(
                        EditListingEvent.ShowToast(
                            "Failed to update listing: ${updateResult.exceptionOrNull()?.message}"
                        )
                    )
                }
            } catch (e: Exception) {
                _events.emit(EditListingEvent.ShowToast("Error: ${e.message}"))
            } finally {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }
}
