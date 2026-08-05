package com.example.ui.screens.marketplace

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.EscrowBadge
import com.example.ui.components.RatingChip
import com.example.ui.components.VerifiedBadge
import com.example.ui.screens.home.PackageCard
import com.example.ui.viewmodel.MarketplaceViewModel

@Composable
fun MarketplaceScreen(
    viewModel: MarketplaceViewModel,
    onNavigateToPackageDetail: (String) -> Unit,
    onNavigateToAgentDetail: (String) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val agents by viewModel.agents.collectAsState()
    val packages by viewModel.packages.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val quoteSuccessMsg by viewModel.quoteRequestSuccessMsg.collectAsState()

    var showQuoteDialog by remember { mutableStateOf(false) }
    var targetAgentId by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    LaunchedEffect(quoteSuccessMsg) {
        quoteSuccessMsg?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    if (showQuoteDialog && targetAgentId != null) {
        QuoteRequestDialog(
            agentName = agents.find { it.id == targetAgentId }?.name ?: "Verified Agent",
            onDismiss = { showQuoteDialog = false },
            onSubmit = { dest, dates, guests, budget, notes ->
                viewModel.submitCustomQuoteRequest(
                    agentId = targetAgentId!!,
                    destination = dest,
                    travelDates = dates,
                    travelersCount = guests,
                    budgetPerPerson = budget,
                    notes = notes
                )
                showQuoteDialog = false
                onNavigateToChat(targetAgentId!!)
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
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
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
                            text = "Travel Agent Marketplace",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Verified IATA Agencies & Tailored Escrow Bookings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    EscrowBadge()
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf("All", "Honeymoon", "Adventure", "Luxury", "Budget", "Group Tour")) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { viewModel.selectCategory(cat) },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- TOP FEATURED AGENTS CAROUSEL ---
            item {
                Text(
                    text = "Top Verified Agencies",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(agents) { agent ->
                        MarketplaceAgentRowCard(
                            agent = agent,
                            onAgentClick = { onNavigateToAgentDetail(agent.id) },
                            onRequestQuote = {
                                targetAgentId = agent.id
                                showQuoteDialog = true
                            },
                            onChatClick = { onNavigateToChat(agent.id) }
                        )
                    }
                }
            }

            // --- ALL PACKAGES LIST ---
            item {
                Text(
                    text = "Handcrafted Packages (${packages.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            items(packages) { pkg ->
                PackageCard(
                    pkg = pkg,
                    onClick = { onNavigateToPackageDetail(pkg.id) }
                )
            }
        }
    }
}

@Composable
fun MarketplaceAgentRowCard(
    agent: com.example.data.model.TravelAgent,
    onAgentClick: () -> Unit,
    onRequestQuote: () -> Unit,
    onChatClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .testTag("marketplace_agent_${agent.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .clickable { onAgentClick() }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = agent.logoUrl,
                    contentDescription = agent.name,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = agent.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    VerifiedBadge()
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = agent.tagline,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RatingChip(rating = agent.rating, reviewsCount = agent.reviewsCount)
                Text(
                    text = "⚡ ${agent.responseTime}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRequestQuote,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text("Get Quote", fontSize = 11.sp)
                }

                Button(
                    onClick = onChatClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text("Chat", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun QuoteRequestDialog(
    agentName: String,
    onDismiss: () -> Unit,
    onSubmit: (destination: String, dates: String, guests: Int, budget: Double, notes: String) -> Unit
) {
    var dest by remember { mutableStateOf("") }
    var dates by remember { mutableStateOf("") }
    var guests by remember { mutableStateOf("2") }
    var budget by remember { mutableStateOf("1200") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request Custom Quote from $agentName", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = dest,
                    onValueChange = { dest = it },
                    label = { Text("Destination City / Region") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dates,
                    onValueChange = { dates = it },
                    label = { Text("Travel Dates (e.g. Sep 15 - Sep 22)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = guests,
                        onValueChange = { guests = it },
                        label = { Text("Travelers") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = budget,
                        onValueChange = { budget = it },
                        label = { Text("Budget/Person ($)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Special Preferences / Hotel Stars") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val guestsInt = guests.toIntOrNull() ?: 2
                    val budgetDbl = budget.toDoubleOrNull() ?: 1000.0
                    onSubmit(dest.ifEmpty { "Bali" }, dates.ifEmpty { "Next Month" }, guestsInt, budgetDbl, notes)
                },
                modifier = Modifier.testTag("submit_quote_btn")
            ) {
                Text("Submit & Start Chat")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
