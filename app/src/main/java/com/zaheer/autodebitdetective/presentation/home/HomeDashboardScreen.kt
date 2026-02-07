package com.zaheer.autodebitdetective.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaheer.autodebitdetective.domain.model.CadenceType
import com.zaheer.autodebitdetective.domain.model.RecurringItem
import com.zaheer.autodebitdetective.presentation.components.*
import com.zaheer.autodebitdetective.presentation.theme.AutoDebitDetectiveTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(
    viewModel: HomeViewModel,
    onNavigateToRecurringList: () -> Unit,
    onNavigateToUpcoming: () -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPaywall: () -> Unit,
    onNavigateToScan: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToRecurringList,
                    icon = { Icon(Icons.Default.List, contentDescription = "Recurring") },
                    label = { Text("Recurring") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToInsights,
                    icon = { Icon(Icons.Default.PieChart, contentDescription = "Insights") },
                    label = { Text("Insights") }
                )
            }
        }
    ) { padding ->
        when (val currentState = state) {
            is HomeState.Loading -> {
                LoadingState(modifier = Modifier.padding(padding))
            }
            is HomeState.Success -> {
                HomeDashboardContent(
                    modifier = Modifier.padding(padding),
                    data = currentState.data,
                    onNavigateToPaywall = onNavigateToPaywall,
                    onNavigateToScan = onNavigateToScan,
                    onNavigateToRecurringList = onNavigateToRecurringList,
                    onNavigateToUpcoming = onNavigateToUpcoming,
                    onNavigateToExport = onNavigateToExport
                )
            }
            is HomeState.Error -> {
                ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.refresh() },
                    modifier = Modifier.padding(padding)
                )
            }
            is HomeState.Empty -> {
                EmptyState(
                    message = "No recurring charges found",
                    actionText = "Scan Messages",
                    onAction = onNavigateToScan,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun HomeDashboardContent(
    modifier: Modifier = Modifier,
    data: DashboardData,
    onNavigateToPaywall: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToRecurringList: () -> Unit,
    onNavigateToUpcoming: () -> Unit,
    onNavigateToExport: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (!data.isPro) {
            ProBanner(onClick = onNavigateToPaywall)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Monthly",
                value = "$${String.format("%.2f", data.monthlyTotal)}",
                icon = Icons.Default.CalendarMonth
            )
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Yearly",
                value = "$${String.format("%.0f", data.yearlyProjection)}",
                icon = Icons.Default.TrendingUp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SummaryCard(
            modifier = Modifier.fillMaxWidth(),
            title = "Recurring Charges",
            value = "${data.recurringCount}",
            icon = Icons.Default.Repeat
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Top Merchants",
                style = MaterialTheme.typography.titleLarge
            )
            TextButton(onClick = onNavigateToRecurringList) {
                Text("View All")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        data.topMerchants.forEach { item ->
            MerchantListItem(item = item)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        AutoDebitButton(
            text = "Rescan Messages",
            onClick = onNavigateToScan,
            variant = ButtonVariant.Secondary
        )

        if (data.isPro) {
            Spacer(modifier = Modifier.height(8.dp))

            AutoDebitButton(
                text = "View Upcoming Charges",
                onClick = onNavigateToUpcoming,
                variant = ButtonVariant.Secondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            AutoDebitButton(
                text = "Export Data",
                onClick = onNavigateToExport,
                variant = ButtonVariant.Secondary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ProBanner(onClick: () -> Unit) {
    AutoDebitCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Pro",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Upgrade to PRO",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Unlock unlimited access & advanced features",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Go",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    AutoDebitCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MerchantListItem(item: RecurringItem) {
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
                    text = item.merchant,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.cadenceType.displayName} • ${item.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "$${String.format("%.2f", item.avgAmount)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeDashboardPreview() {
    AutoDebitDetectiveTheme {
        val sampleData = DashboardData(
            monthlyTotal = 127.50,
            yearlyProjection = 1530.0,
            recurringCount = 8,
            topMerchants = listOf(
                RecurringItem(
                    merchant = "Netflix",
                    avgAmount = 15.99,
                    cadenceType = CadenceType.MONTHLY,
                    category = "Entertainment",
                    lastChargeEpoch = System.currentTimeMillis(),
                    nextPredictedEpoch = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
                    isAlertEnabled = true
                ),
                RecurringItem(
                    merchant = "Spotify",
                    avgAmount = 9.99,
                    cadenceType = CadenceType.MONTHLY,
                    category = "Entertainment",
                    lastChargeEpoch = System.currentTimeMillis(),
                    nextPredictedEpoch = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
                    isAlertEnabled = true
                )
            ),
            isPro = false
        )
        HomeDashboardContent(
            data = sampleData,
            onNavigateToPaywall = {},
            onNavigateToScan = {},
            onNavigateToRecurringList = {},
            onNavigateToUpcoming = {},
            onNavigateToExport = {}
        )
    }
}
