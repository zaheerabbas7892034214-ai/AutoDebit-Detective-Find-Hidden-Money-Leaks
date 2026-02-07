package com.zaheer.autodebitdetective.utils

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {
    
    private val locale = Locale("en", "IN")
    
    fun formatTimestampToDate(timestamp: Long, pattern: String = Constants.DATE_FORMAT_DISPLAY): String {
        return try {
            val date = Date(timestamp)
            val formatter = SimpleDateFormat(pattern, locale)
            formatter.format(date)
        } catch (e: Exception) {
            ""
        }
    }
    
    fun formatTimestampToRelative(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 0 -> "Future"
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                "$minutes minute${if (minutes > 1) "s" else ""} ago"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "$hours hour${if (hours > 1) "s" else ""} ago"
            }
            diff < TimeUnit.DAYS.toMillis(2) -> "Yesterday"
            diff < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                "$days day${if (days > 1) "s" else ""} ago"
            }
            diff < TimeUnit.DAYS.toMillis(30) -> {
                val weeks = TimeUnit.MILLISECONDS.toDays(diff) / 7
                "$weeks week${if (weeks > 1) "s" else ""} ago"
            }
            diff < TimeUnit.DAYS.toMillis(365) -> {
                val months = TimeUnit.MILLISECONDS.toDays(diff) / 30
                "$months month${if (months > 1) "s" else ""} ago"
            }
            else -> {
                val years = TimeUnit.MILLISECONDS.toDays(diff) / 365
                "$years year${if (years > 1) "s" else ""} ago"
            }
        }
    }
    
    fun calculateDaysBetween(startTimestamp: Long, endTimestamp: Long): Long {
        val diffInMillis = endTimestamp - startTimestamp
        return TimeUnit.MILLISECONDS.toDays(diffInMillis)
    }
    
    fun getStartOfMonth(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    fun getEndOfMonth(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
    
    fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    fun getEndOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
    
    fun addDays(timestamp: Long, days: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.add(Calendar.DAY_OF_MONTH, days)
        return calendar.timeInMillis
    }
    
    fun addMonths(timestamp: Long, months: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.add(Calendar.MONTH, months)
        return calendar.timeInMillis
    }
    
    fun addYears(timestamp: Long, years: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.add(Calendar.YEAR, years)
        return calendar.timeInMillis
    }
    
    fun isToday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_YEAR)
        val todayYear = calendar.get(Calendar.YEAR)
        
        calendar.timeInMillis = timestamp
        val dateDay = calendar.get(Calendar.DAY_OF_YEAR)
        val dateYear = calendar.get(Calendar.YEAR)
        
        return today == dateDay && todayYear == dateYear
    }
    
    fun isYesterday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.get(Calendar.DAY_OF_YEAR)
        val yesterdayYear = calendar.get(Calendar.YEAR)
        
        calendar.timeInMillis = timestamp
        val dateDay = calendar.get(Calendar.DAY_OF_YEAR)
        val dateYear = calendar.get(Calendar.YEAR)
        
        return yesterday == dateDay && yesterdayYear == dateYear
    }
    
    fun isTomorrow(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = calendar.get(Calendar.DAY_OF_YEAR)
        val tomorrowYear = calendar.get(Calendar.YEAR)
        
        calendar.timeInMillis = timestamp
        val dateDay = calendar.get(Calendar.DAY_OF_YEAR)
        val dateYear = calendar.get(Calendar.YEAR)
        
        return tomorrow == dateDay && tomorrowYear == dateYear
    }
    
    fun formatRelativeDate(timestamp: Long): String {
        return when {
            isToday(timestamp) -> "Today"
            isYesterday(timestamp) -> "Yesterday"
            isTomorrow(timestamp) -> "Tomorrow"
            else -> {
                val now = System.currentTimeMillis()
                val diff = timestamp - now
                
                when {
                    diff > 0 && diff < TimeUnit.DAYS.toMillis(7) -> {
                        val days = TimeUnit.MILLISECONDS.toDays(diff)
                        "In ${days + 1} day${if (days + 1 > 1) "s" else ""}"
                    }
                    diff < 0 && diff > -TimeUnit.DAYS.toMillis(7) -> {
                        val days = TimeUnit.MILLISECONDS.toDays(-diff)
                        "${days + 1} day${if (days + 1 > 1) "s" else ""} ago"
                    }
                    else -> formatTimestampToDate(timestamp, Constants.DATE_FORMAT_DISPLAY)
                }
            }
        }
    }
    
    fun getDayOfMonth(timestamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.DAY_OF_MONTH)
    }
    
    fun getMonth(timestamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.MONTH)
    }
    
    fun getYear(timestamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.YEAR)
    }
    
    fun getMonthName(timestamp: Long): String {
        val formatter = SimpleDateFormat("MMMM", locale)
        return formatter.format(Date(timestamp))
    }
    
    fun getMonthYearString(timestamp: Long): String {
        return formatTimestampToDate(timestamp, Constants.DATE_FORMAT_MONTH_YEAR)
    }
    
    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }
    
    fun getTimestampFromDateString(dateString: String, pattern: String = Constants.DATE_FORMAT_DISPLAY): Long? {
        return try {
            val formatter = SimpleDateFormat(pattern, locale)
            formatter.parse(dateString)?.time
        } catch (e: Exception) {
            null
        }
    }
    
    fun getMonthsBetween(startTimestamp: Long, endTimestamp: Long): Int {
        val startCalendar = Calendar.getInstance()
        startCalendar.timeInMillis = startTimestamp
        
        val endCalendar = Calendar.getInstance()
        endCalendar.timeInMillis = endTimestamp
        
        val yearDiff = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR)
        val monthDiff = endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH)
        
        return yearDiff * 12 + monthDiff
    }
    
    fun isSameMonth(timestamp1: Long, timestamp2: Long): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.timeInMillis = timestamp1
        
        val cal2 = Calendar.getInstance()
        cal2.timeInMillis = timestamp2
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
    }
    
    fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.timeInMillis = timestamp1
        
        val cal2 = Calendar.getInstance()
        cal2.timeInMillis = timestamp2
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
    
    fun getLastNMonths(n: Int): List<Long> {
        val months = mutableListOf<Long>()
        val calendar = Calendar.getInstance()
        
        for (i in 0 until n) {
            months.add(getStartOfMonth(calendar.timeInMillis))
            calendar.add(Calendar.MONTH, -1)
        }
        
        return months.reversed()
    }
    
    fun getDaysInMonth(timestamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
}
