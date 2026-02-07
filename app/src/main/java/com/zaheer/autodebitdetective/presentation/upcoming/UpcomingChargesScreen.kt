package com.zaheer.autodebitdetective.presentation.upcoming

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaheer.autodebitdetective.domain.model.CadenceType
import com.zaheer.autodebitdetective.domain.model.UpcomingCharge
import com.zaheer.autodebitdetective.presentation.components.*
import com.zaheer.autodebitdetective.presentation.theme.AutoDebitDetectiveTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingChargesScreen(
    viewModel: UpcomingViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Upcoming Charges")
                        Spacer(modifier = Modifier.width(8.dp))
                        ProBadge()
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val currentState = state) {
            is UpcomingState.Loading -> {
                LoadingState(modifier = Modifier.padding(padding))
            }
            is UpcomingState.Success -> {
                UpcomingChargesContent(
                    charges = currentState.charges,
                    onToggleAlert = { charge, enabled ->
                        viewModel.toggleAlert(charge.recurringItemId, enabled)
                    },
                    modifier = Modifier.padding(padding)
                )
            }
            is UpcomingState.Error -> {
                ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.refresh() },
                    modifier = Modifier.padding(padding)
                )
            }
            is UpcomingState.Empty -> {
                EmptyState(
                    message = "No upcoming charges",
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun UpcomingChargesContent(
    charges: List<UpcomingCharge>,
    onToggleAlert: (UpcomingCharge, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Predicted charges for the next 30 days",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(charges) { charge ->
            UpcomingChargeCard(
                charge = charge,
                onToggleAlert = onToggleAlert
            )
        }
    }
}

@Composable
private fun UpcomingChargeCard(
    charge: UpcomingCharge,
    onToggleAlert: (UpcomingCharge, Boolean) -> Unit
) {
    var alertEnabled by remember { mutableStateOf(charge.alertEnabled) }
    
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val daysUntil = ((charge.predictedDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()

    AutoDebitCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = charge.merchantName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = dateFormatter.format(Date(charge.predictedDate)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = when {
                        daysUntil == 0 -> "Today"
                        daysUntil == 1 -> "Tomorrow"
                        daysUntil > 0 -> "in $daysUntil days"
                        else -> "Overdue"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        daysUntil <= 2 -> MaterialTheme.colorScheme.error
                        daysUntil <= 7 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$${String.format("%.2f", charge.estimatedAmount)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                IconButton(
                    onClick = {
                        alertEnabled = !alertEnabled
                        onToggleAlert(charge, alertEnabled)
                    }
                ) {
                    Icon(
                        imageVector = if (alertEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                        contentDescription = if (alertEnabled) "Disable alert" else "Enable alert",
                        tint = if (alertEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UpcomingChargesPreview() {
    AutoDebitDetectiveTheme {
        val sampleCharges = listOf(
            UpcomingCharge(
                recurringItemId = 1,
                merchantName = "Netflix",
                estimatedAmount = 15.99,
                predictedDate = System.currentTimeMillis() + 2L * 24 * 60 * 60 * 1000,
                cadenceType = CadenceType.MONTHLY,
                alertEnabled = true
            ),
            UpcomingCharge(
                recurringItemId = 2,
                merchantName = "Spotify",
                estimatedAmount = 9.99,
                predictedDate = System.currentTimeMillis() + 15L * 24 * 60 * 60 * 1000,
                cadenceType = CadenceType.MONTHLY,
                alertEnabled = false
            )
        )
        UpcomingChargesContent(
            charges = sampleCharges,
            onToggleAlert = { _, _ -> }
        )
    }
}
