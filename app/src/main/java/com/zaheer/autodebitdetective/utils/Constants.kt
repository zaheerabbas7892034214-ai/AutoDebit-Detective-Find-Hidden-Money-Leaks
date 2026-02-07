package com.zaheer.autodebitdetective.utils

object Constants {
    
    // Notification Channels
    const val NOTIFICATION_CHANNEL_ALERTS = "alerts_channel"
    const val NOTIFICATION_CHANNEL_ALERTS_NAME = "Payment Alerts"
    const val NOTIFICATION_CHANNEL_ALERTS_DESC = "Notifications for upcoming payments"
    
    const val NOTIFICATION_CHANNEL_SCAN = "scan_channel"
    const val NOTIFICATION_CHANNEL_SCAN_NAME = "SMS Scanning"
    const val NOTIFICATION_CHANNEL_SCAN_DESC = "Notifications for SMS scanning progress"
    
    // Notification IDs
    const val NOTIFICATION_ID_ALERT = 1001
    const val NOTIFICATION_ID_SCAN = 1002
    
    // Preference Keys
    const val PREF_KEY_FIRST_LAUNCH = "first_launch"
    const val PREF_KEY_PERMISSIONS_GRANTED = "permissions_granted"
    const val PREF_KEY_LAST_SCAN_TIME = "last_scan_time"
    const val PREF_KEY_THEME_MODE = "theme_mode"
    const val PREF_KEY_CURRENCY_SYMBOL = "currency_symbol"
    const val PREF_KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    const val PREF_KEY_ALERT_DAYS_BEFORE = "alert_days_before"
    const val PREF_KEY_HIDE_NOTIFICATION_CONTENT = "hide_notification_content"
    const val PREF_KEY_APP_LOCK_ENABLED = "app_lock_enabled"
    const val PREF_KEY_AUTO_CATEGORIZE = "auto_categorize"
    const val PREF_KEY_SCAN_FREQUENCY = "scan_frequency"
    const val PREF_KEY_IS_PREMIUM = "is_premium"
    
    // Billing Product IDs
    const val PRODUCT_ID_MONTHLY = "autodebit_premium_monthly"
    const val PRODUCT_ID_YEARLY = "autodebit_premium_yearly"
    const val PRODUCT_ID_LIFETIME = "autodebit_premium_lifetime"
    
    // SMS Patterns - Bank Keywords
    val BANK_KEYWORDS = listOf(
        "HDFC", "ICICI", "SBI", "AXIS", "KOTAK", "PAYTM", "PHONEPE",
        "GOOGLEPAY", "AMAZONPAY", "MOBIKWIK", "FREECHARGE", "BHIM",
        "BANK", "BANKING", "DEBIT", "CREDIT", "UPI", "IMPS", "NEFT"
    )
    
    // SMS Patterns - Debit Keywords
    val DEBIT_KEYWORDS = listOf(
        "debited", "debit", "deducted", "paid", "payment", 
        "purchase", "spent", "withdrawn", "charged"
    )
    
    // SMS Patterns - Amount Patterns
    val AMOUNT_PATTERNS = listOf(
        "Rs\\.?\\s*([\\d,]+\\.?\\d*)",
        "INR\\s*([\\d,]+\\.?\\d*)",
        "₹\\s*([\\d,]+\\.?\\d*)",
        "Amount\\s*:?\\s*Rs\\.?\\s*([\\d,]+\\.?\\d*)",
        "Amount\\s*:?\\s*INR\\s*([\\d,]+\\.?\\d*)",
        "Amount\\s*:?\\s*₹\\s*([\\d,]+\\.?\\d*)"
    )
    
    // Category Names
    const val CATEGORY_SUBSCRIPTION = "Subscription"
    const val CATEGORY_UTILITY = "Utility"
    const val CATEGORY_INSURANCE = "Insurance"
    const val CATEGORY_EMI = "EMI"
    const val CATEGORY_RENT = "Rent"
    const val CATEGORY_INVESTMENT = "Investment"
    const val CATEGORY_ENTERTAINMENT = "Entertainment"
    const val CATEGORY_FOOD = "Food & Dining"
    const val CATEGORY_SHOPPING = "Shopping"
    const val CATEGORY_TRAVEL = "Travel"
    const val CATEGORY_HEALTHCARE = "Healthcare"
    const val CATEGORY_EDUCATION = "Education"
    const val CATEGORY_OTHER = "Other"
    
