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
import week11.st451951.nearbuy.data.ListingsRepository

/**
 * Buy screen UI state
 */
data class BuyUIState(
    val listings: List<Listing> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedCategory: Category? = null
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
 * Handles fetching and displaying all available listings
 */
class BuyViewModel(
    private val repository: ListingsRepository = ListingsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(BuyUIState())
    val uiState: StateFlow<BuyUIState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<BuyEvent>()
    val events = _events.asSharedFlow()

    init {
        loadListings()
    }

    private fun loadListings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                repository.getAllListings().collect { listings ->
                    _uiState.value = _uiState.value.copy(
                        listings = listings,
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

    fun selectCategory(category: Category) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        // TODO: Filter listings by category
    }

}
