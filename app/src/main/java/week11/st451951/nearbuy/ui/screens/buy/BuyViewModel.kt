package week11.st451951.nearbuy.ui.screens.buy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import week11.st451951.nearbuy.data.Category
import week11.st451951.nearbuy.data.Listing
import week11.st451951.nearbuy.data.ListingLocation
import week11.st451951.nearbuy.data.ListingsRepository
import week11.st451951.nearbuy.data.LocationManager

/**
 * Buy screen UI state
 */
data class BuyUIState(
    val listings: List<Listing> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedCategory: Category? = null,
    val userLocation: ListingLocation? = null
)

/**
 * Buy screen events
 */
sealed class BuyEvent {
    data class ShowToast(val message: String) : BuyEvent()
}

/**
 * ViewModel for BuyScreen
 *
 * Handles fetching and displaying all available listings sorted by distance
 */
class BuyViewModel(
    private val repository: ListingsRepository = ListingsRepository(),
    private val locationManager: LocationManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(BuyUIState())
    val uiState: StateFlow<BuyUIState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<BuyEvent>()
    val events = _events.asSharedFlow()

    init {
        getUserLocation()
        loadListings()
    }

    private fun getUserLocation() {
        if (locationManager == null || !locationManager.hasLocationPermission()) {
            return
        }

        viewModelScope.launch {
            val result = locationManager.getCurrentLocation()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(userLocation = result.getOrNull())
            }
        }
    }

    private fun loadListings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                repository.getAllListings().collect { listings ->
                    val sortedListings = sortListingsByDistance(listings)
                    _uiState.value = _uiState.value.copy(
                        listings = sortedListings,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load listings"
                )
                _events.emit(BuyEvent.ShowToast(e.message ?: "Failed to load listings"))
            }
        }
    }

    private fun sortListingsByDistance(listings: List<Listing>): List<Listing> {
        val userLoc = _uiState.value.userLocation ?: return listings

        return listings.sortedBy { listing ->
            if (listing.location.latitude != 0.0 && listing.location.longitude != 0.0) {
                LocationManager.calculateDistance(
                    userLoc.latitude,
                    userLoc.longitude,
                    listing.location.latitude,
                    listing.location.longitude
                )
            } else {
                Double.MAX_VALUE // Put listings without location at the end
            }
        }
    }

    fun selectCategory(category: Category) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        // TODO: Filter listings by category
    }

}
