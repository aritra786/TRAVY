package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AgentPackage
import com.example.data.model.TravelAgent
import com.example.data.repository.TravyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MarketplaceViewModel(private val repository: TravyRepository) : ViewModel() {

    private val _agents = MutableStateFlow<List<TravelAgent>>(emptyList())
    val agents: StateFlow<List<TravelAgent>> = _agents.asStateFlow()

    private val _packages = MutableStateFlow<List<AgentPackage>>(emptyList())
    val packages: StateFlow<List<AgentPackage>> = _packages.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _quoteRequestSuccessMsg = MutableStateFlow<String?>(null)
    val quoteRequestSuccessMsg: StateFlow<String?> = _quoteRequestSuccessMsg.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _agents.value = repository.getAgents()
        _packages.value = repository.getPackages()
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun submitCustomQuoteRequest(
        agentId: String,
        destination: String,
        travelDates: String,
        travelersCount: Int,
        budgetPerPerson: Double,
        notes: String
    ) {
        viewModelScope.launch {
            val agent = _agents.value.find { it.id == agentId } ?: repository.getAgents().first()
            repository.sendChatMessage(
                agentId = agentId,
                sender = "USER",
                messageText = "📥 Custom Quote Request:\nDestination: $destination\nDates: $travelDates\nTravelers: $travelersCount\nBudget/Person: $$budgetPerPerson\nNotes: $notes"
            )

            // Simulate Agent Automated Quote Response
            repository.sendChatMessage(
                agentId = agentId,
                sender = "AGENT",
                messageText = "Hello! Thanks for reaching out to ${agent.name}. I've reviewed your request for $destination. Here is an exclusive customized package quote for $travelersCount travelers!",
                quotePrice = budgetPerPerson * travelersCount,
                quoteTitle = "$destination Special Customized Group Package"
            )

            _quoteRequestSuccessMsg.value = "Quote request submitted to ${agent.name}! Check Agent Chat."
        }
    }

    fun addPackageToCart(pkg: AgentPackage) {
        viewModelScope.launch {
            repository.addToCart(
                itemType = "PACKAGE",
                title = pkg.title,
                subtitle = "Organized by ${pkg.agentName} • ${pkg.destination}",
                dateOrDuration = "${pkg.durationDays} Days / ${pkg.durationNights} Nights",
                price = pkg.pricePerPerson,
                imageUrl = pkg.heroImageUrl,
                detailsJson = pkg.inclusions.joinToString(", ")
            )
            _quoteRequestSuccessMsg.value = "Package added to Cart! 🛒"
        }
    }

    fun clearToastMessage() {
        _quoteRequestSuccessMsg.value = null
    }
}

class MarketplaceViewModelFactory(private val repository: TravyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarketplaceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MarketplaceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
