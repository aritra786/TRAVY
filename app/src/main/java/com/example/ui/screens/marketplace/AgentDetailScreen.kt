package com.example.ui.screens.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.TravelAgent
import com.example.ui.components.EscrowBadge
import com.example.ui.components.RatingChip
import com.example.ui.components.VerifiedBadge
import com.example.ui.screens.home.PackageCard
import com.example.ui.viewmodel.MarketplaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentDetailScreen(
    agentId: String,
    viewModel: MarketplaceViewModel,
    onBackClick: () -> Unit,
    onPackageClick: (String) -> Unit,
    onChatClick: (String) -> Unit
) {
    val agent = viewModel.agents.value.find { it.id == agentId }
        ?: viewModel.agents.value.firstOrNull()

    val agentPackages = viewModel.packages.value.filter { it.agentId == agentId }

    var showQuoteDialog by remember { mutableStateOf(false) }

    if (agent == null) return

    if (showQuoteDialog) {
        QuoteRequestDialog(
            agentName = agent.name,
            onDismiss = { showQuoteDialog = false },
            onSubmit = { dest, dates, guests, budget, notes ->
                viewModel.submitCustomQuoteRequest(
                    agentId = agent.id,
                    destination = dest,
                    travelDates = dates,
                    travelersCount = guests,
                    budgetPerPerson = budget,
                    notes = notes
                )
                showQuoteDialog = false
                onChatClick(agent.id)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(agent.name, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showQuoteDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("request_quote_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Request Quote")
                    }

                    Button(
                        onClick = { onChatClick(agent.id) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("direct_chat_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Chat Directly")
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = agent.logoUrl,
                                contentDescription = agent.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(text = agent.name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                VerifiedBadge(text = agent.iataRegistrationNo)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(text = agent.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            InfoStat(label = "Completed Trips", value = "${agent.completedTrips}+")
                            InfoStat(label = "Response Time", value = agent.responseTime)
                            InfoStat(label = "Rating", value = "${agent.rating} ⭐")
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Active Packages (${agentPackages.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    EscrowBadge()
                }
            }

            items(agentPackages) { pkg ->
                PackageCard(
                    pkg = pkg,
                    onClick = { onPackageClick(pkg.id) }
                )
            }
        }
    }
}

@Composable
fun InfoStat(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}
