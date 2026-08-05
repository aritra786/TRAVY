package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.CartItemEntity
import com.example.data.local.WishlistEntity
import com.example.data.repository.TravyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CartAndItineraryViewModel(private val repository: TravyRepository) : ViewModel() {

    val cartItems: StateFlow<List<CartItemEntity>> = repository.getCartItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wishlistItems: StateFlow<List<WishlistEntity>> = repository.getWishlist()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _checkoutSuccess = MutableStateFlow(false)
    val checkoutSuccess: StateFlow<Boolean> = _checkoutSuccess.asStateFlow()

    val totalPrice: StateFlow<Double> = cartItems.map { items ->
        items.filter { !it.isBooked }.sumOf { it.price }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addToCart(
        itemType: String,
        title: String,
        subtitle: String,
        dateOrDuration: String,
        price: Double,
        imageUrl: String
    ) {
        viewModelScope.launch {
            repository.addToCart(
                itemType = itemType,
                title = title,
                subtitle = subtitle,
                dateOrDuration = dateOrDuration,
                price = price,
                imageUrl = imageUrl
            )
        }
    }

    fun removeItem(id: Long) {
        viewModelScope.launch {
            repository.deleteCartItem(id)
        }
    }

    fun checkoutEscrow() {
        viewModelScope.launch {
            _checkoutSuccess.value = true
        }
    }

    fun resetCheckoutState() {
        _checkoutSuccess.value = false
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }
}

class CartAndItineraryViewModelFactory(private val repository: TravyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartAndItineraryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CartAndItineraryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
