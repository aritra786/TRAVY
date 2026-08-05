package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.TravyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class BookingType { FLIGHTS, HOTELS, CABS, TRAINS }

class DiyBookingViewModel(private val repository: TravyRepository) : ViewModel() {

    private val _selectedTab = MutableStateFlow(BookingType.FLIGHTS)
    val selectedTab: StateFlow<BookingType> = _selectedTab.asStateFlow()

    private val _flights = MutableStateFlow<List<FlightOption>>(emptyList())
    val flights: StateFlow<List<FlightOption>> = _flights.asStateFlow()

    private val _hotels = MutableStateFlow<List<HotelOption>>(emptyList())
    val hotels: StateFlow<List<HotelOption>> = _hotels.asStateFlow()

    private val _cabs = MutableStateFlow<List<CabOption>>(emptyList())
    val cabs: StateFlow<List<CabOption>> = _cabs.asStateFlow()

    private val _trains = MutableStateFlow<List<TrainOption>>(emptyList())
    val trains: StateFlow<List<TrainOption>> = _trains.asStateFlow()

    private val _originCity = MutableStateFlow("New York (JFK)")
    val originCity: StateFlow<String> = _originCity.asStateFlow()

    private val _destCity = MutableStateFlow("Bali (DPS)")
    val destCity: StateFlow<String> = _destCity.asStateFlow()

    private val _toastMsg = MutableStateFlow<String?>(null)
    val toastMsg: StateFlow<String?> = _toastMsg.asStateFlow()

    init {
        loadBookingData()
    }

    private fun loadBookingData() {
        _flights.value = repository.getFlightOptions()
        _hotels.value = repository.getHotelOptions()
        _cabs.value = repository.getCabOptions()
        _trains.value = repository.getTrainOptions()
    }

    fun selectTab(type: BookingType) {
        _selectedTab.value = type
    }

    fun updateSearch(origin: String, dest: String) {
        _originCity.value = origin
        _destCity.value = dest
    }

    fun addFlightToCart(flight: FlightOption) {
        viewModelScope.launch {
            repository.addToCart(
                itemType = "FLIGHT",
                title = "${flight.airlineName} (${flight.flightNumber})",
                subtitle = "${flight.originCode} ✈️ ${flight.destCode} • ${flight.departureTime}",
                dateOrDuration = flight.durationText,
                price = flight.price,
                imageUrl = "https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=800"
            )
            _toastMsg.value = "Flight ${flight.flightNumber} added to Cart! 🛫"
        }
    }

    fun addHotelToCart(hotel: HotelOption) {
        viewModelScope.launch {
            repository.addToCart(
                itemType = "HOTEL",
                title = hotel.name,
                subtitle = "${hotel.roomType} • ${hotel.location}",
                dateOrDuration = "1 Night",
                price = hotel.pricePerNight,
                imageUrl = hotel.imageUrl
            )
            _toastMsg.value = "Hotel ${hotel.name} added to Cart! 🏨"
        }
    }

    fun addCabToCart(cab: CabOption) {
        viewModelScope.launch {
            repository.addToCart(
                itemType = "CAB",
                title = "${cab.vehicleType} - ${cab.vehicleModel}",
                subtitle = "${cab.category} • ${cab.supplierName}",
                dateOrDuration = cab.estimatedTime,
                price = cab.price,
                imageUrl = "https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?w=800"
            )
            _toastMsg.value = "Cab ${cab.vehicleType} added to Cart! 🚗"
        }
    }

    fun addTrainToCart(train: TrainOption) {
        viewModelScope.launch {
            repository.addToCart(
                itemType = "TRAIN",
                title = "${train.trainName} (${train.trainNumber})",
                subtitle = "${train.departureCity} 🚆 ${train.arrivalCity} • ${train.classType}",
                dateOrDuration = train.durationText,
                price = train.price,
                imageUrl = "https://images.unsplash.com/photo-1474487548417-781cb71495f3?w=800"
            )
            _toastMsg.value = "Train ticket added to Cart! 🚆"
        }
    }

    fun clearToast() {
        _toastMsg.value = null
    }
}

class DiyBookingViewModelFactory(private val repository: TravyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiyBookingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiyBookingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
