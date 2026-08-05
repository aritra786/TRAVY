package com.example.ui.screens.cart

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.CartItemEntity
import com.example.ui.components.EscrowBadge
import com.example.ui.viewmodel.CartAndItineraryViewModel

@Composable
fun CartAndItineraryScreen(
    viewModel: CartAndItineraryViewModel,
    onNavigateToHome: () -> Unit
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val wishlistItems by viewModel.wishlistItems.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()
    val checkoutSuccess by viewModel.checkoutSuccess.collectAsState()

    var activeTab by remember { mutableStateOf("CART") } // "CART", "TIMELINE", "WISHLIST"

    val context = LocalContext.current

    if (checkoutSuccess) {
        AlertDialog(
            onDismissRequest = { viewModel.resetCheckoutState() },
            icon = { Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Success", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp)) },
            title = { Text("Escrow Booking Confirmed! 🎉", fontWeight = FontWeight.Bold) },
            text = { Text("Your payment of \$${totalPrice.toInt()} is held safely in escrow. Your travel vouchers and flight tickets are now visible in your Master Trip Timeline.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetCheckoutState()
                        activeTab = "TIMELINE"
                    },
                    modifier = Modifier.testTag("view_timeline_btn")
                ) {
                    Text("View Trip Timeline")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 90.dp)
    ) {
        // --- HEADER ---
        Surface(shadowElevation = 2.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Unified Cart & Itinerary",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Combine Flights, Stays & Packages into One Timeline",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    EscrowBadge()
                }

                Spacer(modifier = Modifier.height(14.dp))

                TabRow(
                    selectedTabIndex = when (activeTab) {
                        "CART" -> 0
                        "TIMELINE" -> 1
                        else -> 2
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = activeTab == "CART",
                        onClick = { activeTab = "CART" },
                        text = { Text("Cart (${cartItems.size})", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeTab == "TIMELINE",
                        onClick = { activeTab = "TIMELINE" },
                        text = { Text("Trip Timeline 📅", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeTab == "WISHLIST",
                        onClick = { activeTab = "WISHLIST" },
                        text = { Text("Wishlist (${wishlistItems.size}) ⭐", fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        when (activeTab) {
            "CART" -> {
                if (cartItems.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.ShoppingCart,
                        message = "Your cart is currently empty.",
                        subtext = "Add packages, flights, hotels or cabs to create your combined itinerary.",
                        buttonText = "Explore Packages",
                        onButtonClick = onNavigateToHome
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 100.dp)
                        ) {
                            items(cartItems) { item ->
                                CartItemCard(
                                    item = item,
                                    onDelete = { viewModel.removeItem(item.id) }
                                )
                            }
                        }

                        // Bottom Checkout Bar
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(),
                            shadowElevation = 12.dp,
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "Total Cart Price", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "\$${totalPrice.toInt()}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Button(
                                    onClick = { viewModel.checkoutEscrow() },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.testTag("checkout_escrow_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pay & Lock Escrow", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            "TIMELINE" -> {
                if (cartItems.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.Timeline,
                        message = "No active bookings in your timeline.",
                        subtext = "Booked flights, hotels, and agent packages will automatically build your master itinerary timeline here.",
                        buttonText = "Plan New Trip",
                        onButtonClick = onNavigateToHome
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.FlightLand, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = "Master Trip Timeline Ticket", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(text = "All component vouchers synced & verified", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                    }
                                }
                            }
                        }

                        items(cartItems) { item ->
                            TimelineCard(item = item)
                        }
                    }
                }
            }

            "WISHLIST" -> {
                if (wishlistItems.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.FavoriteBorder,
                        message = "Your wishlist is empty.",
                        subtext = "Tap the heart icon on any post, hotel, or package to save it here for later.",
                        buttonText = "Discover Destinations",
                        onButtonClick = onNavigateToHome
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(wishlistItems) { item ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = item.imageUrl,
                                        contentDescription = item.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = item.subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(item: CartItemEntity, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cart_item_${item.id}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = item.itemType,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                Text(text = item.subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "\$${item.price.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
            }

            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun TimelineCard(item: CartItemEntity) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (item.itemType) {
                            "FLIGHT" -> Icons.Default.Flight
                            "HOTEL" -> Icons.Default.Hotel
                            "CAB" -> Icons.Default.DirectionsCar
                            "TRAIN" -> Icons.Default.Train
                            else -> Icons.Default.ConfirmationNumber
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = item.itemType, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(text = "Confirmed Ticket", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text = item.subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Voucher Code: TRVY-${(100000..999999).random()}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun EmptyStateView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    subtext: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtext,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onButtonClick, shape = RoundedCornerShape(12.dp)) {
            Text(buttonText)
        }
    }
}
