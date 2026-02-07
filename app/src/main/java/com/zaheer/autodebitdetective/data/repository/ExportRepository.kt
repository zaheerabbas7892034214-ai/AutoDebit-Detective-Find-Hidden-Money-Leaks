package com.zaheer.autodebitdetective.data.repository

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.zaheer.autodebitdetective.domain.model.RecurringItem
import com.zaheer.autodebitdetective.domain.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportRepository(private val context: Context) {

    suspend fun exportTransactionsToCSV(
        transactions: List<Transaction>,
        fileName: String? = null
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val csvFileName = fileName ?: generateFileName("transactions", "csv")
            val file = File(context.getExternalFilesDir(null), csvFileName)

            file.bufferedWriter().use { writer ->
                writer.write("ID,Timestamp,Date,Merchant,Amount,Source,Hash\n")

                transactions.forEach { txn ->
                    val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(Date(txn.timestamp))
                    writer.write("${txn.id},${txn.timestamp},\"$date\",\"${txn.merchant}\",${txn.amount},${txn.source},${txn.rawSnippetHash}\n")
                }
            }

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportRecurringToCSV(
        recurringItems: List<RecurringItem>,
        fileName: String? = null
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val csvFileName = fileName ?: generateFileName("recurring", "csv")
            val file = File(context.getExternalFilesDir(null), csvFileName)

            file.bufferedWriter().use { writer ->
                writer.write("ID,Merchant,Average Amount,Cadence,Category,Last Charge,Next Predicted,Alert Enabled\n")

                recurringItems.forEach { item ->
                    val lastDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(Date(item.lastChargeEpoch))
                    val nextDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(Date(item.nextPredictedEpoch))
                    writer.write("${item.id},\"${item.merchant}\",${item.avgAmount},${item.cadenceType.displayName},${item.category},\"$lastDate\",\"$nextDate\",${item.isAlertEnabled}\n")
                }
            }

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportRecurringToPDF(
        recurringItems: List<RecurringItem>,
        fileName: String? = null
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val pdfFileName = fileName ?: generateFileName("recurring", "pdf")
            val file = File(context.getExternalFilesDir(null), pdfFileName)

            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)

            val canvas = page.canvas
            val paint = Paint().apply {
                textSize = 12f
                isAntiAlias = true
            }

            var yPosition = 50f
            canvas.drawText("AutoDebit Detective - Recurring Charges Report", 50f, yPosition, paint.apply { 
                textSize = 16f
                isFakeBoldText = true
            })
            
            yPosition += 30f
            paint.textSize = 12f
            paint.isFakeBoldText = false

            canvas.drawText("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}", 
                50f, yPosition, paint)
            yPosition += 30f

            canvas.drawText("Merchant", 50f, yPosition, paint.apply { isFakeBoldText = true })
            canvas.drawText("Amount", 250f, yPosition, paint)
            canvas.drawText("Cadence", 350f, yPosition, paint)
            canvas.drawText("Next Date", 450f, yPosition, paint)
            yPosition += 20f

            paint.isFakeBoldText = false

            recurringItems.take(20).forEach { item ->
                if (yPosition > 800) {
                    pdfDocument.finishPage(page)
                    val newPage = pdfDocument.startPage(pageInfo)
                    yPosition = 50f
                }

                val nextDate = SimpleDateFormat("MMM dd", Locale.getDefault())
                    .format(Date(item.nextPredictedEpoch))

                canvas.drawText(item.merchant.take(20), 50f, yPosition, paint)
                canvas.drawText("₹${String.format("%.2f", item.avgAmount)}", 250f, yPosition, paint)
                canvas.drawText(item.cadenceType.displayName, 350f, yPosition, paint)
                canvas.drawText(nextDate, 450f, yPosition, paint)
                yPosition += 20f
            }

            pdfDocument.finishPage(page)

            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateFileName(prefix: String, extension: String): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "${prefix}_$timestamp.$extension"
    }
}
