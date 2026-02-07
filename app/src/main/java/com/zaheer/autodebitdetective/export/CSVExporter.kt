package com.zaheer.autodebitdetective.export

import android.content.Context
import android.net.Uri
import android.util.Log
import com.zaheer.autodebitdetective.domain.model.RecurringItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.IOException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CSVExporter(private val context: Context) {
    
    suspend fun exportToCSV(
        items: List<RecurringItem>,
        outputUri: Uri
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                val writer = outputStream.bufferedWriter()
                
                writeCSVHeader(writer)
                
                items.forEach { item ->
                    writeCSVRow(writer, item)
                }
                
                writer.flush()
                Log.d(TAG, "Successfully exported ${items.size} items to CSV")
                Result.success(Unit)
            } ?: run {
                Log.e(TAG, "Failed to open output stream")
                Result.failure(IOException("Unable to open output stream"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting to CSV", e)
            Result.failure(e)
        }
    }
    
    private fun writeCSVHeader(writer: BufferedWriter) {
        val headers = listOf(
            "Merchant",
            "Category",
            "Amount (₹)",
            "Cadence",
            "Last Charge Date",
            "Next Predicted Date",
            "Days Until Next",
            "Alerts Enabled"
        )
        
        writer.write(headers.joinToString(",") { escapeCSVField(it) })
        writer.newLine()
    }
    
    private fun writeCSVRow(writer: BufferedWriter, item: RecurringItem) {
        val currencyFormat = NumberFormat.getInstance(Locale("en", "IN"))
        val dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        
        val currentTime = System.currentTimeMillis()
        val daysUntilNext = ((item.nextPredictedEpoch - currentTime) / (1000 * 60 * 60 * 24)).toInt()
        
        val row = listOf(
            item.merchant,
            item.category,
            currencyFormat.format(item.avgAmount),
            formatCadence(item.cadenceType),
            dateFormat.format(Date(item.lastChargeEpoch)),
            dateFormat.format(Date(item.nextPredictedEpoch)),
            daysUntilNext.toString(),
            if (item.isAlertEnabled) "Yes" else "No"
        )
        
        writer.write(row.joinToString(",") { escapeCSVField(it) })
        writer.newLine()
    }
    
    private fun escapeCSVField(field: String): String {
        return if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
    }
    
    private fun formatCadence(cadence: String): String {
        return when (cadence.uppercase()) {
            "MONTHLY" -> "Monthly"
            "YEARLY" -> "Yearly"
            else -> "Unknown"
        }
    }
    
    suspend fun generateCSVString(items: List<RecurringItem>): String = withContext(Dispatchers.IO) {
        val builder = StringBuilder()
        
        val headers = listOf(
            "Merchant", "Category", "Amount (₹)", "Cadence",
            "Last Charge Date", "Next Predicted Date"
        )
        builder.appendLine(headers.joinToString(",") { escapeCSVField(it) })
        
        val currencyFormat = NumberFormat.getInstance(Locale("en", "IN"))
        val dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        
        items.forEach { item ->
            val row = listOf(
                item.merchant,
                item.category,
                currencyFormat.format(item.avgAmount),
                formatCadence(item.cadenceType),
                dateFormat.format(Date(item.lastChargeEpoch)),
                dateFormat.format(Date(item.nextPredictedEpoch))
            )
            builder.appendLine(row.joinToString(",") { escapeCSVField(it) })
        }
        
        builder.toString()
    }
    
    companion object {
        private const val TAG = "CSVExporter"
    }
}
