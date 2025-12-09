package week11.st451951.nearbuy.ui.screens.sell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import week11.st451951.nearbuy.data.Listing
import week11.st451951.nearbuy.data.ListingsRepository

/**
 * Listing detail UI state (seller's view)
 */
data class ListingDetailUIState(
    val listing: Listing? = null,
    val isLoading: Boolean = true,
    val showDeleteDialog: Boolean = false,
    val isDeleting: Boolean = false,
    val isOwner: Boolean = false
)

/**
 * Listing detail events
 */
sealed class ListingDetailEvent {
    data class ShowToast(val message: String) : ListingDetailEvent()
    object NavigateBack : ListingDetailEvent()
}

/**
 * ViewModel for ListingDetailScreen (seller's view)
 *
 * Handles fetching listing details, ownership check, and deletion
 */
class ListingDetailViewModel(
    private val repository: ListingsRepository = ListingsRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListingDetailUIState())
    val uiState: StateFlow<ListingDetailUIState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ListingDetailEvent>()
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
                val isOwner = listing?.sellerId == auth.currentUser?.uid
                _uiState.value = _uiState.value.copy(
                    listing = listing,
                    isOwner = isOwner,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _events.emit(
                    ListingDetailEvent.ShowToast(
                        "Failed to load listing: ${result.exceptionOrNull()?.message}"
                    )
                )
            }
        }
    }

    // ################
    // DELETE DIALOG
    // ################
    fun showDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = true)
    }

    fun hideDeleteDialog() {
        if (!_uiState.value.isDeleting) {
            _uiState.value = _uiState.value.copy(showDeleteDialog = false)
        }
    }

    // ##############
    // DELETE LISTING
    // ##############
    fun deleteListing(listingId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true)

            val result = repository.deleteListing(listingId)
            _uiState.value = _uiState.value.copy(isDeleting = false)

            if (result.isSuccess) {
                _events.emit(ListingDetailEvent.ShowToast("Listing deleted"))
                _events.emit(ListingDetailEvent.NavigateBack)
            } else {
                _events.emit(
                    ListingDetailEvent.ShowToast(
                        "Failed to delete listing: ${result.exceptionOrNull()?.message}"
                    )
                )
                _uiState.value = _uiState.value.copy(showDeleteDialog = false)
            }
        }
    }
}
