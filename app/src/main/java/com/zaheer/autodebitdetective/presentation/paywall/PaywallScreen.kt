package com.zaheer.autodebitdetective.presentation.paywall

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaheer.autodebitdetective.presentation.components.AutoDebitButton
import com.zaheer.autodebitdetective.presentation.components.AutoDebitCard
import com.zaheer.autodebitdetective.presentation.components.ButtonVariant
import com.zaheer.autodebitdetective.presentation.theme.AutoDebitDetectiveTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    viewModel: PaywallViewModel,
    onNavigateBack: () -> Unit,
    onSubscriptionSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state) {
        when (state) {
            is PaywallState.Success -> onSubscriptionSuccess()
            is PaywallState.AlreadySubscribed -> onNavigateBack()
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upgrade to PRO") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { padding ->
        PaywallContent(
            state = state,
            onSubscribe = { viewModel.launchBillingFlow(context as Activity) },
            onRestore = { viewModel.restorePurchases() },
            onDismissError = { viewModel.dismissError() },
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
private fun PaywallContent(
    state: PaywallState,
    onSubscribe: () -> Unit,
    onRestore: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Pro",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "AutoDebit Detective PRO",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Unlock all features and take full control",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        AutoDebitCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "PRO Features",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                FeatureItem(
                    icon = Icons.Default.Lock,
                    title = "Unlimited Access",
                    description = "View all your recurring charges, no limits"
                )

                FeatureItem(
                    icon = Icons.Default.Notifications,
                    title = "Smart Alerts",
                    description = "Get notified before charges hit your account"
                )

                FeatureItem(
                    icon = Icons.Default.CalendarMonth,
                    title = "Upcoming Charges",
                    description = "See predicted charges for the next 30 days"
                )

                FeatureItem(
                    icon = Icons.Default.Download,
                    title = "Export Data",
                    description = "Download CSV and PDF reports"
                )

                FeatureItem(
                    icon = Icons.Default.PieChart,
                    title = "Advanced Insights",
                    description = "Category breakdowns and spending trends"
                )

                FeatureItem(
                    icon = Icons.Default.Security,
                    title = "App Lock",
                    description = "PIN and biometric protection"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        AutoDebitCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$4.99",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "per month",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        AutoDebitButton(
            text = when (state) {
                is PaywallState.Purchasing -> "Processing..."
                else -> "Subscribe Now"
            },
            onClick = onSubscribe,
            enabled = state !is PaywallState.Purchasing && state !is PaywallState.Loading,
            loading = state is PaywallState.Purchasing,
            variant = ButtonVariant.Primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        AutoDebitButton(
            text = "Restore Purchases",
            onClick = onRestore,
            enabled = state !is PaywallState.Purchasing && state !is PaywallState.Loading,
            loading = state is PaywallState.Loading,
            variant = ButtonVariant.Text
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Cancel anytime • No hidden fees",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (state is PaywallState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            
            AutoDebitCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    
                    IconButton(onClick = onDismissError) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss")
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ComparisonTable() {
    AutoDebitCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "FREE vs PRO",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Feature", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(2f))
                Text("FREE", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("PRO", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            ComparisonRow("View charges", "5 items", "Unlimited")
            ComparisonRow("Smart alerts", "✗", "✓")
            ComparisonRow("Upcoming charges", "✗", "✓")
            ComparisonRow("Export data", "✗", "✓")
            ComparisonRow("Advanced insights", "✗", "✓")
            ComparisonRow("App lock", "✗", "✓")
        }
    }
}

@Composable
private fun ComparisonRow(feature: String, free: String, pro: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(feature, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(2f))
        Text(free, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        Text(pro, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
    }
}

@Preview(showBackground = true)
@Composable
private fun PaywallScreenPreview() {
    AutoDebitDetectiveTheme {
        PaywallContent(
            state = PaywallState.Idle,
            onSubscribe = {},
            onRestore = {},
            onDismissError = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PaywallErrorPreview() {
    AutoDebitDetectiveTheme {
        PaywallContent(
            state = PaywallState.Error("Payment failed. Please try again."),
            onSubscribe = {},
            onRestore = {},
            onDismissError = {}
        )
    }
}
