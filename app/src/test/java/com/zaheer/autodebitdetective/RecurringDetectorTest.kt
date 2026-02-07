package com.zaheer.autodebitdetective

import com.zaheer.autodebitdetective.domain.model.Transaction
import com.zaheer.autodebitdetective.parser.CadenceType
import com.zaheer.autodebitdetective.parser.RecurringCategory
import com.zaheer.autodebitdetective.parser.RecurringDetector
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class RecurringDetectorTest {
    
    private lateinit var recurringDetector: RecurringDetector
    
    @Before
    fun setup() {
        recurringDetector = RecurringDetector()
    }
    
    @Test
    fun `detect monthly subscription pattern`() {
        val transactions = listOf(
            createTransaction(1, "Netflix", 799.0, daysAgo(90)),
            createTransaction(2, "Netflix", 799.0, daysAgo(60)),
            createTransaction(3, "Netflix", 799.0, daysAgo(30))
        )
        
        val patterns = recurringDetector.detectRecurringPatterns(transactions, minOccurrences = 2)
        
        assertEquals(1, patterns.size)
        val pattern = patterns[0]
        assertEquals("netflix", pattern.merchant)
        assertEquals(CadenceType.MONTHLY, pattern.cadenceType)
        assertEquals(799.0, pattern.averageAmount, 0.01)
        assertEquals(RecurringCategory.SUBSCRIPTION, pattern.category)
    }
    
    @Test
    fun `detect yearly insurance pattern`() {
        val now = System.currentTimeMillis()
        val oneYearAgo = now - TimeUnit.DAYS.toMillis(365)
        val twoYearsAgo = now - TimeUnit.DAYS.toMillis(730)
        
        val transactions = listOf(
            createTransaction(1, "LIC Premium", 25000.0, twoYearsAgo),
            createTransaction(2, "LIC Premium", 25000.0, oneYearAgo),
            createTransaction(3, "LIC Premium", 25000.0, now)
        )
        
        val patterns = recurringDetector.detectRecurringPatterns(transactions, minOccurrences = 2)
        
        assertEquals(1, patterns.size)
        val pattern = patterns[0]
        assertEquals("lic premium", pattern.merchant)
        assertEquals(CadenceType.YEARLY, pattern.cadenceType)
        assertEquals(RecurringCategory.INSURANCE, pattern.category)
    }
    
    @Test
    fun `detect EMI pattern`() {
        val transactions = listOf(
            createTransaction(1, "HDFC EMI", 5500.0, daysAgo(90)),
            createTransaction(2, "HDFC EMI", 5500.0, daysAgo(60)),
            createTransaction(3, "HDFC EMI", 5500.0, daysAgo(30))
        )
        
        val patterns = recurringDetector.detectRecurringPatterns(transactions, minOccurrences = 2)
        
        assertEquals(1, patterns.size)
        val pattern = patterns[0]
        assertEquals(CadenceType.MONTHLY, pattern.cadenceType)
        assertEquals(RecurringCategory.EMI, pattern.category)
    }
    
    @Test
    fun `detect utility bill pattern`() {
        val transactions = listOf(
            createTransaction(1, "Electricity Bill", 1200.0, daysAgo(60)),
            createTransaction(2, "Electricity Bill", 1300.0, daysAgo(30)),
            createTransaction(3, "Electricity Bill", 1250.0, daysAgo(0))
        )
        
        val patterns = recurringDetector.detectRecurringPatterns(transactions, minOccurrences = 2)
        
        assertEquals(1, patterns.size)
        val pattern = patterns[0]
        assertEquals(CadenceType.MONTHLY, pattern.cadenceType)
        assertEquals(RecurringCategory.UTILITY, pattern.category)
    }
    
    @Test
    fun `reject pattern with high amount variance`() {
        val transactions = listOf(
            createTransaction(1, "Random Store", 100.0, daysAgo(60)),
            createTransaction(2, "Random Store", 500.0, daysAgo(30)),
            createTransaction(3, "Random Store", 1000.0, daysAgo(0))
        )
        
        val patterns = recurringDetector.detectRecurringPatterns(transactions, minOccurrences = 2)
        
        assertTrue(patterns.isEmpty())
    }
    
    @Test
    fun `reject pattern with irregular intervals`() {
        val transactions = listOf(
            createTransaction(1, "Irregular Merchant", 500.0, daysAgo(100)),
            createTransaction(2, "Irregular Merchant", 500.0, daysAgo(50)),
            createTransaction(3, "Irregular Merchant", 500.0, daysAgo(10))
        )
        
        val patterns = recurringDetector.detectRecurringPatterns(transactions, minOccurrences = 2)
        
        assertTrue(patterns.isEmpty())
    }
    
    @Test
    fun `detect multiple recurring patterns`() {
        val transactions = listOf(
            createTransaction(1, "Netflix", 799.0, daysAgo(90)),
            createTransaction(2, "Netflix", 799.0, daysAgo(60)),
            createTransaction(3, "Netflix", 799.0, daysAgo(30)),
            createTransaction(4, "Spotify", 119.0, daysAgo(90)),
            createTransaction(5, "Spotify", 119.0, daysAgo(60)),
            createTransaction(6, "Spotify", 119.0, daysAgo(30))
        )
        
        val patterns = recurringDetector.detectRecurringPatterns(transactions, minOccurrences = 2)
        
        assertEquals(2, patterns.size)
        assertTrue(patterns.any { it.merchant == "netflix" })
        assertTrue(patterns.any { it.merchant == "spotify" })
    }
    
    @Test
    fun `require minimum occurrences`() {
        val transactions = listOf(
            createTransaction(1, "OneTime Merchant", 500.0, daysAgo(30))
        )
        
        val patterns = recurringDetector.detectRecurringPatterns(transactions, minOccurrences = 2)
        
        assertTrue(patterns.isEmpty())
    }
    
    @Test
    fun `predict next charge for monthly pattern`() {
        val lastCharge = daysAgo(0)
        val transactions = listOf(
            createTransaction(1, "Netflix", 799.0, daysAgo(60)),
            createTransaction(2, "Netflix", 799.0, daysAgo(30)),
            createTransaction(3, "Netflix", 799.0, lastCharge)
        )
        
        val patterns = recurringDetector.detectRecurringPatterns(transactions, minOccurrences = 2)
        
        assertEquals(1, patterns.size)
        val pattern = patterns[0]
        val daysDiff = TimeUnit.MILLISECONDS.toDays(pattern.nextPredictedTimestamp - lastCharge)
        assertTrue(daysDiff in 28..31)
    }
    
    @Test
    fun `categorize subscription correctly`() {
        assertTrue(recurringDetector.isSubscription("Netflix"))
        assertTrue(recurringDetector.isSubscription("Amazon Prime"))
        assertTrue(recurringDetector.isSubscription("Spotify"))
        assertTrue(recurringDetector.isSubscription("Disney+ Hotstar"))
        assertFalse(recurringDetector.isSubscription("Random Store"))
    }
    
    @Test
    fun `categorize EMI correctly`() {
        assertTrue(recurringDetector.isEMI("HDFC EMI"))
        assertTrue(recurringDetector.isEMI("Bajaj Finance"))
        assertTrue(recurringDetector.isEMI("Loan Instalment"))
        assertFalse(recurringDetector.isEMI("Netflix"))
    }
    
    @Test
    fun `categorize insurance correctly`() {
        assertTrue(recurringDetector.isInsurance("LIC Premium"))
        assertTrue(recurringDetector.isInsurance("ICICI Prudential"))
        assertTrue(recurringDetector.isInsurance("Health Insurance"))
        assertFalse(recurringDetector.isInsurance("Spotify"))
    }
    
    @Test
    fun `handle case insensitive merchant names`() {
        val transactions = listOf(
            createTransaction(1, "NETFLIX", 799.0, daysAgo(60)),
            createTransaction(2, "netflix", 799.0, daysAgo(30)),
            createTransaction(3, "Netflix", 799.0, daysAgo(0))
        )
        
        val patterns = recurringDetector.detectRecurringPatterns(transactions, minOccurrences = 2)
        
        assertEquals(1, patterns.size)
    }
    
    @Test
    fun `handle merchant names with extra spaces`() {
        val transactions = listOf(
            createTransaction(1, "  Netflix  ", 799.0, daysAgo(60)),
            createTransaction(2, "Netflix", 799.0, daysAgo(30)),
            createTransaction(3, " Netflix ", 799.0, daysAgo(0))
        )
        
        val patterns = recurringDetector.detectRecurringPatterns(transactions, minOccurrences = 2)
        
        assertEquals(1, patterns.size)
    }
    
    @Test
    fun `calculate correct average amount`() {
        val transactions = listOf(
            createTransaction(1, "Utility", 1200.0, daysAgo(60)),
            createTransaction(2, "Utility", 1300.0, daysAgo(30)),
            createTransaction(3, "Utility", 1250.0, daysAgo(0))
        )
        
        val patterns = recurringDetector.detectRecurringPatterns(transactions, minOccurrences = 2)
        
        assertEquals(1, patterns.size)
        assertEquals(1250.0, patterns[0].averageAmount, 1.0)
    }
    
    @Test
    fun `track all transaction IDs in pattern`() {
        val transactions = listOf(
            createTransaction(101, "Netflix", 799.0, daysAgo(60)),
            createTransaction(102, "Netflix", 799.0, daysAgo(30)),
            createTransaction(103, "Netflix", 799.0, daysAgo(0))
        )
        
        val patterns = recurringDetector.detectRecurringPatterns(transactions, minOccurrences = 2)
        
        assertEquals(1, patterns.size)
        assertEquals(3, patterns[0].transactionIds.size)
        assertTrue(patterns[0].transactionIds.contains(101L))
        assertTrue(patterns[0].transactionIds.contains(102L))
        assertTrue(patterns[0].transactionIds.contains(103L))
    }
    
    @Test
    fun `accept slight amount variation for utilities`() {
        val transactions = listOf(
            createTransaction(1, "Electricity", 1200.0, daysAgo(60)),
            createTransaction(2, "Electricity", 1280.0, daysAgo(30)),
            createTransaction(3, "Electricity", 1220.0, daysAgo(0))
        )
        
        val patterns = recurringDetector.detectRecurringPatterns(transactions, minOccurrences = 2)
        
        assertFalse(patterns.isEmpty())
    }
    
    @Test
    fun `categorize membership correctly`() {
        val transactions = listOf(
            createTransaction(1, "Gym Membership", 1500.0, daysAgo(60)),
            createTransaction(2, "Gym Membership", 1500.0, daysAgo(30)),
            createTransaction(3, "Gym Membership", 1500.0, daysAgo(0))
        )
        
        val patterns = recurringDetector.detectRecurringPatterns(transactions, minOccurrences = 2)
        
        assertEquals(1, patterns.size)
        assertEquals(RecurringCategory.MEMBERSHIP, patterns[0].category)
    }
    
    @Test
    fun `detect pattern with exactly minimum occurrences`() {
        val transactions = listOf(
            createTransaction(1, "Test Service", 99.0, daysAgo(30)),
            createTransaction(2, "Test Service", 99.0, daysAgo(0))
        )
        
        val patterns = recurringDetector.detectRecurringPatterns(transactions, minOccurrences = 2)
        
        assertEquals(1, patterns.size)
    }
    
    private fun createTransaction(id: Long, merchant: String, amount: Double, timestamp: Long): Transaction {
        return Transaction(
            id = id,
            timestamp = timestamp,
            amount = amount,
            merchant = merchant,
            rawSnippetHash = "",
            source = "Test Bank"
        )
    }
    
    private fun daysAgo(days: Int): Long {
        return System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong())
    }
}
