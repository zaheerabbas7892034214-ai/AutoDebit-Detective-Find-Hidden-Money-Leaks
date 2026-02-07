package com.zaheer.autodebitdetective.domain.usecase

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.zaheer.autodebitdetective.domain.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class ScanSMSUseCase(private val context: Context) {

    companion object {
        private val AMOUNT_REGEX = """(?:Rs\.?|INR|₹)\s*(\d+(?:,\d+)*(?:\.\d{2})?)""".toRegex(RegexOption.IGNORE_CASE)
        private val AT_REGEX = """at\s+([A-Za-z0-9\s&.'-]+?)(?:\s+on|\s+for|\s+via|\.|\n|$)""".toRegex(RegexOption.IGNORE_CASE)
        private val TO_REGEX = """to\s+([A-Za-z0-9\s&.'-]+?)(?:\s+on|\s+for|\s+via|\.|\n|$)""".toRegex(RegexOption.IGNORE_CASE)
        private val DEBIT_KEYWORDS = listOf("debited", "debit", "spent", "paid", "payment", "purchase")
    }

    suspend operator fun invoke(): Result<List<Transaction>> = withContext(Dispatchers.IO) {
        try {
            val transactions = mutableListOf<Transaction>()
            val uri: Uri = Telephony.Sms.CONTENT_URI
            val projection = arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE
            )

            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )

            cursor?.use {
                val idIndex = it.getColumnIndexOrThrow(Telephony.Sms._ID)
                val addressIndex = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyIndex = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndexOrThrow(Telephony.Sms.DATE)

                while (it.moveToNext()) {
                    val body = it.getString(bodyIndex) ?: continue
                    val address = it.getString(addressIndex) ?: "Unknown"
                    val date = it.getLong(dateIndex)

                    val parsedTransaction = parseTransactionFromSMS(body, address, date)
                    if (parsedTransaction != null) {
                        transactions.add(parsedTransaction)
                    }
                }
            }

            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseTransactionFromSMS(body: String, address: String, timestamp: Long): Transaction? {
        val lowerBody = body.lowercase()
        val containsDebitKeyword = DEBIT_KEYWORDS.any { lowerBody.contains(it) }

        if (!containsDebitKeyword) {
            return null
        }

        val amountMatch = AMOUNT_REGEX.find(body)
        val amount = amountMatch?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull() ?: return null

        val merchant = extractMerchantName(body, address)
        val hash = generateHash(body)

        return Transaction(
            timestamp = timestamp,
            amount = amount,
            merchant = merchant,
            rawSnippetHash = hash,
            source = "SMS"
        )
    }

    private fun extractMerchantName(body: String, address: String): String {
        val atMatch = AT_REGEX.find(body)
        if (atMatch != null) {
            return atMatch.groupValues[1].trim()
        }

        val toMatch = TO_REGEX.find(body)
        if (toMatch != null) {
            return toMatch.groupValues[1].trim()
        }

        return address.take(20)
    }

    private fun generateHash(text: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(text.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
