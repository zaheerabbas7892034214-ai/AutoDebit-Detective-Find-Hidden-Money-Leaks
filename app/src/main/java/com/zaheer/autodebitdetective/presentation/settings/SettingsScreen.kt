package com.zaheer.autodebitdetective.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaheer.autodebitdetective.billing.SubscriptionStatus
import com.zaheer.autodebitdetective.presentation.components.*
import com.zaheer.autodebitdetective.presentation.theme.AutoDebitDetectiveTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToPaywall: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val dataOperationState by viewModel.dataOperationState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val currentState = state) {
            is SettingsState.Loading -> {
                LoadingState(modifier = Modifier.padding(padding))
            }
            is SettingsState.Success -> {
                SettingsContent(
                    data = currentState.data,
                    dataOperationState = dataOperationState,
                    onToggleAlerts = { viewModel.toggleAlerts(it) },
                    onToggleNotificationPrivacy = { viewModel.toggleNotificationPrivacy(it) },
                    onToggleAppLock = { viewModel.toggleAppLock(it) },
                    onToggleBiometric = { viewModel.toggleBiometric(it) },
                    onRestorePurchases = { viewModel.restorePurchases() },
                    onRescan = onNavigateToScan,
                    onDeleteAllData = { showDeleteDialog = true },
                    onNavigateToPrivacy = onNavigateToPrivacy,
                    onNavigateToPaywall = onNavigateToPaywall,
                    onResetDataState = { viewModel.resetDataOperationState() },
                    modifier = Modifier.padding(padding)
                )
            }
            is SettingsState.Error -> {
                ErrorState(
                    message = currentState.message,
                    onRetry = { },
                    modifier = Modifier.padding(padding)
                )
            }
        }

        if (showDeleteDialog) {
            DeleteDataDialog(
                onConfirm = {
                    viewModel.deleteAllData()
                    showDeleteDialog = false
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
}

@Composable
private fun SettingsContent(
    data: SettingsData,
    dataOperationState: DataOperationState,
    onToggleAlerts: (Boolean) -> Unit,
    onToggleNotificationPrivacy: (Boolean) -> Unit,
    onToggleAppLock: (Boolean) -> Unit,
    onToggleBiometric: (Boolean) -> Unit,
    onRestorePurchases: () -> Unit,
    onRescan: () -> Unit,
    onDeleteAllData: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToPaywall: () -> Unit,
    onResetDataState: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SubscriptionCard(
            status = data.subscriptionStatus,
            onRestore = onRestorePurchases,
            onUpgrade = onNavigateToPaywall,
            isRestoring = dataOperationState is DataOperationState.Processing
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(title = "Notifications") {
            SettingSwitch(
                title = "Enable Alerts",
                description = "Get notified before recurring charges",
                checked = data.alertsEnabled,
                onCheckedChange = onToggleAlerts
            )
            
            SettingSwitch(
                title = "Hide Content",
                description = "Hide merchant names in notifications",
                checked = data.notificationPrivacy,
                onCheckedChange = onToggleNotificationPrivacy
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(title = "Security") {
            SettingSwitch(
                title = "App Lock",
                description = "Require PIN or biometric to open app",
                checked = data.appLockEnabled,
                onCheckedChange = onToggleAppLock
            )
            
            if (data.appLockEnabled) {
                SettingSwitch(
                    title = "Biometric Authentication",
                    description = "Use fingerprint or face unlock",
                    checked = data.biometricEnabled,
                    onCheckedChange = onToggleBiometric
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(title = "Data Management") {
            SettingItem(
                title = "Rescan Messages",
                description = "Scan for new recurring charges",
                icon = Icons.Default.Refresh,
                onClick = onRescan
            )
            
            SettingItem(
                title = "Delete All Data",
                description = "Remove all scanned charges",
                icon = Icons.Default.Delete,
                onClick = onDeleteAllData,
                isDangerous = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(title = "About") {
            SettingItem(
                title = "Privacy Policy",
                description = "How we handle your data",
                icon = Icons.Default.Security,
                onClick = onNavigateToPrivacy
            )
            
            SettingItem(
                title = "Version",
                description = "1.0.0",
                icon = Icons.Default.Info
            )
        }

        if (dataOperationState is DataOperationState.Success) {
            Spacer(modifier = Modifier.height(16.dp))
            
            AutoDebitCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text("Operation completed successfully", modifier = Modifier.weight(1f))
                    
                    IconButton(onClick = onResetDataState) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss")
                    }
                }
            }
        }

        if (dataOperationState is DataOperationState.Error) {
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
                    
                    Text(dataOperationState.message, modifier = Modifier.weight(1f))
                    
                    IconButton(onClick = onResetDataState) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss")
                    }
                }
            }
        }
    }
}

@Composable
private fun SubscriptionCard(
    status: SubscriptionStatus,
    onRestore: () -> Unit,
    onUpgrade: () -> Unit,
    isRestoring: Boolean
) {
    AutoDebitCard {
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
                Text(
                    text = "Subscription",
                    style = MaterialTheme.typography.titleMedium
                )
                
                when (status) {
                    is SubscriptionStatus.Active -> ProBadge()
                    else -> {}
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (status) {
                    is SubscriptionStatus.Active -> "You have an active PRO subscription"
                    is SubscriptionStatus.Expired -> "Your subscription has expired"
                    is SubscriptionStatus.Inactive -> "You're using the free version"
                    else -> "Loading subscription status..."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (status) {
                is SubscriptionStatus.Active -> {
                    AutoDebitButton(
                        text = "Restore Purchases",
                        onClick = onRestore,
                        loading = isRestoring,
                        variant = ButtonVariant.Secondary
                    )
                }
                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            AutoDebitButton(
                                text = "Upgrade",
                                onClick = onUpgrade,
                                variant = ButtonVariant.Primary
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            AutoDebitButton(
                                text = "Restore",
                                onClick = onRestore,
                                loading = isRestoring,
                                variant = ButtonVariant.Secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        AutoDebitCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingItem(
    title: String,
    description: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: (() -> Unit)? = null,
    isDangerous: Boolean = false
) {
    val contentColor = if (isDangerous) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (onClick != null) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DeleteDataDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Delete All Data?") },
        text = {
            Text("This will permanently delete all scanned recurring charges. This action cannot be undone.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
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
private fun SettingsScreenPreview() {
    AutoDebitDetectiveTheme {
        val sampleData = SettingsData(
            subscriptionStatus = SubscriptionStatus.Inactive,
            alertsEnabled = true,
            notificationPrivacy = false,
            appLockEnabled = true,
            biometricEnabled = true
        )
        SettingsContent(
            data = sampleData,
            dataOperationState = DataOperationState.Idle,
            onToggleAlerts = {},
            onToggleNotificationPrivacy = {},
            onToggleAppLock = {},
            onToggleBiometric = {},
            onRestorePurchases = {},
            onRescan = {},
            onDeleteAllData = {},
            onNavigateToPrivacy = {},
            onNavigateToPaywall = {},
            onResetDataState = {}
        )
    }
}
