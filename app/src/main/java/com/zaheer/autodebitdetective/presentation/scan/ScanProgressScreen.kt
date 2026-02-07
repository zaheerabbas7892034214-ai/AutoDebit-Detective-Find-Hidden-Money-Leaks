package com.zaheer.autodebitdetective.presentation.scan

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaheer.autodebitdetective.presentation.components.AutoDebitButton
import com.zaheer.autodebitdetective.presentation.components.ButtonVariant
import com.zaheer.autodebitdetective.presentation.theme.AutoDebitDetectiveTheme

@Composable
fun ScanProgressScreen(
    viewModel: ScanViewModel,
    onNavigateToHome: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        if (state is ScanState.Idle) {
            viewModel.startScan()
        }
    }

    ScanProgressContent(
        state = state,
        onCancel = {
            viewModel.cancelScan()
            onNavigateToHome()
        },
        onRetry = { viewModel.startScan() },
        onComplete = {
            viewModel.resetState()
            onNavigateToHome()
        }
    )
}

@Composable
private fun ScanProgressContent(
    state: ScanState,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onComplete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (state) {
                is ScanState.Idle -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is ScanState.Scanning -> {
                    CircularProgressIndicator(
                        progress = state.progress / 100f,
                        modifier = Modifier.size(64.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "${state.progress}%",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    AutoDebitButton(
                        text = "Cancel",
                        onClick = onCancel,
                        variant = ButtonVariant.Secondary
                    )
                }
                is ScanState.Success -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Scan Complete!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Found ${state.itemsFound} recurring charges",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    AutoDebitButton(
                        text = "View Dashboard",
                        onClick = onComplete,
                        variant = ButtonVariant.Primary
                    )
                }
                is ScanState.Error -> {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Scan Failed",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    AutoDebitButton(
                        text = "Retry",
                        onClick = onRetry,
                        variant = ButtonVariant.Primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AutoDebitButton(
                        text = "Go Back",
                        onClick = onCancel,
                        variant = ButtonVariant.Text
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ScanProgressScreenPreview() {
    AutoDebitDetectiveTheme {
        ScanProgressContent(
            state = ScanState.Scanning(45, "Parsing 127 messages..."),
            onCancel = {},
            onRetry = {},
            onComplete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ScanSuccessScreenPreview() {
    AutoDebitDetectiveTheme {
        ScanProgressContent(
            state = ScanState.Success(12),
            onCancel = {},
            onRetry = {},
            onComplete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ScanErrorScreenPreview() {
    AutoDebitDetectiveTheme {
        ScanProgressContent(
            state = ScanState.Error("Permission denied"),
            onCancel = {},
            onRetry = {},
            onComplete = {}
        )
    }
}
