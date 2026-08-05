package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AiTripPlan
import com.example.data.repository.TravyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AiCuratorUiState {
    object Idle : AiCuratorUiState()
    object Loading : AiCuratorUiState()
    data class Success(val plan: AiTripPlan) : AiCuratorUiState()
    data class Error(val message: String) : AiCuratorUiState()
}

class AiCuratorViewModel(private val repository: TravyRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AiCuratorUiState>(AiCuratorUiState.Idle)
    val uiState: StateFlow<AiCuratorUiState> = _uiState.asStateFlow()

    private val _promptInput = MutableStateFlow("")
    val promptInput: StateFlow<String> = _promptInput.asStateFlow()

    private val _addedToCartMessage = MutableStateFlow<String?>(null)
    val addedToCartMessage: StateFlow<String?> = _addedToCartMessage.asStateFlow()

    fun onPromptChanged(newText: String) {
        _promptInput.value = newText
    }

    fun generateItinerary(promptOverride: String? = null) {
        val query = promptOverride ?: _promptInput.value
        if (query.isBlank()) return

        _uiState.value = AiCuratorUiState.Loading

        viewModelScope.launch {
            repository.planTripWithAi(query)
                .onSuccess { plan ->
                    _uiState.value = AiCuratorUiState.Success(plan)
                }
                .onFailure { error ->
                    _uiState.value = AiCuratorUiState.Error(error.message ?: "Failed to generate AI trip plan")
                }
        }
    }

    fun addWholeItineraryToCart(plan: AiTripPlan) {
        viewModelScope.launch {
            repository.addToCart(
                itemType = "PACKAGE",
                title = plan.tripTitle,
                subtitle = "${plan.destination} • ${plan.durationDays} Days AI Curated Trip",
                dateOrDuration = "${plan.durationDays} Days",
                price = plan.estimatedTotalBudget,
                imageUrl = "https://images.unsplash.com/photo-1537996194471-e657df975ab4?w=800",
                detailsJson = plan.summary
            )
            _addedToCartMessage.value = "Full AI Itinerary added to Cart! 🛒"
        }
    }

    fun clearToastMessage() {
        _addedToCartMessage.value = null
    }
}

class AiCuratorViewModelFactory(private val repository: TravyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AiCuratorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AiCuratorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
