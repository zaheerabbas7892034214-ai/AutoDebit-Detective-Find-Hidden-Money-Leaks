package com.zaheer.autodebitdetective.presentation.recurring

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
fun RecurringListScreen(
    viewModel: RecurringViewModel,
    onNavigateToPaywall: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showFilterMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recurring Charges") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            SearchBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    viewModel.search(it)
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when (val currentState = state) {
                is RecurringListState.Loading -> {
                    LoadingState()
                }
                is RecurringListState.Success -> {
                    RecurringListContent(
                        data = currentState.data,
                        onUnlockPro = onNavigateToPaywall,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is RecurringListState.Error -> {
                    ErrorState(
                        message = currentState.message,
                        onRetry = { }
                    )
                }
                is RecurringListState.Empty -> {
                    EmptyState(
                        message = "No recurring charges found"
                    )
                }
            }
        }

        if (showFilterMenu) {
            FilterMenuDialog(
                onDismiss = { showFilterMenu = false },
                onFilterSelect = { category ->
                    viewModel.filterByCategory(category)
                    showFilterMenu = false
                },
                onClearFilters = {
                    viewModel.clearFilters()
                    showFilterMenu = false
                }
            )
        }
    }
}

@Composable
private fun RecurringListContent(
    data: RecurringListData,
    onUnlockPro: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (data.filteredCategory != null) {
            item {
                FilterChip(
                    selected = true,
                    onClick = { },
                    label = { Text(data.filteredCategory) }
                )
            }
        }

        itemsIndexed(data.items) { index, item ->
            val shouldBlur = !data.isPro && index >= RecurringViewModel.FREE_ITEM_LIMIT
            
            RecurringItemCard(
                item = item,
                isBlurred = shouldBlur,
                modifier = if (shouldBlur) Modifier.blur(8.dp) else Modifier
            )

            if (index == RecurringViewModel.FREE_ITEM_LIMIT - 1 && !data.isPro && data.items.size > RecurringViewModel.FREE_ITEM_LIMIT) {
                Spacer(modifier = Modifier.height(8.dp))
                UnlockProCard(onClick = onUnlockPro)
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search charges...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        singleLine = true
    )
}

@Composable
private fun RecurringItemCard(
    item: RecurringItem,
    isBlurred: Boolean,
    modifier: Modifier = Modifier
) {
    AutoDebitCard(modifier = modifier) {
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.cadenceType.displayName} • ${item.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.transactionCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${item.transactionCount} transactions • Total: $${String.format("%.2f", item.totalSpent)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%.2f", item.avgAmount)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                if (item.isAlertEnabled) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Alert enabled",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun UnlockProCard(onClick: () -> Unit) {
    AutoDebitCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Unlock All Charges",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Upgrade to PRO to view all your recurring charges",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AutoDebitButton(
                text = "Upgrade to PRO",
                onClick = onClick,
                variant = ButtonVariant.Primary
            )
        }
    }
}

@Composable
private fun FilterMenuDialog(
    onDismiss: () -> Unit,
    onFilterSelect: (String?) -> Unit,
    onClearFilters: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter by Category") },
        text = {
            Column {
                listOf(
                    "Entertainment",
                    "Utilities",
                    "Shopping",
                    "Food",
                    "Transportation",
                    "Health",
                    "Finance",
                    "Other"
                ).forEach { category ->
                    TextButton(
                        onClick = { onFilterSelect(category) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(category, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClearFilters) {
                Text("Clear Filters")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun RecurringListPreview() {
    AutoDebitDetectiveTheme {
        val sampleData = RecurringListData(
            items = listOf(
                RecurringItem(
                    id = 1,
                    merchant = "Netflix",
                    avgAmount = 15.99,
                    cadenceType = CadenceType.MONTHLY,
                    category = "Entertainment",
                    lastChargeEpoch = System.currentTimeMillis(),
                    nextPredictedEpoch = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
                    isAlertEnabled = true,
                    totalSpent = 191.88,
                    transactionCount = 12
                ),
                RecurringItem(
                    id = 2,
                    merchant = "Spotify",
                    avgAmount = 9.99,
                    cadenceType = CadenceType.MONTHLY,
                    category = "Entertainment",
                    lastChargeEpoch = System.currentTimeMillis(),
                    nextPredictedEpoch = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
                    isAlertEnabled = false,
                    totalSpent = 119.88,
                    transactionCount = 12
                )
            ),
            isPro = false
        )
        RecurringListContent(
            data = sampleData,
            onUnlockPro = {}
        )
    }
}