    // Category Keywords
    val CATEGORY_KEYWORDS = mapOf(
        CATEGORY_SUBSCRIPTION to listOf(
            "netflix", "prime", "spotify", "youtube", "hotstar", 
            "disney", "zee5", "sonyliv", "voot", "subscription"
        ),
        CATEGORY_UTILITY to listOf(
            "electricity", "water", "gas", "broadband", "internet",
            "mobile", "recharge", "postpaid", "bill", "bses", "adani"
        ),
        CATEGORY_INSURANCE to listOf(
            "insurance", "policy", "premium", "lic", "icici prudential",
            "hdfc life", "sbi life", "max life", "bajaj allianz"
        ),
        CATEGORY_EMI to listOf(
            "emi", "loan", "instalment", "installment", "repayment",
            "mortgage", "credit card"
        ),
        CATEGORY_RENT to listOf(
            "rent", "rental", "lease", "housing"
        ),
        CATEGORY_INVESTMENT to listOf(
            "mutual fund", "sip", "stock", "share", "investment",
            "zerodha", "groww", "upstox", "kuvera"
        ),
        CATEGORY_ENTERTAINMENT to listOf(
            "movie", "cinema", "bookmyshow", "paytm insider",
            "gaming", "game", "entertainment"
        ),
        CATEGORY_FOOD to listOf(
            "zomato", "swiggy", "food", "restaurant", "dining",
            "dominos", "pizza", "mcdonalds", "kfc", "burger"
        ),
        CATEGORY_SHOPPING to listOf(
            "amazon", "flipkart", "myntra", "ajio", "shopping",
            "purchase", "store", "mart", "mall"
        ),
        CATEGORY_TRAVEL to listOf(
            "uber", "ola", "rapido", "flight", "hotel", "booking",
            "makemytrip", "goibibo", "cleartrip", "irctc", "travel"
        ),
        CATEGORY_HEALTHCARE to listOf(
            "hospital", "clinic", "doctor", "medicine", "pharmacy",
            "health", "medical", "apollo", "fortis", "max healthcare"
        ),
        CATEGORY_EDUCATION to listOf(
            "school", "college", "university", "course", "tuition",
            "education", "udemy", "coursera", "byju", "unacademy"
        )
    )
    
    // Recurring Detection
    const val MIN_TRANSACTIONS_FOR_PATTERN = 2
    const val MAX_AMOUNT_VARIANCE_PERCENT = 10.0
    const val MONTHLY_PATTERN_DAYS_VARIANCE = 5
    const val YEARLY_PATTERN_DAYS_VARIANCE = 7
    
    // Alert Settings
    const val DEFAULT_ALERT_DAYS_BEFORE = 3
    const val MIN_ALERT_DAYS = 1
    const val MAX_ALERT_DAYS = 7
    
    // Scan Settings
    const val DEFAULT_SCAN_FREQUENCY_HOURS = 24L
    const val MIN_SCAN_FREQUENCY_HOURS = 6L
    const val MAX_SCAN_FREQUENCY_HOURS = 168L // 7 days
    
    // Export Settings
    const val EXPORT_FILE_PREFIX = "AutoDebit_"
    const val EXPORT_DATE_FORMAT = "yyyyMMdd_HHmmss"
    const val PDF_FILE_EXTENSION = ".pdf"
    const val CSV_FILE_EXTENSION = ".csv"
    
    // Date Formats
    const val DATE_FORMAT_DISPLAY = "dd MMM yyyy"
    const val DATE_FORMAT_FULL = "dd MMMM yyyy"
    const val DATE_FORMAT_SHORT = "dd/MM/yy"
    const val DATE_FORMAT_WITH_TIME = "dd MMM yyyy, hh:mm a"
    const val DATE_FORMAT_MONTH_YEAR = "MMM yyyy"
    
    // Currency
    const val CURRENCY_SYMBOL_INR = "₹"
    const val CURRENCY_CODE_INR = "INR"
    
    // WorkManager Tags
    const val WORK_TAG_SCAN = "sms_scan_work"
    const val WORK_TAG_ALERT = "alert_work"
    
    // Free Tier Limits
    const val FREE_TIER_TRANSACTION_LIMIT = 50
    const val FREE_TIER_RECURRING_LIMIT = 10
    const val FREE_TIER_EXPORT_LIMIT = 3
    const val FREE_TIER_MONTHS_HISTORY = 3
    
    // Premium Features
    const val PREMIUM_UNLIMITED_TRANSACTIONS = true
    const val PREMIUM_UNLIMITED_RECURRING = true
    const val PREMIUM_UNLIMITED_EXPORT = true
    const val PREMIUM_FULL_HISTORY = true
    const val PREMIUM_ADVANCED_INSIGHTS = true
    const val PREMIUM_NO_ADS = true
    
    // App Lock
    const val APP_LOCK_TIMEOUT_SECONDS = 300 // 5 minutes
    const val MAX_BIOMETRIC_ATTEMPTS = 3
    
    // Database
    const val DATABASE_NAME = "autodebit_database"
    const val DATABASE_VERSION = 1
    
    // Misc
    const val SPLASH_SCREEN_DURATION_MS = 2000L
    const val SCAN_PROGRESS_UPDATE_INTERVAL_MS = 100L
    const val DEBOUNCE_DELAY_MS = 300L
    const val ANIMATION_DURATION_MS = 300
}
