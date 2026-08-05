package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AiDayPlan
import com.example.data.model.AiTripPlan
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

// --- GEMINI REST REQUEST & RESPONSE DATA CLASSES ---

data class GeminiPart(
    val text: String
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiRequest(
    val contents: List<GeminiContent>
)

data class GeminiCandidate(
    val content: GeminiContent?
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val geminiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

class GeminiRemoteDataSource {

    private val moshi = RetrofitClient.moshi
    private val jsonAdapter = moshi.adapter(AiTripPlan::class.java)

    suspend fun generateItinerary(prompt: String): AiTripPlan {
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isBlank() || apiKey == "YOUR_GEMINI_API_KEY_HERE") {
            Log.w("GeminiRemoteDataSource", "GEMINI_API_KEY is not set. Using fallback curated plan.")
            return getFallbackPlan(prompt)
        }

        val systemInstruction = """
            You are Travy AI, an expert travel curator. Generate a travel itinerary JSON object for the user's prompt: '$prompt'.
            The JSON MUST strictly follow this exact schema:
            {
              "tripTitle": "Title of the Trip",
              "destination": "City, Country",
              "durationDays": 5,
              "estimatedTotalBudget": 1250.0,
              "summary": "Brief 2 sentence summary",
              "recommendedFlightSummary": "e.g. Flight JFK to DPS $650 round trip",
              "recommendedHotelSummary": "e.g. 4 nights at Uluwatu Cliff Resort $480",
              "dayByDay": [
                {
                  "dayNumber": 1,
                  "theme": "Arrival & Beach Sunset",
                  "morningActivity": "Airport transfer & Villa Check-in",
                  "afternoonActivity": "Relax at Seminyak Beach club",
                  "eveningActivity": "Jimbaran Seafood BBQ dinner",
                  "recommendedFood": "Grilled Snapper with Sambal",
                  "estimatedDayCost": 120.0
                }
              ]
            }
            Return ONLY raw JSON, with no markdown formatting or extra text.
        """.trimIndent()

        try {
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = "$systemInstruction\nUser Prompt: $prompt")))
                )
            )

            val response = RetrofitClient.geminiService.generateContent(apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (!responseText.isNullOrBlank()) {
                val cleanedJson = responseText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()

                val parsedPlan = jsonAdapter.fromJson(cleanedJson)
                if (parsedPlan != null) {
                    return parsedPlan
                }
            }
        } catch (e: Exception) {
            Log.e("GeminiRemoteDataSource", "Error generating itinerary with Gemini API: ${e.message}", e)
        }

        return getFallbackPlan(prompt)
    }

    private fun getFallbackPlan(prompt: String): AiTripPlan {
        val destination = when {
            prompt.contains("Bali", ignoreCase = true) -> "Bali, Indonesia"
            prompt.contains("Swiss", ignoreCase = true) || prompt.contains("Alps", ignoreCase = true) -> "Interlaken, Switzerland"
            prompt.contains("Japan", ignoreCase = true) || prompt.contains("Tokyo", ignoreCase = true) -> "Tokyo & Kyoto, Japan"
            prompt.contains("Amalfi", ignoreCase = true) || prompt.contains("Italy", ignoreCase = true) -> "Amalfi Coast, Italy"
            else -> "Tropical Island Escape"
        }

        return AiTripPlan(
            tripTitle = "Curated $destination Experience",
            destination = destination,
            durationDays = 5,
            estimatedTotalBudget = 1250.0,
            summary = "A customized 5-day itinerary based on your preferences ($prompt). Includes boutique stay, curated activities, and flight options.",
            recommendedFlightSummary = "Roundtrip Flights included ($580)",
            recommendedHotelSummary = "4 Nights Boutique Beach Villa ($520)",
            dayByDay = listOf(
                AiDayPlan(
                    dayNumber = 1,
                    theme = "Arrival & Coastal Welcome",
                    morningActivity = "Airport private pickup & check-in to villa",
                    afternoonActivity = "Leisure time at infinity pool",
                    eveningActivity = "Sunset cliffside dinner with local music",
                    recommendedFood = "Local Seafood Curry",
                    estimatedDayCost = 90.0
                ),
                AiDayPlan(
                    dayNumber = 2,
                    theme = "Cultural Landmarks & Waterfalls",
                    morningActivity = "Guided visit to sacred temple ruins",
                    afternoonActivity = "Trek to jungle waterfall and eco-cafe lunch",
                    eveningActivity = "Night market street food tour",
                    recommendedFood = "Traditional Satay Skewers",
                    estimatedDayCost = 110.0
                ),
                AiDayPlan(
                    dayNumber = 3,
                    theme = "Island Cruise & Snorkeling",
                    morningActivity = "Speedboat trip to coral reefs",
                    afternoonActivity = "Snorkeling with sea turtles & beach picnic",
                    eveningActivity = "Beachfront lounge DJ session",
                    recommendedFood = "Fresh Coconut & Grilled Fish",
                    estimatedDayCost = 140.0
                ),
                AiDayPlan(
                    dayNumber = 4,
                    theme = "Wellness & Spa Day",
                    morningActivity = "Sunrise yoga class on the cliff",
                    afternoonActivity = "2-hour flower bath and aromatherapy spa massage",
                    eveningActivity = "Fine dining seafood dinner",
                    recommendedFood = "Signature Lobster Thermidor",
                    estimatedDayCost = 160.0
                ),
                AiDayPlan(
                    dayNumber = 5,
                    theme = "Souvenirs & Departure",
                    morningActivity = "Artisan market shopping for handmade crafts",
                    afternoonActivity = "Farewell coffee at organic roastery",
                    eveningActivity = "Private transfer to airport for departure flight",
                    recommendedFood = "Artisanal Drip Coffee & Pastries",
                    estimatedDayCost = 60.0
                )
            )
        )
    }
}
