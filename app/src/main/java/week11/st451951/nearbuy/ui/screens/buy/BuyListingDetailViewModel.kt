package week11.st451951.nearbuy.ui.screens.buy

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
import week11.st451951.nearbuy.data.UsersRepository

/**
 * Buy listing detail UI state (buyer's view)
 */
data class BuyListingDetailUIState(
    val listing: Listing? = null,
    val sellerName: String? = null,
    val isLoading: Boolean = true
)

/**
 * Buy listing detail events
 */
sealed class BuyListingDetailEvent {
    data class ShowToast(val message: String) : BuyListingDetailEvent()
}

/**
 * ViewModel for BuyListingDetailScreen (buyer's view)
 *
 * Handles fetching listing details and seller information
 */
class BuyListingDetailViewModel(
    private val listingsRepository: ListingsRepository = ListingsRepository(),
    private val usersRepository: UsersRepository = UsersRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(BuyListingDetailUIState())
    val uiState: StateFlow<BuyListingDetailUIState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<BuyListingDetailEvent>()
    val events = _events.asSharedFlow()

    // #############
    // LOAD LISTING
    // #############
    fun loadListing(listingId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Fetch the listing
            val listingResult = listingsRepository.getListing(listingId)
            if (listingResult.isSuccess) {
                val listing = listingResult.getOrNull()
                _uiState.value = _uiState.value.copy(listing = listing)

                // Fetch the seller's name
                listing?.sellerId?.let { sellerId ->
                    val userResult = usersRepository.getUser(sellerId)
                    if (userResult.isSuccess) {
                        _uiState.value = _uiState.value.copy(
                            sellerName = userResult.getOrNull()?.displayName
                        )
                    }
                }
            } else {
                _events.emit(
                    BuyListingDetailEvent.ShowToast(
                        "Failed to load listing: ${listingResult.exceptionOrNull()?.message}"
                    )
                )
            }

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    // ###############
    // CONTACT SELLER
    // ###############
    fun contactSeller() {
        viewModelScope.launch {
            // TODO: Implement contact seller functionality
            _events.emit(BuyListingDetailEvent.ShowToast("Contact seller feature coming soon!"))
        }
    }
}
