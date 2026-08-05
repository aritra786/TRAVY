package com.example.data.repository

import com.example.data.local.CartItemEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.TravyDao
import com.example.data.local.WishlistEntity
import com.example.data.model.*
import com.example.data.remote.GeminiRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TravyRepository(
    private val dao: TravyDao,
    private val geminiSource: GeminiRemoteDataSource = GeminiRemoteDataSource()
) {

    // --- GEMINI AI PROMPT SEARCH ---
    suspend fun planTripWithAi(prompt: String): Result<AiTripPlan> {
        return runCatching { geminiSource.generateItinerary(prompt) }
    }

    // --- LOCAL ROOM DB READ/WRITE ---
    fun getCartItems(): Flow<List<CartItemEntity>> = dao.getAllCartItems()

    suspend fun addToCart(
        itemType: String,
        title: String,
        subtitle: String,
        dateOrDuration: String,
        price: Double,
        imageUrl: String,
        detailsJson: String = ""
    ) {
        val cartItem = CartItemEntity(
            itemType = itemType,
            title = title,
            subtitle = subtitle,
            dateOrDuration = dateOrDuration,
            price = price,
            imageUrl = imageUrl,
            detailsJson = detailsJson
        )
        dao.insertCartItem(cartItem)
    }

    suspend fun deleteCartItem(id: Long) {
        dao.deleteCartItem(id)
    }

    suspend fun clearCart() {
        dao.clearCart()
    }

    fun getWishlist(): Flow<List<WishlistEntity>> = dao.getAllWishlist()

    suspend fun isWishlisted(id: String): Boolean = dao.isWishlisted(id)

    suspend fun toggleWishlist(
        id: String,
        itemType: String,
        title: String,
        subtitle: String,
        imageUrl: String,
        price: Double
    ) {
        if (dao.isWishlisted(id)) {
            dao.deleteWishlist(id)
        } else {
            dao.insertWishlist(
                WishlistEntity(
                    id = id,
                    itemType = itemType,
                    title = title,
                    subtitle = subtitle,
                    imageUrl = imageUrl,
                    price = price
                )
            )
        }
    }

    fun getChatMessages(agentId: String): Flow<List<ChatMessageEntity>> = dao.getMessagesForAgent(agentId)

    suspend fun sendChatMessage(
        agentId: String,
        sender: String,
        messageText: String,
        timestampText: String = "Just now",
        quotePrice: Double? = null,
        quoteTitle: String? = null
    ) {
        dao.insertChatMessage(
            ChatMessageEntity(
                agentId = agentId,
                sender = sender,
                messageText = messageText,
                timestampText = timestampText,
                quotePrice = quotePrice,
                quoteTitle = quoteTitle
            )
        )
    }

    suspend fun approveEscrow(messageId: Long) {
        dao.markEscrowPaid(messageId)
    }

    // --- MOCK DATA SOURCES FOR MARKETPLACE, SOCIAL & BOOKINGS ---

    fun getAgents(): List<TravelAgent> = listOf(
        TravelAgent(
            id = "agt_01",
            name = "Wanderlust Expeditions",
            logoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
            isVerified = true,
            rating = 4.9,
            reviewsCount = 328,
            tagline = "Curated luxury beach retreats & island hopping packages",
            location = "Miami, FL & Bali, ID",
            specializedRegions = listOf("Southeast Asia", "Maldives", "Caribbean"),
            iataRegistrationNo = "IATA-9683402",
            activePackagesCount = 14,
            startingPrice = 899.0,
            description = "Wanderlust Expeditions is a premier IATA-verified travel consultancy specializing in bespoke tropical itineraries, honeymoon packages, and VIP island transfers with 24/7 dedicated concierge support.",
            responseTime = "< 10 mins",
            completedTrips = 1420
        ),
        TravelAgent(
            id = "agt_02",
            name = "Nomad Alpine Treks",
            logoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
            isVerified = true,
            rating = 4.8,
            reviewsCount = 215,
            tagline = "High altitude mountain treks, ski packages & wilderness lodges",
            location = "Interlaken, Switzerland",
            specializedRegions = listOf("Swiss Alps", "Himalayas", "Patagonia"),
            iataRegistrationNo = "IATA-7740192",
            activePackagesCount = 9,
            startingPrice = 1150.0,
            description = "Expert mountain guides and certified adventure planners providing safety-first trekking, luxury alpine chalets, and scenic railway passes.",
            responseTime = "< 20 mins",
            completedTrips = 890
        ),
        TravelAgent(
            id = "agt_03",
            name = "Kyoto Zen Journeys",
            logoUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200",
            isVerified = true,
            rating = 4.95,
            reviewsCount = 512,
            tagline = "Authentic cultural immersions, Ryokan stays & gourmet sushi tours",
            location = "Kyoto & Tokyo, Japan",
            specializedRegions = listOf("Japan", "South Korea", "Taiwan"),
            iataRegistrationNo = "IATA-8839201",
            activePackagesCount = 18,
            startingPrice = 1290.0,
            description = "Specializing in traditional Japanese hospitality, private tea ceremonies, Bullet Train passes, and access to exclusive Kaiseki dining.",
            responseTime = "< 5 mins",
            completedTrips = 2100
        )
    )

    fun getPackages(): List<AgentPackage> = listOf(
        AgentPackage(
            id = "pkg_01",
            agentId = "agt_01",
            agentName = "Wanderlust Expeditions",
            agentLogo = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
            isAgentVerified = true,
            title = "Bali Tropical Bliss: Villa Stay & Nusa Penida Cruise",
            destination = "Bali, Indonesia",
            durationDays = 6,
            durationNights = 5,
            pricePerPerson = 999.0,
            rating = 4.9,
            reviewsCount = 142,
            inclusions = listOf("Roundtrip Flights", "Private Pool Villa", "Daily Gourmet Breakfast", "Nusa Penida Cruise", "Airport Transfers", "24/7 Concierge"),
            exclusions = listOf("Personal Expenses", "Travel Insurance"),
            itinerary = listOf(
                DayItinerary(1, "Airport Arrival & Private Villa Check-In", "Welcome drink upon arrival at Seminyak Private Pool Villa. Evening relaxed beach dinner at Jimbaran Bay.", listOf("VIP Airport Pickup", "Welcome Massage", "Seafood Sunset Dinner")),
                DayItinerary(2, "Ubud Cultural Tour & Rice Terraces", "Guided tour of Tegallalang Rice Terraces, Sacred Monkey Forest, and jungle swing experience.", listOf("Rice Terrace Walk", "Jungle Swing", "Traditional Coffee Tasting")),
                DayItinerary(3, "Speedboat Cruise to Nusa Penida Island", "Full day island cruise visiting Kelingking T-Rex beach, Broken Beach, and snorkeling with Manta Rays.", listOf("Speedboat Cruise", "Manta Ray Snorkeling", "Cliffside Photo Ops")),
                DayItinerary(4, "Waterfalls & Temple Heritage Safari", "Visit Lempuyang Heaven's Gate temple and Tirta Gangga water palace.", listOf("Temple Heritage Pass", "Waterfall Hike")),
                DayItinerary(5, "Spa Wellness Day & Uluwatu Kecak Dance", "Morning Balinese herbal spa treatment followed by cliffside sunset Kecak fire dance.", listOf("2-Hour Spa", "Uluwatu Sunset Show")),
                DayItinerary(6, "Souvenir Walk & Departure", "Breakfast at villa, boutique shopping in Seminyak, and transfer to Denpasar Airport.", listOf("Private Airport Drop-off"))
            ),
            heroImageUrl = "https://images.unsplash.com/photo-1537996194471-e657df975ab4?w=800",
            category = "Honeymoon",
            nextDepartureDate = "Sep 15, 2026"
        ),
        AgentPackage(
            id = "pkg_02",
            agentId = "agt_02",
            agentName = "Nomad Alpine Treks",
            agentLogo = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
            isAgentVerified = true,
            title = "Swiss Alps Magic: Interlaken, Jungfrau & Glacier Express",
            destination = "Interlaken, Switzerland",
            durationDays = 5,
            durationNights = 4,
            pricePerPerson = 1390.0,
            rating = 4.85,
            reviewsCount = 98,
            inclusions = listOf("Swiss Travel Pass (Unlimited Rail)", "4-Star Mountain Lodge", "Jungfraujoch Ticket", "Glacier Express Seat", "Daily Breakfast"),
            exclusions = listOf("International Flights", "Equipment Rental"),
            itinerary = listOf(
                DayItinerary(1, "Arrival in Zurich & Scenic Train to Interlaken", "Board Swiss Rail to Interlaken with panoramic mountain views. Check into alpine chalet.", listOf("Swiss Rail Pass Activation", "Welcome Alpine Fondue Dinner")),
                DayItinerary(2, "Jungfraujoch - Top of Europe Excursion", "Cogwheel train ride up to Europe's highest railway station at 3,454m. Ice Palace tour.", listOf("Cogwheel Railway", "Sphinx Observatory")),
                DayItinerary(3, "Glacier Express Scenic Panoramic Ride", "Board the iconic Glacier Express panoramic train through snowy gorges and viaducts.", listOf("First Class Rail Car", "Audio Guide")),
                DayItinerary(4, "Grindelwald First Cliff Walk & Lake Brienz", "Thrill walk on First Cliff walk, followed by Lake Brienz steamboat cruise.", listOf("Cliff Walk", "Lake Cruise")),
                DayItinerary(5, "Zurich City Walk & Departure", "Morning train back to Zurich for souvenir shopping and departure.", listOf("Airport Rail Transfer"))
            ),
            heroImageUrl = "https://images.unsplash.com/photo-1530122037265-a5f1f91d3b99?w=800",
            category = "Adventure",
            nextDepartureDate = "Oct 01, 2026"
        ),
        AgentPackage(
            id = "pkg_03",
            agentId = "agt_03",
            agentName = "Kyoto Zen Journeys",
            agentLogo = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200",
            isAgentVerified = true,
            title = "Japan Golden Route: Tokyo Lights, Fuji & Kyoto Temples",
            destination = "Tokyo & Kyoto, Japan",
            durationDays = 7,
            durationNights = 6,
            pricePerPerson = 1590.0,
            rating = 4.98,
            reviewsCount = 210,
            inclusions = listOf("7-Day Shinkansen Bullet Train Pass", "Traditional Ryokan with Onsen Hot Springs", "Private Tea Ceremony", "Tokyo Foodie Tour", "All Hotel Transfers"),
            exclusions = listOf("Visa Fees"),
            itinerary = listOf(
                DayItinerary(1, "Tokyo Arrival & Shibuya Sky Nightview", "Check into Shinjuku hotel. Evening 360-degree views from Shibuya Sky observatory.", listOf("Airport Express Train", "Shibuya Sky Pass")),
                DayItinerary(2, "Tsukiji Market & Senso-ji Temple", "Fresh sushi breakfast at Tsukiji Outer Market and historic tour of Asakusa.", listOf("Guided Sushi Tour", "Kimono Rental")),
                DayItinerary(3, "Mount Fuji & Hakone Onsen Resort", "Romancecar train to Hakone. Cable car ride over volcanic valleys and hot spring bath.", listOf("Hakone Ropeway", "Private Onsen Bath")),
                DayItinerary(4, "Shinkansen Bullet Train to Kyoto", "Hop on the 300km/h Shinkansen to Kyoto. Check into luxury Ryokan.", listOf("Shinkansen Green Car", "Traditional Kaiseki Dinner")),
                DayItinerary(5, "Fushimi Inari Torii Gates & Arashiyama Bamboo", "Early morning walk through thousands of vermilion Torii gates and Bamboo Grove.", listOf("Torii Gate Walk", "Rickshaw Tour")),
                DayItinerary(6, "Gion Geisha District & Tea Ceremony", "Learn traditional Japanese tea art from a tea master in Gion.", listOf("Tea Ceremony Masterclass", "Gion Evening Walk")),
                DayItinerary(7, "Osaka Castle & Departure", "Short train to Osaka for street food tasting at Dotonbori before Kansai Airport drop-off.", listOf("Osaka Food Safari", "Airport Transfer"))
            ),
            heroImageUrl = "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?w=800",
            category = "Luxury",
            nextDepartureDate = "Sep 20, 2026"
        )
    )

    fun getSocialPosts(): List<SocialPost> = listOf(
        SocialPost(
            id = "post_01",
            authorName = "Elena Rostova",
            authorHandle = "@elena.travels",
            authorAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
            isAgent = false,
            mediaType = "REEL",
            mediaUrl = "https://images.unsplash.com/photo-1537996194471-e657df975ab4?w=800",
            destinationTag = "Bali, Indonesia",
            hotelOrSpotTag = "Nusa Penida T-Rex Cliff",
            caption = "Nothing prepares you for the sheer scale of Nusa Penida! Booked this exact itinerary through @WanderlustExpeditions and it was 10/10 🌊✨",
            likesCount = 4280,
            commentsCount = 312,
            isLiked = false,
            isSaved = false,
            linkedPackageId = "pkg_01",
            linkedPackageTitle = "Bali Tropical Bliss Package",
            linkedPackagePrice = 999.0
        ),
        SocialPost(
            id = "post_02",
            authorName = "Wanderlust Expeditions",
            authorHandle = "@wanderlust_exp",
            authorAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
            isAgent = true,
            agentId = "agt_01",
            mediaType = "IMAGE",
            mediaUrl = "https://images.unsplash.com/photo-1512100356356-de1b84283e18?w=800",
            destinationTag = "Maldives Islands",
            hotelOrSpotTag = "Overwater Sunset Villa",
            caption = "Tag someone you'd bring to this overwater hammock! Our September Maldives group packages are now live with early bird 20% off. 🏝️✨",
            likesCount = 8920,
            commentsCount = 540,
            isLiked = true,
            isSaved = true,
            linkedPackageId = "pkg_01",
            linkedPackageTitle = "Maldives All-Inclusive Escape",
            linkedPackagePrice = 1290.0
        ),
        SocialPost(
            id = "post_03",
            authorName = "Marcello Rossi",
            authorHandle = "@marcello_hikes",
            authorAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
            isAgent = false,
            mediaType = "REEL",
            mediaUrl = "https://images.unsplash.com/photo-1530122037265-a5f1f91d3b99?w=800",
            destinationTag = "Jungfraujoch, Switzerland",
            hotelOrSpotTag = "Glacier Express Train",
            caption = "Riding the Glacier Express through the Swiss Alps felt like being inside a snowglobe 🚂❄️ Unforgettable experience!",
            likesCount = 12400,
            commentsCount = 820,
            isLiked = false,
            isSaved = false,
            linkedPackageId = "pkg_02",
            linkedPackageTitle = "Swiss Alps Magic 5-Day Rail Tour",
            linkedPackagePrice = 1390.0
        )
    )

    fun getFlightOptions(): List<FlightOption> = listOf(
        FlightOption(
            id = "flt_01",
            airlineName = "SkyWings Airlines",
            airlineLogo = "✈️",
            flightNumber = "SW-408",
            originCode = "JFK",
            originCity = "New York",
            destCode = "DPS",
            destCity = "Bali",
            departureTime = "08:30 AM",
            arrivalTime = "06:15 PM (+1d)",
            durationText = "19h 45m",
            stopsCount = 1,
            price = 480.0
        ),
        FlightOption(
            id = "flt_02",
            airlineName = "AeroExpress Intl",
            airlineLogo = "🛫",
            flightNumber = "AE-882",
            originCode = "JFK",
            originCity = "New York",
            destCode = "DPS",
            destCity = "Bali",
            departureTime = "11:15 AM",
            arrivalTime = "08:40 PM (+1d)",
            durationText = "21h 25m",
            stopsCount = 1,
            price = 420.0
        ),
        FlightOption(
            id = "flt_03",
            airlineName = "Swiss Air Express",
            airlineLogo = "🇨🇭",
            flightNumber = "LX-18",
            originCode = "JFK",
            originCity = "New York",
            destCode = "ZRH",
            destCity = "Zurich",
            departureTime = "06:10 PM",
            arrivalTime = "07:50 AM",
            durationText = "7h 40m",
            stopsCount = 0,
            price = 610.0
        )
    )

    fun getHotelOptions(): List<HotelOption> = listOf(
        HotelOption(
            id = "htl_01",
            name = "The Grand Azure Seminyak Resort",
            location = "Seminyak Beach, Bali",
            rating = 4.9,
            reviewsCount = 420,
            imageUrl = "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800",
            pricePerNight = 145.0,
            amenities = listOf("Infinity Pool", "Free WiFi", "Spa & Wellness", "Beachfront", "Breakfast Included")
        ),
        HotelOption(
            id = "htl_02",
            name = "Interlaken Alpine Lodge & Spa",
            location = "Central Interlaken, Switzerland",
            rating = 4.8,
            reviewsCount = 280,
            imageUrl = "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800",
            pricePerNight = 210.0,
            amenities = listOf("Mountain View", "Sauna & Hot Tub", "Ski Storage", "Free Swiss Rail Pass")
        ),
        HotelOption(
            id = "htl_03",
            name = "Kyoto Zen Heritage Ryokan",
            location = "Gion District, Kyoto",
            rating = 4.95,
            reviewsCount = 310,
            imageUrl = "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=800",
            pricePerNight = 260.0,
            amenities = listOf("Private Onsen", "Traditional Kaiseki Dinner", "Garden View", "Tatami Rooms")
        )
    )

    fun getCabOptions(): List<CabOption> = listOf(
        CabOption(
            id = "cab_01",
            vehicleType = "Airport Transfer SUV",
            vehicleModel = "Toyota Innova Crysta / Similar",
            capacitySeats = 6,
            price = 35.0,
            category = "Airport Transfer",
            supplierName = "Travy Chauffeur Express",
            rating = 4.9
        ),
        CabOption(
            id = "cab_02",
            vehicleType = "Full Day Private Chauffeur",
            vehicleModel = "Mercedes E-Class Sedan",
            capacitySeats = 4,
            price = 95.0,
            category = "Full Day Sightseeing (10 Hours)",
            supplierName = "Bali Premium Fleet",
            rating = 4.95
        ),
        CabOption(
            id = "cab_03",
            vehicleType = "Outstation Intercity Cab",
            vehicleModel = "Hyundai SUV 4WD",
            capacitySeats = 5,
            price = 80.0,
            category = "Intercity Transfer",
            supplierName = "Alpine Express Cabs",
            rating = 4.8
        )
    )

    fun getTrainOptions(): List<TrainOption> = listOf(
        TrainOption(
            id = "trn_01",
            trainName = "Swiss Express Glacier Pass",
            trainNumber = "EXP-901",
            departureCity = "Zurich",
            arrivalCity = "Interlaken",
            departureTime = "09:00 AM",
            arrivalTime = "10:55 AM",
            durationText = "1h 55m",
            classType = "1st Class Panoramic",
            price = 65.0,
            seatStatus = "Available"
        ),
        TrainOption(
            id = "trn_02",
            trainName = "Shinkansen Nozomi Bullet",
            trainNumber = "NZ-214",
            departureCity = "Tokyo",
            arrivalCity = "Kyoto",
            departureTime = "10:00 AM",
            arrivalTime = "12:15 PM",
            durationText = "2h 15m",
            classType = "Green Car Reserved",
            price = 110.0,
            seatStatus = "Available"
        )
    )
}
