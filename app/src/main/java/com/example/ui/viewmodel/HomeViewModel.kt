package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.model.AgentPackage
import com.example.data.model.SocialPost
import com.example.data.model.TravelAgent
import com.example.data.repository.TravyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(private val repository: TravyRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _featuredPackages = MutableStateFlow<List<AgentPackage>>(emptyList())
    val featuredPackages: StateFlow<List<AgentPackage>> = _featuredPackages.asStateFlow()

    private val _topAgents = MutableStateFlow<List<TravelAgent>>(emptyList())
    val topAgents: StateFlow<List<TravelAgent>> = _topAgents.asStateFlow()

    private val _trendingPosts = MutableStateFlow<List<SocialPost>>(emptyList())
    val trendingPosts: StateFlow<List<SocialPost>> = _trendingPosts.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        _featuredPackages.value = repository.getPackages()
        _topAgents.value = repository.getAgents()
        _trendingPosts.value = repository.getSocialPosts()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}

class HomeViewModelFactory(private val repository: TravyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
