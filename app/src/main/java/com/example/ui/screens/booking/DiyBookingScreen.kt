package com.example.ui.screens.booking

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.components.RatingChip
import com.example.ui.viewmodel.BookingType
import com.example.ui.viewmodel.DiyBookingViewModel

@Composable
fun DiyBookingScreen(
    viewModel: DiyBookingViewModel,
    onNavigateToCart: () -> Unit
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val originCity by viewModel.originCity.collectAsState()
    val destCity by viewModel.destCity.collectAsState()

    val flights by viewModel.flights.collectAsState()
    val hotels by viewModel.hotels.collectAsState()
    val cabs by viewModel.cabs.collectAsState()
    val trains by viewModel.trains.collectAsState()

    val toastMsg by viewModel.toastMsg.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(toastMsg) {
        toastMsg?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 90.dp)
    ) {
        // --- HEADER & SEARCH CONTROLS ---
        Surface(shadowElevation = 2.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "'Go Solo' DIY Booking Engine",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Book individual flights, hotels, cabs & rail with live inventory",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = originCity,
                        onValueChange = { viewModel.updateSearch(it, destCity) },
                        label = { Text("Origin") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = destCity,
                        onValueChange = { viewModel.updateSearch(originCity, it) },
                        label = { Text("Destination") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == BookingType.FLIGHTS,
                        onClick = { viewModel.selectTab(BookingType.FLIGHTS) },
                        text = { Text("Flights", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == BookingType.HOTELS,
                        onClick = { viewModel.selectTab(BookingType.HOTELS) },
                        text = { Text("Hotels", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == BookingType.CABS,
                        onClick = { viewModel.selectTab(BookingType.CABS) },
                        text = { Text("Cabs", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == BookingType.TRAINS,
                        onClick = { viewModel.selectTab(BookingType.TRAINS) },
                        text = { Text("Trains", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        // --- RESULTS LIST ---
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            when (selectedTab) {
                BookingType.FLIGHTS -> {
                    items(flights) { flight ->
                        FlightCard(flight = flight, onAddToCart = { viewModel.addFlightToCart(flight) })
                    }
                }
                BookingType.HOTELS -> {
                    items(hotels) { hotel ->
                        HotelCard(hotel = hotel, onAddToCart = { viewModel.addHotelToCart(hotel) })
                    }
                }
                BookingType.CABS -> {
                    items(cabs) { cab ->
                        CabCard(cab = cab, onAddToCart = { viewModel.addCabToCart(cab) })
                    }
                }
                BookingType.TRAINS -> {
                    items(trains) { train ->
                        TrainCard(train = train, onAddToCart = { viewModel.addTrainToCart(train) })
                    }
                }
            }
        }
    }
}

@Composable
fun FlightCard(flight: FlightOption, onAddToCart: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("flight_card_${flight.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = flight.airlineLogo, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = flight.airlineName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Text(text = flight.flightNumber, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = flight.departureTime, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = flight.originCode, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = flight.durationText, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Icon(imageVector = Icons.Default.FlightTakeoff, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Text(text = if (flight.stopsCount == 0) "Direct" else "${flight.stopsCount} Stop", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = flight.arrivalTime, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = flight.destCode, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🧳 ${flight.baggage}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "\$${flight.price.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(onClick = onAddToCart, shape = RoundedCornerShape(10.dp)) {
                        Text("Add Flight")
                    }
                }
            }
        }
    }
}

@Composable
fun HotelCard(hotel: HotelOption, onAddToCart: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hotel_card_${hotel.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                AsyncImage(
                    model = hotel.imageUrl,
                    contentDescription = hotel.name,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                RatingChip(
                    rating = hotel.rating,
                    reviewsCount = hotel.reviewsCount,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = hotel.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "📍 ${hotel.location} • ${hotel.distanceToCenter}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = hotel.roomType, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "\$${hotel.pricePerNight.toInt()} / night", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    }

                    Button(onClick = onAddToCart, shape = RoundedCornerShape(10.dp)) {
                        Text("Add Room")
                    }
                }
            }
        }
    }
}

@Composable
fun CabCard(cab: CabOption, onAddToCart: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = cab.vehicleType, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = "${cab.vehicleModel} • ${cab.capacitySeats} Seats", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "⚡ ${cab.estimatedTime}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "\$${cab.price.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))
                Button(onClick = onAddToCart, shape = RoundedCornerShape(10.dp)) {
                    Text("Book Cab")
                }
            }
        }
    }
}

@Composable
fun TrainCard(train: TrainOption, onAddToCart: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "🚆 ${train.trainName}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = train.classType, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "${train.departureCity} (${train.departureTime}) ➔ ${train.arrivalCity} (${train.arrivalTime})", fontSize = 13.sp)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Status: ${train.seatStatus}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "\$${train.price.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(onClick = onAddToCart, shape = RoundedCornerShape(10.dp)) {
                        Text("Book Rail")
                    }
                }
            }
        }
    }
}
