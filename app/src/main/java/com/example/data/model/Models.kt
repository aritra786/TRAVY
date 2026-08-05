package com.example.data.model

// --- MARKETPLACE MODELS ---

data class TravelAgent(
    val id: String,
    val name: String,
    val logoUrl: String,
    val isVerified: Boolean = true,
    val rating: Double,
    val reviewsCount: Int,
    val tagline: String,
    val location: String,
    val specializedRegions: List<String>,
    val iataRegistrationNo: String,
    val activePackagesCount: Int,
    val startingPrice: Double,
    val description: String,
    val responseTime: String = "< 15 mins",
    val completedTrips: Int
)

data class DayItinerary(
    val dayNumber: Int = 1,
    val title: String = "",
    val description: String = "",
    val activities: List<String> = emptyList(),
    val mealPlan: String = "Breakfast included",
    val stayName: String = "4-Star Hotel"
)

data class AgentPackage(
    val id: String,
    val agentId: String,
    val agentName: String,
    val agentLogo: String,
    val isAgentVerified: Boolean,
    val title: String,
    val destination: String,
    val durationDays: Int,
    val durationNights: Int,
    val pricePerPerson: Double,
    val rating: Double,
    val reviewsCount: Int,
    val inclusions: List<String>, // e.g. "Flights", "4-Star Hotel", "Daily Breakfast", "Transfers", "Guided Tour"
    val exclusions: List<String>,
    val itinerary: List<DayItinerary>,
    val heroImageUrl: String,
    val category: String, // "Honeymoon", "Adventure", "Luxury", "Group Tour", "Budget"
    val nextDepartureDate: String
)

// --- SOCIAL FEED / REEL MODELS ---

data class SocialPost(
    val id: String,
    val authorName: String,
    val authorHandle: String,
    val authorAvatarUrl: String,
    val isAgent: Boolean = false,
    val agentId: String? = null,
    val mediaType: String, // "IMAGE" or "REEL"
    val mediaUrl: String,
    val destinationTag: String,
    val hotelOrSpotTag: String? = null,
    val caption: String,
    val likesCount: Int,
    val commentsCount: Int,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val linkedPackageId: String? = null,
    val linkedPackageTitle: String? = null,
    val linkedPackagePrice: Double? = null,
    val musicTrack: String? = "Original Audio - Wanderlust Vibes"
)

data class SocialComment(
    val id: String,
    val postId: String,
    val authorName: String,
    val avatarUrl: String,
    val text: String,
    val timeAgo: String
)

// --- DIY BOOKING MODELS ---

data class FlightOption(
    val id: String,
    val airlineName: String,
    val airlineLogo: String,
    val flightNumber: String,
    val originCode: String,
    val originCity: String,
    val destCode: String,
    val destCity: String,
    val departureTime: String,
    val arrivalTime: String,
    val durationText: String,
    val stopsCount: Int,
    val price: Double,
    val baggage: String = "7kg Cabin + 15kg Check-in"
)

data class HotelOption(
    val id: String,
    val name: String,
    val location: String,
    val rating: Double,
    val reviewsCount: Int,
    val imageUrl: String,
    val pricePerNight: Double,
    val amenities: List<String>,
    val roomType: String = "Deluxe King Room",
    val distanceToCenter: String = "0.8 km from center"
)

data class CabOption(
    val id: String,
    val vehicleType: String, // "Sedan", "SUV", "Luxury", "Outstation"
    val vehicleModel: String,
    val capacitySeats: Int,
    val price: Double,
    val category: String, // "Airport Transfer", "Outstation 2 Days", "Hourly Rental"
    val supplierName: String,
    val rating: Double,
    val estimatedTime: String = "Available in 5 mins"
)

data class TrainOption(
    val id: String,
    val trainName: String,
    val trainNumber: String,
    val departureCity: String,
    val arrivalCity: String,
    val departureTime: String,
    val arrivalTime: String,
    val durationText: String,
    val classType: String, // "AC 1st Class", "AC 2-Tier", "Executive Chair"
    val price: Double,
    val seatStatus: String = "Available (WL 0)"
)

// --- AI ITINERARY MODELS ---

data class AiTripPlan(
    val tripTitle: String = "",
    val destination: String = "",
    val durationDays: Int = 1,
    val estimatedTotalBudget: Double = 0.0,
    val summary: String = "",
    val dayByDay: List<AiDayPlan> = emptyList(),
    val recommendedFlightSummary: String? = null,
    val recommendedHotelSummary: String? = null,
    val bestMatchedAgentPackageId: String? = null
)

data class AiDayPlan(
    val dayNumber: Int = 1,
    val theme: String = "",
    val morningActivity: String = "",
    val afternoonActivity: String = "",
    val eveningActivity: String = "",
    val recommendedFood: String = "",
    val estimatedDayCost: Double = 0.0
)
