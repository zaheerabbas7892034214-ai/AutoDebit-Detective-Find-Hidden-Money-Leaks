package com.zaheer.autodebitdetective

import com.zaheer.autodebitdetective.parser.SMSParser
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SMSParserTest {
    
    private lateinit var smsParser: SMSParser
    
    @Before
    fun setup() {
        smsParser = SMSParser()
    }
    
    @Test
    fun `parse HDFC debit SMS extracts amount correctly`() {
        val sms = "Dear Customer, Rs.1,299.00 has been debited from your account ending with 1234 to Netflix on 15-Jan-2024"
        val result = smsParser.parseSMS("HDFCBK", sms, System.currentTimeMillis())
        
        assertNotNull(result)
        assertEquals(1299.0, result?.amount, 0.01)
    }
    
    @Test
    fun `parse ICICI debit SMS extracts merchant correctly`() {
        val sms = "Rs 599 debited from A/c XX1234 on 15-Jan-24 at Amazon. Available bal: Rs 10000"
        val result = smsParser.parseSMS("ICICIB", sms, System.currentTimeMillis())
        
        assertNotNull(result)
        assertEquals("Amazon", result?.merchant)
    }
    
    @Test
    fun `parse SBI UPI transaction extracts merchant from UPI ID`() {
        val sms = "Rs.299.00 debited from A/c **1234 on 15-Jan-24 to paytm@paytm via UPI Ref No 123456789"
        val result = smsParser.parseSMS("SBIINB", sms, System.currentTimeMillis())
        
        assertNotNull(result)
        assertTrue(result?.merchant?.lowercase()?.contains("paytm") == true)
    }
    
    @Test
    fun `parse amount with rupee symbol`() {
        val sms = "₹2,500 has been debited from your account for payment to Swiggy on 15-Jan-2024"
        val result = smsParser.parseSMS("AXISBK", sms, System.currentTimeMillis())
        
        assertNotNull(result)
        assertEquals(2500.0, result?.amount, 0.01)
    }
    
    @Test
    fun `parse amount with INR currency code`() {
        val sms = "INR 1500.50 debited from your account for payment at Zomato on 15-Jan-2024"
        val result = smsParser.parseSMS("KOTAKB", sms, System.currentTimeMillis())
        
        assertNotNull(result)
        assertEquals(1500.50, result?.amount, 0.01)
    }
    
    @Test
    fun `parse amount without comma separator`() {
        val sms = "Rs.999 has been debited from your A/c ending 1234 at BigBasket on 15-Jan-2024"
        val result = smsParser.parseSMS("HDFCBK", sms, System.currentTimeMillis())
        
        assertNotNull(result)
        assertEquals(999.0, result?.amount, 0.01)
    }
    
    @Test
    fun `parse large amount with multiple commas`() {
        val sms = "Rs.1,25,000.00 debited from A/c XX1234 for EMI payment to HDFC Bank on 15-Jan-24"
        val result = smsParser.parseSMS("HDFCBK", sms, System.currentTimeMillis())
        
        assertNotNull(result)
        assertEquals(125000.0, result?.amount, 0.01)
    }
    
    @Test
    fun `identify SBI bank from sender address`() {
        val sms = "Rs.500 debited from A/c XX1234 on 15-Jan-24 at PayTM"
        val result = smsParser.parseSMS("SBIINB", sms, System.currentTimeMillis())
        
        assertNotNull(result)
        assertEquals("SBI", result?.source)
    }
    
    @Test
    fun `identify HDFC bank from sender address`() {
        val sms = "Rs.500 debited from A/c XX1234 on 15-Jan-24 at PayTM"
        val result = smsParser.parseSMS("HDFCBK", sms, System.currentTimeMillis())
        
        assertNotNull(result)
        assertEquals("HDFC", result?.source)
    }
    
    @Test
    fun `identify Axis bank from sender address`() {
        val sms = "Rs.500 debited from A/c XX1234 on 15-Jan-24 at PayTM"
        val result = smsParser.parseSMS("AXISBK", sms, System.currentTimeMillis())
        
        assertNotNull(result)
        assertEquals("Axis", result?.source)
    }
    
    @Test
    fun `return null for non-debit SMS`() {
        val sms = "Your OTP for transaction is 123456. Valid for 10 minutes."
        val result = smsParser.parseSMS("HDFCBK", sms, System.currentTimeMillis())
        
        assertNull(result)
    }
    
    @Test
    fun `return null for credit SMS`() {
        val sms = "Rs.5000 credited to your account ending with 1234 on 15-Jan-2024"
        val result = smsParser.parseSMS("HDFCBK", sms, System.currentTimeMillis())
        
        assertNull(result)
    }
    
    @Test
    fun `return null for SMS without amount`() {
        val sms = "Transaction failed at Netflix due to insufficient balance"
        val result = smsParser.parseSMS("HDFCBK", sms, System.currentTimeMillis())
        
        assertNull(result)
    }
    
    @Test
    fun `parse EMI debit SMS`() {
        val sms = "EMI of Rs.5,500 debited from A/c XX1234 for Loan Account on 01-Jan-2024"
        val result = smsParser.parseSMS("HDFCBK", sms, System.currentTimeMillis())
        
        assertNotNull(result)
        assertEquals(5500.0, result?.amount, 0.01)
    }
    
    @Test
    fun `parse NACH debit SMS`() {
        val sms = "NACH debit of Rs.2,999 from A/c XX1234 to LIC Insurance on 05-Jan-2024"
        val result = smsParser.parseSMS("SBIINB", sms, System.currentTimeMillis())
        
        assertNotNull(result)
        assertEquals(2999.0, result?.amount, 0.01)
    }
    
    @Test
    fun `parse autopay debit SMS`() {
        val sms = "AutoPay: Rs.399 debited from A/c XX1234 for Netflix Premium on 15-Jan-2024"
        val result = smsParser.parseSMS("ICICIB", sms, System.currentTimeMillis())
        
        assertNotNull(result)
        assertEquals(399.0, result?.amount, 0.01)
        assertTrue(result?.merchant?.lowercase()?.contains("netflix") == true)
    }
    
    @Test
    fun `extract merchant from 'at' pattern`() {
        val sms = "Rs.1000 debited from your A/c at McDonald's on 15-Jan-2024"
        val result = smsParser.parseSMS("HDFCBK", sms, System.currentTimeMillis())
        
        assertNotNull(result)
        assertTrue(result?.merchant?.lowercase()?.contains("mcdonald") == true)
    }
    
    @Test
    fun `extract merchant from 'to' pattern`() {
        val sms = "Payment of Rs.500 made to PhonePe from your A/c XX1234"
        val result = smsParser.parseSMS("AXISBK", sms, System.currentTimeMillis())
        
        assertNotNull(result)
        assertTrue(result?.merchant?.lowercase()?.contains("phonepe") == true)
    }
    
    @Test
    fun `extract merchant from 'via' pattern`() {
        val sms = "Rs.299 debited from A/c XX1234 via Google Pay on 15-Jan-2024"
        val result = smsParser.parseSMS("ICICIB", sms, System.currentTimeMillis())
        
        assertNotNull(result)
        assertTrue(result?.merchant?.lowercase()?.contains("google") == true)
    }
    
    @Test
    fun `handle malformed SMS with special characters`() {
        val sms = "Rs.@#$500 debited!!!!! from A/c at Test**Merchant on 15-Jan-2024"
        val result = smsParser.parseSMS("HDFCBK", sms, System.currentTimeMillis())
        
        assertNotNull(result)
        assertEquals(500.0, result?.amount, 0.01)
    }
    
    @Test
    fun `parse decimal amount correctly`() {
        val sms = "Rs.99.99 debited from your account for payment to Disney+ Hotstar"
        val result = smsParser.parseSMS("SBIINB", sms, System.currentTimeMillis())
        
        assertNotNull(result)
        assertEquals(99.99, result?.amount, 0.01)
    }
    
    @Test
    fun `parse amount with 'Amount:' prefix`() {
        val sms = "Transaction successful. Amount: Rs.1,599 debited from A/c XX1234 at Flipkart"
        val result = smsParser.parseSMS("KOTAKB", sms, System.currentTimeMillis())
        
        assertNotNull(result)
        assertEquals(1599.0, result?.amount, 0.01)
    }
    
    @Test
    fun `parse PayTM Payments Bank SMS`() {
        val sms = "Rs.250 paid from your Paytm account to Uber on 15-Jan-2024"
        val result = smsParser.parseSMS("PAYTM", sms, System.currentTimeMillis())
        
        assertNotNull(result)
        assertEquals(250.0, result?.amount, 0.01)
        assertEquals("Paytm", result?.source)
    }
    
    @Test
    fun `parse PhonePe UPI transaction`() {
        val sms = "Rs.199 debited from your PhonePe wallet to zomato@paytm"
        val result = smsParser.parseSMS("PHONEPE", sms, System.currentTimeMillis())
        
        assertNotNull(result)
        assertEquals(199.0, result?.amount, 0.01)
    }
    
    @Test
    fun `parse Google Pay transaction`() {
        val sms = "You paid Rs.500 to swiggy@paytm via Google Pay"
        val result = smsParser.parseSMS("GPAY", sms, System.currentTimeMillis())
        
        assertNotNull(result)
        assertEquals(500.0, result?.amount, 0.01)
    }
    
    @Test
    fun `handle edge case with no merchant found`() {
        val sms = "Rs.100 debited from your account. Transaction successful."
        val result = smsParser.parseSMS("HDFCBK", sms, System.currentTimeMillis())
        
        assertNotNull(result)
        assertEquals(100.0, result?.amount, 0.01)
        assertNotNull(result?.merchant)
    }
    
    @Test
    fun `parse amount with only two decimal places`() {
        val sms = "Rs.999.50 debited from your account at BookMyShow on 15-Jan-2024"
        val result = smsParser.parseSMS("ICICIB", sms, System.currentTimeMillis())
        
        assertNotNull(result)
        assertEquals(999.50, result?.amount, 0.01)
    }
    
    @Test
    fun `reject SMS with invalid amount format`() {
        val sms = "Rs.ABC debited from your account at Netflix"
        val result = smsParser.parseSMS("HDFCBK", sms, System.currentTimeMillis())
        
        assertNull(result)
    }
}
