package com.zaheer.autodebitdetective.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import com.zaheer.autodebitdetective.domain.model.RecurringItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PDFExporter(private val context: Context) {
    
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    
    private val titlePaint = Paint().apply {
        textSize = 24f
        color = Color.BLACK
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    
    private val headerPaint = Paint().apply {
        textSize = 16f
        color = Color.BLACK
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    
    private val normalPaint = Paint().apply {
        textSize = 12f
        color = Color.BLACK
        isAntiAlias = true
    }
    
    private val smallPaint = Paint().apply {
        textSize = 10f
        color = Color.DKGRAY
        isAntiAlias = true
    }
    
    suspend fun exportToPDF(
        items: List<RecurringItem>,
        outputUri: Uri
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val document = PdfDocument()
            
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
            var yPosition = 50f
            
            yPosition = drawTitle(canvas, yPosition)
            
            yPosition = drawSummary(canvas, items, yPosition)
            
            yPosition = drawCategoryBreakdown(canvas, items, yPosition)
            
            if (yPosition > 700) {
                document.finishPage(page)
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 50f
            }
            
            yPosition = drawUpcomingCharges(canvas, items, yPosition)
            
            if (yPosition > 700) {
                document.finishPage(page)
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 50f
            }
            
            drawAllRecurringItems(canvas, document, items, yPosition)
            
            document.finishPage(page)
            
            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                document.writeTo(outputStream)
                document.close()
                Log.d(TAG, "Successfully exported PDF with ${items.size} items")
                Result.success(Unit)
            } ?: run {
                document.close()
                Log.e(TAG, "Failed to open output stream")
                Result.failure(IOException("Unable to open output stream"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting to PDF", e)
            Result.failure(e)
        }
    }
    
    private fun drawTitle(canvas: Canvas, yPosition: Float): Float {
        var y = yPosition
        canvas.drawText("AutoDebit Detective Report", 50f, y, titlePaint)
        y += 30f
        
        canvas.drawText("Generated: ${dateFormat.format(Date())}", 50f, y, smallPaint)
        y += 40f
        
        return y
    }
    
    private fun drawSummary(canvas: Canvas, items: List<RecurringItem>, yPosition: Float): Float {
        var y = yPosition
        
        canvas.drawText("Financial Summary", 50f, y, headerPaint)
        y += 25f
        
        val monthlyTotal = items.filter { it.cadenceType == "MONTHLY" }.sumOf { it.avgAmount }
        val yearlyTotal = items.filter { it.cadenceType == "YEARLY" }.sumOf { it.avgAmount }
        val estimatedYearlyTotal = (monthlyTotal * 12) + yearlyTotal
        
        canvas.drawText("Total Monthly Charges: ${currencyFormat.format(monthlyTotal)}", 70f, y, normalPaint)
        y += 20f
        
        canvas.drawText("Total Yearly Charges: ${currencyFormat.format(yearlyTotal)}", 70f, y, normalPaint)
        y += 20f
        
        canvas.drawText("Estimated Annual Spending: ${currencyFormat.format(estimatedYearlyTotal)}", 70f, y, normalPaint)
        y += 20f
        
        canvas.drawText("Total Recurring Items: ${items.size}", 70f, y, normalPaint)
        y += 35f
        
        return y
    }
    
    private fun drawCategoryBreakdown(canvas: Canvas, items: List<RecurringItem>, yPosition: Float): Float {
        var y = yPosition
        
        canvas.drawText("Category Breakdown", 50f, y, headerPaint)
        y += 25f
        
        val categoryTotals = items.groupBy { it.category }
            .mapValues { (_, itemList) -> itemList.sumOf { it.avgAmount } }
            .toList()
            .sortedByDescending { it.second }
        
        categoryTotals.forEach { (category, total) ->
            val count = items.count { it.category == category }
            canvas.drawText(
                "$category: ${currencyFormat.format(total)} ($count items)",
                70f,
                y,
                normalPaint
            )
            y += 20f
        }
        
        y += 20f
        return y
    }
    
    private fun drawUpcomingCharges(canvas: Canvas, items: List<RecurringItem>, yPosition: Float): Float {
        var y = yPosition
        
        canvas.drawText("Upcoming Charges (Next 30 Days)", 50f, y, headerPaint)
        y += 25f
        
        val currentTime = System.currentTimeMillis()
        val thirtyDaysFromNow = currentTime + (30L * 24 * 60 * 60 * 1000)
        
        val upcomingItems = items
            .filter { it.nextPredictedEpoch in currentTime..thirtyDaysFromNow }
            .sortedBy { it.nextPredictedEpoch }
        
        if (upcomingItems.isEmpty()) {
            canvas.drawText("No charges in the next 30 days", 70f, y, normalPaint)
            y += 20f
        } else {
            upcomingItems.take(10).forEach { item ->
                val dateStr = dateFormat.format(Date(item.nextPredictedEpoch))
                val text = "$dateStr - ${item.merchant}: ${currencyFormat.format(item.avgAmount)}"
                canvas.drawText(text, 70f, y, normalPaint)
                y += 20f
            }
            
            if (upcomingItems.size > 10) {
                canvas.drawText("... and ${upcomingItems.size - 10} more", 70f, y, smallPaint)
                y += 20f
            }
        }
        
        y += 20f
        return y
    }
    
    private fun drawAllRecurringItems(
        canvas: Canvas,
        document: PdfDocument,
        items: List<RecurringItem>,
        startY: Float
    ) {
        var y = startY
        var currentPage = canvas
        var pageNumber = 1
        
        canvas.drawText("All Recurring Items", 50f, y, headerPaint)
        y += 30f
        
        items.sortedBy { it.merchant }.forEach { item ->
            if (y > 780) {
                document.finishPage(document.pages[pageNumber - 1])
                
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, ++pageNumber).create()
                val page = document.startPage(pageInfo)
                currentPage = page.canvas
                y = 50f
            }
            
            currentPage.drawText(item.merchant, 70f, y, normalPaint)
            y += 15f
            
            currentPage.drawText(
                "Amount: ${currencyFormat.format(item.avgAmount)} | " +
                "Category: ${item.category} | " +
                "Cadence: ${formatCadence(item.cadenceType)}",
                90f,
                y,
                smallPaint
            )
            y += 15f
            
            currentPage.drawText(
                "Next: ${dateFormat.format(Date(item.nextPredictedEpoch))}",
                90f,
                y,
                smallPaint
            )
            y += 25f
        }
    }
    
    private fun formatCadence(cadence: String): String {
        return when (cadence.uppercase()) {
            "MONTHLY" -> "Monthly"
            "YEARLY" -> "Yearly"
            else -> "Unknown"
        }
    }
    
    companion object {
        private const val TAG = "PDFExporter"
    }
}
