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
    val allListings: List<Listing> = emptyList(),
    val filteredListings: List<Listing> = emptyList(),
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
            try {
                val result = locationManager.getCurrentLocation()
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(userLocation = result.getOrNull())
                }
            } catch (_: SecurityException) {
                // Permission was revoked, ignore silently
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
                        allListings = sortedListings,
                        filteredListings = filterListings(sortedListings, _uiState.value.selectedCategory),
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
                Double.MAX_VALUE
            }
        }
    }

    private fun filterListings(listings: List<Listing>, category: Category?): List<Listing> {
        return if (category == null) {
            listings
        } else {
            listings.filter { it.category.equals(category.displayName, ignoreCase = true) }
        }
    }

    fun selectCategory(category: Category) {
        val newCategory = if (_uiState.value.selectedCategory == category) {
            null
        } else {
            category
        }

        _uiState.value = _uiState.value.copy(
            selectedCategory = newCategory,
            filteredListings = filterListings(_uiState.value.allListings, newCategory)
        )
    }

    fun clearCategoryFilter() {
        _uiState.value = _uiState.value.copy(
            selectedCategory = null,
            filteredListings = _uiState.value.allListings
        )
    }
}