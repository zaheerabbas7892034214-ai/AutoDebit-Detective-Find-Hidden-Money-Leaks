package com.zaheer.autodebitdetective.presentation.permission

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Security
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

@Composable
fun SMSPermissionExplainerScreen(
    viewModel: PermissionViewModel,
    onNavigateToHome: () -> Unit
) {
    val permissionState by viewModel.permissionState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onPermissionResult(granted, false)
        if (granted) {
            viewModel.onPermissionGranted()
            onNavigateToHome()
        }
    }

    LaunchedEffect(permissionState) {
        if (permissionState is PermissionState.Granted) {
            onNavigateToHome()
        }
    }

    SMSPermissionExplainerContent(
        permissionState = permissionState,
        onRequestPermission = {
            permissionLauncher.launch(Manifest.permission.READ_SMS)
        },
        onOpenSettings = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        },
        onContinueWithoutPermission = {
            viewModel.onContinueWithoutPermission()
            onNavigateToHome()
        }
    )
}

@Composable
private fun SMSPermissionExplainerContent(
    permissionState: PermissionState,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onContinueWithoutPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = "Security",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "SMS Permission Required",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "To detect recurring charges, we need to scan your SMS messages",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        AutoDebitCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Why we need this:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                PermissionBulletPoint("Scan SMS for transaction patterns")
                PermissionBulletPoint("Identify recurring charges automatically")
                PermissionBulletPoint("Detect subscription renewals")
                PermissionBulletPoint("Calculate your monthly spending")

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Your privacy matters:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                PermissionBulletPoint("All processing happens locally")
                PermissionBulletPoint("No data is sent to servers")
                PermissionBulletPoint("No analytics or tracking")
                PermissionBulletPoint("You control your data")
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        when (permissionState) {
            is PermissionState.NotRequested, is PermissionState.Denied -> {
                AutoDebitButton(
                    text = "Request Permission",
                    onClick = onRequestPermission,
                    variant = ButtonVariant.Primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                AutoDebitButton(
                    text = "Continue in Limited Mode",
                    onClick = onContinueWithoutPermission,
                    variant = ButtonVariant.Text
                )
            }
            is PermissionState.PermanentlyDenied -> {
                AutoDebitButton(
                    text = "Open Settings",
                    onClick = onOpenSettings,
                    variant = ButtonVariant.Primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                AutoDebitButton(
                    text = "Continue in Limited Mode",
                    onClick = onContinueWithoutPermission,
                    variant = ButtonVariant.Text
                )
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PermissionBulletPoint(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SMSPermissionExplainerScreenPreview() {
    AutoDebitDetectiveTheme {
        SMSPermissionExplainerContent(
            permissionState = PermissionState.NotRequested,
            onRequestPermission = {},
            onOpenSettings = {},
            onContinueWithoutPermission = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SMSPermissionExplainerDeniedPreview() {
    AutoDebitDetectiveTheme {
        SMSPermissionExplainerContent(
            permissionState = PermissionState.PermanentlyDenied,
            onRequestPermission = {},
            onOpenSettings = {},
            onContinueWithoutPermission = {}
        )
    }
}
