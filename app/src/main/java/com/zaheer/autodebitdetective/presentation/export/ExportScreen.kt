package com.zaheer.autodebitdetective.presentation.export

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.zaheer.autodebitdetective.presentation.components.*
import com.zaheer.autodebitdetective.presentation.theme.AutoDebitDetectiveTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    viewModel: ExportViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val history by viewModel.history.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state) {
        if (state is ExportState.Success) {
            val successState = state as ExportState.Success
            val file = File(successState.filePath)
            
            if (file.exists()) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = when (successState.format) {
                        "CSV" -> "text/csv"
                        "PDF" -> "application/pdf"
                        else -> "*/*"
                    }
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                context.startActivity(Intent.createChooser(intent, "Share ${successState.format}"))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Export")
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
        ExportContent(
            state = state,
            history = history,
            onExportCSV = { viewModel.exportCSV() },
            onExportPDF = { viewModel.exportPDF() },
            onDismissSuccess = { viewModel.resetState() },
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
private fun ExportContent(
    state: ExportState,
    history: List<ExportHistoryItem>,
    onExportCSV: () -> Unit,
    onExportPDF: () -> Unit,
    onDismissSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        AutoDebitCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Export Your Data",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Download your recurring charges data in CSV or PDF format",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        AutoDebitButton(
            text = "Export as CSV",
            onClick = onExportCSV,
            enabled = state !is ExportState.Exporting,
            loading = state is ExportState.Exporting,
            variant = ButtonVariant.Primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        AutoDebitButton(
            text = "Export as PDF",
            onClick = onExportPDF,
            enabled = state !is ExportState.Exporting,
            loading = state is ExportState.Exporting,
            variant = ButtonVariant.Secondary
        )

        when (state) {
            is ExportState.Success -> {
                Spacer(modifier = Modifier.height(16.dp))
                
                AutoDebitCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Export Successful",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "${state.format} file created",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        IconButton(onClick = onDismissSuccess) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss")
                        }
                    }
                }
            }
            is ExportState.Error -> {
                Spacer(modifier = Modifier.height(16.dp))
                
                AutoDebitCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Export Failed",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        IconButton(onClick = onDismissSuccess) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss")
                        }
                    }
                }
            }
            else -> {}
        }

        if (history.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Export History",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(history) { item ->
                    ExportHistoryCard(item)
                }
            }
        }
    }
}

@Composable
private fun ExportHistoryCard(item: ExportHistoryItem) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    
    AutoDebitCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateFormatter.format(Date(item.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = item.format,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExportScreenPreview() {
    AutoDebitDetectiveTheme {
        val sampleHistory = listOf(
            ExportHistoryItem(
                fileName = "recurring_charges_2024_01_15.csv",
                format = "CSV",
                timestamp = System.currentTimeMillis() - 24 * 60 * 60 * 1000,
                filePath = "/path/to/file.csv"
            ),
            ExportHistoryItem(
                fileName = "recurring_charges_2024_01_10.pdf",
                format = "PDF",
                timestamp = System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000,
                filePath = "/path/to/file.pdf"
            )
        )
        ExportContent(
            state = ExportState.Idle,
            history = sampleHistory,
            onExportCSV = {},
            onExportPDF = {},
            onDismissSuccess = {}
        )
    }
}
