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
 * Sell screen UI state
 */
data class SellUIState(
    val listings: List<Listing> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * Sell screen events
 */
sealed class SellEvent {
    data class ShowToast(val message: String) : SellEvent()
}

/**
 * ViewModel for SellScreen
 *
 * Handles fetching and displaying current user's listings
 */
class SellViewModel(
    private val repository: ListingsRepository = ListingsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SellUIState())
    val uiState: StateFlow<SellUIState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SellEvent>()
    val events = _events.asSharedFlow()

    init {
        loadUserListings()
    }

    private fun loadUserListings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                repository.getUserListings().collect { listings ->
                    _uiState.value = _uiState.value.copy(
                        listings = listings,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load your listings"
                )
                _events.emit(SellEvent.ShowToast(e.message ?: "Failed to load your listings"))
            }
        }
    }

}
