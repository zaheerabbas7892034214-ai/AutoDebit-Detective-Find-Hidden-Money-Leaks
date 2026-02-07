package com.zaheer.autodebitdetective.presentation.applock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaheer.autodebitdetective.presentation.theme.AutoDebitDetectiveTheme

@Composable
fun AppLockScreen(
    viewModel: AppLockViewModel,
    onUnlocked: () -> Unit,
    showBiometric: Boolean = true
) {
    val mode by viewModel.mode.collectAsState()
    val state by viewModel.state.collectAsState()
    val pin by viewModel.pin.collectAsState()

    LaunchedEffect(state) {
        if (state is AppLockState.Success) {
            onUnlocked()
        }
    }

    AppLockContent(
        mode = mode,
        state = state,
        pin = pin,
        showBiometric = showBiometric,
        onDigitEntered = { viewModel.onDigitEntered(it) },
        onBackspace = { viewModel.onBackspace() },
        onBiometricClick = { }
    )
}

@Composable
private fun AppLockContent(
    mode: AppLockMode,
    state: AppLockState,
    pin: String,
    showBiometric: Boolean,
    onDigitEntered: (Int) -> Unit,
    onBackspace: () -> Unit,
    onBiometricClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = "Lock",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = when (mode) {
                is AppLockMode.Setup -> {
                    when (state) {
                        is AppLockState.EnterPin -> "Create PIN"
                        is AppLockState.ConfirmPin -> "Confirm PIN"
                        else -> "App Lock"
                    }
                }
                is AppLockMode.Verify -> "Enter PIN"
            },
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when (mode) {
                is AppLockMode.Setup -> {
                    when (state) {
                        is AppLockState.EnterPin -> "Enter a 4-digit PIN"
                        is AppLockState.ConfirmPin -> "Enter your PIN again"
                        else -> ""
                    }
                }
                is AppLockMode.Verify -> "Unlock to continue"
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        PinDots(filledCount = pin.length)

        if (state is AppLockState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = state.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        PinPad(
            onDigitEntered = onDigitEntered,
            onBackspace = onBackspace,
            onBiometricClick = if (showBiometric && mode is AppLockMode.Verify) onBiometricClick else null
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PinDots(filledCount: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { index ->
            Surface(
                modifier = Modifier.size(16.dp),
                shape = CircleShape,
                color = if (index < filledCount) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {}
        }
    }
}

@Composable
private fun PinPad(
    onDigitEntered: (Int) -> Unit,
    onBackspace: () -> Unit,
    onBiometricClick: (() -> Unit)?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(3) { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(3) { col ->
                    val digit = row * 3 + col + 1
                    PinButton(
                        text = digit.toString(),
                        onClick = { onDigitEntered(digit) }
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (onBiometricClick != null) {
                IconButton(
                    onClick = onBiometricClick,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Biometric",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(72.dp))
            }

            PinButton(
                text = "0",
                onClick = { onDigitEntered(0) }
            )

            IconButton(
                onClick = onBackspace,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Backspace",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun PinButton(
    text: String,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.size(72.dp),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppLockScreenPreview() {
    AutoDebitDetectiveTheme {
        AppLockContent(
            mode = AppLockMode.Verify,
            state = AppLockState.EnterPin,
            pin = "12",
            showBiometric = true,
            onDigitEntered = {},
            onBackspace = {},
            onBiometricClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppLockSetupPreview() {
    AutoDebitDetectiveTheme {
        AppLockContent(
            mode = AppLockMode.Setup,
            state = AppLockState.EnterPin,
            pin = "",
            showBiometric = false,
            onDigitEntered = {},
            onBackspace = {},
            onBiometricClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppLockErrorPreview() {
    AutoDebitDetectiveTheme {
        AppLockContent(
            mode = AppLockMode.Verify,
            state = AppLockState.Error("Incorrect PIN"),
            pin = "",
            showBiometric = true,
            onDigitEntered = {},
            onBackspace = {},
            onBiometricClick = {}
        )
    }
}
