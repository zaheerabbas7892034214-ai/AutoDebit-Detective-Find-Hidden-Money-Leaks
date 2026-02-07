package com.zaheer.autodebitdetective.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaheer.autodebitdetective.presentation.components.AutoDebitCard
import com.zaheer.autodebitdetective.presentation.theme.AutoDebitDetectiveTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        PrivacyContent(modifier = Modifier.padding(padding))
    }
}

@Composable
private fun PrivacyContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        AutoDebitCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Your Privacy Matters",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "AutoDebit Detective is designed with your privacy as the top priority.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        PrivacySection(
            title = "Local Processing",
            content = "All SMS message scanning and analysis happens entirely on your device. Your messages never leave your phone."
        )

        PrivacySection(
            title = "No Cloud Storage",
            content = "We don't store your data on any servers. All detected recurring charges are stored locally in your device's encrypted database."
        )

        PrivacySection(
            title = "No Third-Party Sharing",
            content = "We never share your financial data with third parties, advertisers, or data brokers. Your information stays private."
        )

        PrivacySection(
            title = "No Analytics or Tracking",
            content = "We don't use analytics services or tracking pixels. We don't know what you're scanning or how you're using the app."
        )

        PrivacySection(
            title = "Minimal Permissions",
            content = "We only request the SMS permission needed for scanning. You can revoke this permission at any time in your device settings."
        )

        PrivacySection(
            title = "No Account Required",
            content = "You don't need to create an account or provide any personal information to use the app."
        )

        PrivacySection(
            title = "Encrypted Storage",
            content = "All data stored on your device is encrypted using Android's built-in encryption mechanisms."
        )

        PrivacySection(
            title = "Optional App Lock",
            content = "Enable PIN or biometric authentication to add an extra layer of security when opening the app."
        )

        PrivacySection(
            title = "Data Control",
            content = "You have complete control over your data. Delete all scanned charges at any time from the Settings screen."
        )

        PrivacySection(
            title = "Subscription Privacy",
            content = "Subscriptions are managed through Google Play. We only receive a subscription token to verify your PRO status, not your payment details."
        )

        Spacer(modifier = Modifier.height(16.dp))

        AutoDebitCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Questions?",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "If you have any questions about how we handle your data, please contact us at support@autodebitdetective.com",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PrivacySection(
    title: String,
    content: String
) {
    AutoDebitCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    Spacer(modifier = Modifier.height(12.dp))
}

@Preview(showBackground = true)
@Composable
private fun PrivacyScreenPreview() {
    AutoDebitDetectiveTheme {
        PrivacyContent()
    }
}
