# AutoDebit Detective - Find Hidden Money Leaks 💰🔍

<div align="center">

![Android](https://img.shields.io/badge/Android-34-green?logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-blue?logo=kotlin)
![MinSdk](https://img.shields.io/badge/MinSdk-24-orange)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.08-brightgreen)
![License](https://img.shields.io/badge/License-MIT-yellow)

**AutoDebit Detective** is an intelligent Android app that helps you discover and manage hidden recurring charges and subscriptions by analyzing your SMS transaction messages. Built with modern Android architecture and Jetpack Compose, it provides a privacy-first approach to tracking your recurring expenses.

[Screenshots] | [Demo Video] | [Download]

</div>

---

## 📱 App Overview

### What is AutoDebit Detective?

AutoDebit Detective automatically scans your SMS messages to identify recurring charges, subscriptions, and auto-debits that you might have forgotten about. It helps you:

- 💡 **Discover Hidden Charges**: Find all your active subscriptions and recurring payments in seconds
- 💰 **Save Money**: Identify and cancel unwanted subscriptions that drain your wallet
- 📊 **Track Spending**: Visualize your recurring expenses with insightful analytics
- ⏰ **Stay Informed**: Get notified before upcoming charges
- 🔒 **Privacy First**: All data stays on your device - nothing is uploaded to servers

### Key Features

✅ Smart SMS-based transaction detection  
✅ Automatic categorization of recurring charges  
✅ Visual analytics and spending insights  
✅ Upcoming payment predictions  
✅ Export data to CSV/PDF (PRO)  
✅ Biometric app lock for security  
✅ Material You design with dynamic theming  
✅ 100% offline - no data leaves your device  

### Screenshots

> _Screenshots showing: Home Dashboard, Recurring List, Category Breakdown, Paywall, Settings_

---

## 🎯 Project Specifications

| Specification | Value |
|--------------|-------|
| **Package Name** | `com.zaheer.autodebitdetective` |
| **Application ID** | `com.zaheer.autodebitdetective` |
| **Version** | 1.0.0 (versionCode: 1) |
| **Minimum SDK** | 24 (Android 7.0 Nougat) - 99.5% device coverage |
| **Target SDK** | 34 (Android 14) |
| **Compile SDK** | 34 |
| **Language** | Kotlin 2.0.0 |
| **JDK Version** | Java 17 |
| **Build Tool** | Gradle 8.7 with Kotlin DSL |
| **UI Framework** | Jetpack Compose + Material 3 (Material You) |
| **Architecture** | MVVM + Repository Pattern |
| **Database** | Room 2.6.1 with KSP |
| **Settings Storage** | DataStore Preferences 1.1.1 |
| **Background Tasks** | WorkManager 2.9.0 |
| **Monetization** | Google Play Billing v7.0.0 (Subscription Model) |
| **Navigation** | Navigation Compose 2.7.7 |
| **Async** | Kotlin Coroutines 1.8.1 |
| **Security** | Biometric 1.2.0-alpha05 + SHA-256 PIN |

---

## 🏗️ Project Structure

```
app/src/main/java/com/zaheer/autodebitdetective/
├── MainActivity.kt                          # Main entry point
├── AutoDebitDetectiveApp.kt                 # Application class
│
├── data/                                    # Data Layer
│   ├── local/
│   │   ├── dao/                             # Room DAOs
│   │   │   ├── RecurringItemDao.kt
│   │   │   └── TransactionDao.kt
│   │   ├── entity/                          # Room Entities
│   │   │   ├── RecurringItemEntity.kt
│   │   │   └── TransactionEntity.kt
│   │   └── AutoDebitDatabase.kt             # Room Database
│   ├── datastore/
│   │   └── PreferencesManager.kt            # DataStore wrapper
│   └── repository/
│       ├── RecurringRepository.kt           # Recurring items repo
│       ├── TransactionRepository.kt         # Transactions repo
│       └── ExportRepository.kt              # Export functionality
│
├── domain/                                  # Domain Layer
│   ├── model/                               # Domain models
│   │   ├── RecurringItem.kt
│   │   ├── Transaction.kt
│   │   ├── Category.kt
│   │   └── Cadence.kt
│   └── usecase/                             # Business logic
│       ├── DetectRecurringUseCase.kt
│       ├── CalculateInsightsUseCase.kt
│       └── PredictUpcomingUseCase.kt
│
├── presentation/                            # UI Layer (11 Screens)
│   ├── splash/                              # 1. Splash Screen
│   │   ├── SplashScreen.kt
│   │   └── SplashViewModel.kt
│   ├── permission/                          # 2. SMS Permission Explainer
│   │   ├── SMSPermissionExplainerScreen.kt
│   │   └── PermissionViewModel.kt
│   ├── scan/                                # 3. Scan Progress
│   │   ├── ScanProgressScreen.kt
│   │   └── ScanViewModel.kt
│   ├── home/                                # 4. Home Dashboard
│   │   ├── HomeDashboardScreen.kt
│   │   └── HomeViewModel.kt
│   ├── recurring/                           # 5. Recurring List
│   │   ├── RecurringListScreen.kt
│   │   └── RecurringViewModel.kt
│   ├── upcoming/                            # 6. Upcoming Charges (PRO)
│   │   ├── UpcomingChargesScreen.kt
│   │   └── UpcomingViewModel.kt
│   ├── insights/                            # 7. Category Breakdown (Insights)
│   │   ├── CategoryBreakdownScreen.kt
│   │   └── InsightsViewModel.kt
│   ├── export/                              # 8. Export Screen (PRO)
│   │   ├── ExportScreen.kt
│   │   └── ExportViewModel.kt
│   ├── paywall/                             # 9. Paywall/Subscription
│   │   ├── PaywallScreen.kt
│   │   └── PaywallViewModel.kt
│   ├── settings/                            # 10. Settings
│   │   ├── SettingsScreen.kt
│   │   ├── PrivacyScreen.kt
│   │   └── SettingsViewModel.kt
│   ├── applock/                             # 11. App Lock (PIN Entry)
│   │   ├── AppLockScreen.kt
│   │   └── AppLockViewModel.kt
│   ├── navigation/
│   │   └── NavGraph.kt                      # Navigation setup
│   ├── components/                          # Reusable UI components
│   │   ├── AutoDebitButton.kt
│   │   ├── AutoDebitCard.kt
│   │   ├── LoadingState.kt
│   │   ├── ErrorState.kt
│   │   ├── EmptyState.kt
│   │   └── ProBadge.kt
│   └── theme/                               # Material 3 Theming
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
│
├── parser/                                  # SMS Parsing Logic
│   ├── SMSParser.kt                         # Extract transaction data
│   └── RecurringDetector.kt                 # Detect recurring patterns
│
├── billing/                                 # In-App Billing
│   └── BillingManager.kt                    # Google Play Billing wrapper
│
├── security/                                # Security Features
│   └── BiometricManager.kt                  # Biometric auth wrapper
│
├── worker/                                  # Background Jobs
│   ├── SMSScanWorker.kt                     # Periodic SMS scanning
│   └── ReminderWorker.kt                    # Payment reminders
│
├── export/                                  # Export Utilities
│   ├── CSVExporter.kt                       # CSV generation
│   └── PDFExporter.kt                       # PDF generation
│
└── utils/                                   # Utility Classes
    ├── DateUtils.kt
    ├── CurrencyUtils.kt
    └── PermissionUtils.kt
```

### All 11 Screens

1. **Splash Screen** - App initialization and branding
2. **Permission Explainer** - SMS permission rationale
3. **Scan Progress** - Real-time SMS scanning with progress
4. **Home Dashboard** - Summary cards and top merchants
5. **Recurring List** - All detected recurring charges (FREE: 5 items max)
6. **Upcoming Charges** - Predicted future payments (PRO only)
7. **Category Breakdown** - Visual insights with charts (Pie + Bar)
8. **Export Screen** - CSV/PDF export options (PRO only)
9. **Paywall** - Subscription purchase flow
10. **Settings** - App preferences and account management
11. **App Lock** - PIN/Biometric entry screen

---

## ✨ Features

### FREE Features (No Subscription Required)

✅ **Up to 5 Recurring Items** - View your top 5 recurring charges  
✅ **Basic Dashboard** - Monthly total and summary cards  
✅ **Category Breakdown** - Basic spending insights  
✅ **SMS Scanning** - Automatic transaction detection  
✅ **Notifications** - Basic payment reminders  
✅ **App Lock** - PIN protection with biometric unlock  

### PRO Features (Subscription Required)

💎 **Unlimited Recurring Items** - Track all your subscriptions  
💎 **Upcoming Charges** - 30-day payment predictions  
💎 **Advanced Analytics** - Detailed charts and trends  
💎 **Export Data** - CSV and PDF reports  
💎 **Priority Support** - Email support for PRO users  
💎 **Ad-Free Experience** - No promotional banners  

### Monetization Strategy

- **Model**: Freemium with annual subscription
- **Product ID**: `autodebit_pro_yearly`
- **Base Plan**: `yearly_base`
- **Price**: ₹499/year (~$6 USD/year)
- **Free Tier Limit**: 5 recurring items maximum
- **Upgrade Prompt**: Blur items after limit + "Unlock PRO" card in list
- **Billing**: Google Play Billing Library v7.0.0
- **Purchase Flow**: Integrated in PaywallScreen with one-click subscribe
- **Restore Purchases**: Available in Settings and Paywall

---

## 🚀 Setup Instructions

### Prerequisites

Before you begin, ensure you have the following installed:

- **Android Studio**: Hedgehog (2023.1.1) or later (recommend Iguana or Jellyfish)
- **JDK**: Java Development Kit 17 or higher
- **Android SDK**: API 34 (Android 14) via SDK Manager
- **Gradle**: 8.7 (included via wrapper - no manual installation needed)
- **Git**: For cloning the repository

### Clone and Build

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/AutoDebit-Detective.git
   cd AutoDebit-Detective
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select **File** > **Open**
   - Navigate to the cloned `AutoDebit-Detective` directory
   - Click **OK**

3. **Sync Gradle dependencies**
   - Android Studio will automatically prompt to sync Gradle
   - Or manually sync: **File** > **Sync Project with Gradle Files**
   - Wait for dependencies to download (~2-5 minutes on first run)

4. **Build the project**
   ```bash
   # Clean and build
   ./gradlew clean build
   
   # Build debug APK
   ./gradlew assembleDebug
   
   # Build release AAB
   ./gradlew bundleRelease
   ```

### Run on Emulator/Device

**Option 1: Using Android Studio**
1. Connect an Android device with USB debugging enabled, OR
2. Start an Android emulator (API 24+ recommended: API 34)
3. Click the **Run** button (green play icon) in Android Studio toolbar
4. Select your device/emulator from the list

**Option 2: Using Command Line**
```bash
# Install debug build on connected device
./gradlew installDebug

# Launch the app
adb shell am start -n com.zaheer.autodebitdetective.debug/com.zaheer.autodebitdetective.MainActivity
```

### Build Variants

| Variant | Description | Package ID | Use Case |
|---------|-------------|------------|----------|
| **debug** | Development build | `com.zaheer.autodebitdetective.debug` | Testing, development |
| **release** | Production build | `com.zaheer.autodebitdetective` | Play Store deployment |

---

## 🏪 Google Play Console Setup (DETAILED)

### Step 1: Create App in Play Console

1. Go to [Google Play Console](https://play.google.com/console)
2. Click **Create app**
3. Fill in details:
   - **App name**: AutoDebit Detective
   - **Default language**: English (United States)
   - **App or game**: App
   - **Free or paid**: Free
4. Complete declarations and click **Create app**

### Step 2: Create Subscription Product

1. Navigate to **Monetize** > **Products** > **Subscriptions**
2. Click **Create subscription**
3. Fill in product details:

   **Product ID**: `autodebit_pro_yearly` (MUST match exactly)
   
   **Name**: AutoDebit PRO - Annual
   
   **Description**:
   ```
   Upgrade to AutoDebit PRO for unlimited recurring items, 
   upcoming charge predictions, advanced analytics, and export features.
   ```

4. Click **Add base plan**

### Step 3: Configure Base Plan

1. **Base plan ID**: `yearly_base` (MUST match exactly)
2. **Billing period**: `Yearly (P1Y)`
3. **Price**: Click **Set price**
   - Region: India
   - Price: ₹499
   - Click **Apply prices to other countries** (Google will suggest local pricing)
4. **Auto-renew**: Enabled (default)
5. **Grace period**: 3 days (recommended)
6. **Free trial**: Optional - 7 days (can attract more users)
7. Click **Save** and then **Activate**

### Step 4: Add License Testers

Testing in-app billing requires license testers to avoid real charges.

1. Go to **Setup** > **License testing**
2. Add test Gmail accounts (comma-separated):
   ```
   yourtest@gmail.com, developer@gmail.com
   ```
3. **License response**: Set to **RESPOND_NORMALLY**
4. Click **Save**

**Important**: Testers must opt-in via the testing link before testing purchases.

### Step 5: Internal Testing Track

1. Navigate to **Release** > **Testing** > **Internal testing**
2. Click **Create new release**
3. Upload AAB:
   ```bash
   # Generate signed release AAB (see "Building for Release" section below)
   ./gradlew bundleRelease
   ```
   - Upload: `app/build/outputs/bundle/release/app-release.aab`
4. **Release name**: 1.0.0 (1)
5. **Release notes**:
   ```
   Initial internal testing release
   - SMS-based recurring charge detection
   - FREE: Up to 5 recurring items
   - PRO: Unlimited items, analytics, export
   ```
6. Click **Save** > **Review release** > **Start rollout to Internal testing**

### Step 6: Add Internal Testers

1. Go to **Testing** > **Internal testing** > **Testers** tab
2. Create an email list:
   - **List name**: Internal Testers
   - **Add emails**: yourtest@gmail.com, developer@gmail.com
3. Copy the **opt-in URL** and share with testers
4. Testers must:
   - Open the opt-in URL in a browser
   - Click **Become a tester**
   - Download the app from Play Store (using the same Google account)

### Step 7: SMS Permission Declaration (CRITICAL)

Google requires policy compliance for apps requesting SMS permissions.

1. Go to **Policy** > **App content** > **Sensitive permissions**
2. Click **Manage** under SMS/Call Log permissions
3. **Select permission**: `READ_SMS`
4. **Provide a declaration**:
   
   **Core functionality**:
   ```
   AutoDebit Detective requires READ_SMS permission to scan transaction 
   messages and automatically detect recurring charges and subscriptions. 
   This is the primary functionality of the app.
   ```
   
   **Usage description**:
   ```
   The app analyzes SMS messages locally on the device to identify 
   patterns in transaction messages (debits, subscriptions). No SMS 
   data is uploaded to servers or shared with third parties.
   ```
   
   **Video demonstration**: Upload a screen recording showing:
   - Permission request dialog
   - SMS messages being scanned
   - Recurring charges being displayed
   
5. Click **Save**

**Video Requirements**:
- Less than 30 MB
- Format: MP4, MOV, or AVI
- Show the permission request flow clearly
- Demonstrate how SMS data is used

### Step 8: Complete All Store Listing Requirements

1. **Main store listing**:
   - App name: AutoDebit Detective
   - Short description (80 chars): Find hidden recurring charges by scanning your SMS messages
   - Full description (4000 chars): [Full Play Store description]
   - App icon: 512x512 PNG
   - Feature graphic: 1024x500 PNG
   - Phone screenshots: At least 2 (1080x1920 recommended)

2. **Content rating**:
   - Complete the questionnaire
   - Financial tracking apps typically get "Everyone" rating

3. **Target audience and content**:
   - Target age: 18+
   - App is primarily for adults managing finances

4. **Privacy policy**:
   - **Required**: Host a privacy policy and provide the URL
   - Must explain SMS data handling

5. **Data safety**:
   - Data collected: None (all data stays on device)
   - Data shared: None
   - Security practices: Data is encrypted in transit and at rest

---

## 🔨 Building for Release

### Step 1: Create a Keystore

First-time setup only. Keep your keystore file safe - you'll need it for all future updates!

```bash
# Navigate to your project root
cd /path/to/AutoDebit-Detective

# Generate keystore
keytool -genkey -v \
  -keystore autodebit-release-key.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias autodebit-key

# You'll be prompted for:
# - Keystore password (remember this!)
# - Key password (remember this!)
# - Your name/organization details
```

**IMPORTANT**: Store your keystore file and passwords securely! If you lose them, you cannot update your app on Play Store.

### Step 2: Configure Signing in Gradle

Create `keystore.properties` in your project root (NEVER commit this file):

```properties
# keystore.properties
storeFile=autodebit-release-key.jks
storePassword=YourStorePassword
keyAlias=autodebit-key
keyPassword=YourKeyPassword
```

Add to `.gitignore`:
```
keystore.properties
*.jks
*.keystore
```

Update `app/build.gradle.kts` (if not already configured):

```kotlin
// Add at the top
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    // ... existing config ...
    
    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] ?: "")
            storePassword = keystoreProperties["storePassword"] as String?
            keyAlias = keystoreProperties["keyAlias"] as String?
            keyPassword = keystoreProperties["keyPassword"] as String?
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... existing release config ...
        }
    }
}
```

### Step 3: Build Signed AAB

```bash
# Clean previous builds
./gradlew clean

# Build signed release AAB (Android App Bundle - preferred for Play Store)
./gradlew bundleRelease

# Output location:
# app/build/outputs/bundle/release/app-release.aab
```

**AAB vs APK**: Google Play requires AAB format for new apps. AABs allow Google to optimize APKs for different device configurations.

### Step 4: Build Signed APK (Optional)

If you need a standalone APK for testing or sideloading:

```bash
# Build signed release APK
./gradlew assembleRelease

# Output location:
# app/build/outputs/apk/release/app-release.apk
```

### Step 5: Upload to Play Console

1. Go to **Release** > **Production** (or **Testing** > **Internal testing**)
2. Click **Create new release**
3. Upload `app-release.aab`
4. Add release notes
5. Click **Review release** > **Start rollout to Production**

### ProGuard Optimization

The release build automatically applies ProGuard rules (defined in `app/proguard-rules.pro`):
- Code obfuscation
- Dead code removal
- Optimization for smaller APK size

Verify ProGuard didn't break anything:
```bash
# Install release build and test thoroughly
adb install app/build/outputs/apk/release/app-release.apk
```

---

## 🔐 Privacy & Permissions

### Permissions Required

| Permission | Usage | Required | Rationale |
|------------|-------|----------|-----------|
| `READ_SMS` | Read transaction SMS messages | **Yes** | Core functionality - detects recurring charges |
| `POST_NOTIFICATIONS` | Show payment reminders | Optional | Payment alerts (Android 13+) |
| `INTERNET` | In-app billing, (future updates) | **Yes** | Google Play Billing requires internet |

### Privacy-First Architecture

✅ **100% Local Processing** - All SMS data is processed on-device  
✅ **No Data Upload** - Zero SMS data is sent to servers  
✅ **No Analytics Tracking** - No third-party analytics SDKs  
✅ **No Ads** - No advertising libraries included  
✅ **Encrypted Storage** - Sensitive data encrypted with DataStore  
✅ **Biometric Protection** - Optional app lock with fingerprint/face  

### SMS Permission Explanation

**Why we need SMS permission:**
- Automatically detect recurring charges from bank/payment SMS
- Identify subscription patterns without manual entry
- Track payment amounts, dates, and merchants

**What we DON'T do:**
- ❌ We don't read personal messages
- ❌ We don't send SMS from your device
- ❌ We don't upload SMS data to servers
- ❌ We don't share data with third parties

**User Control:**
- Permission is requested with full explanation
- Users can deny permission and add items manually
- Users can revoke permission anytime in Android settings

### Play Store Policy Compliance

This app complies with Google Play's [SMS & Call Log Policy](https://support.google.com/googleplay/android-developer/answer/10208820):
- SMS permission is used for core app functionality (financial tracking)
- Clear in-app disclosure provided before permission request
- Privacy policy explains data handling
- No SMS data is uploaded or shared

---

## 🧪 Testing

### Unit Tests

Run unit tests for business logic:

```bash
# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests SMSParserTest

# Run with coverage report
./gradlew testDebugUnitTest jacocoTestReport
```

**Test Coverage Includes:**
- `SMSParserTest.kt` - SMS message parsing logic
- `RecurringDetectorTest.kt` - Recurring pattern detection
- Repository tests (transaction CRUD)
- ViewModel tests (state management)

### Instrumented Tests (UI Tests)

Run UI tests on a connected device or emulator:

```bash
# Run all instrumented tests
./gradlew connectedAndroidTest

# Run on specific device
adb devices  # List devices
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.zaheer.autodebitdetective.MainActivityTest
```

### Lint Checks

```bash
# Run lint analysis
./gradlew lint

# View report
open app/build/reports/lint-results.html
```

### Manual Testing Checklist

Before releasing, manually verify:

- [ ] SMS permission request flow
- [ ] Scan progress shows correctly
- [ ] Recurring items display properly
- [ ] FREE limit (5 items) enforced with blur
- [ ] Paywall purchase flow works (use test account)
- [ ] Restore purchases functionality
- [ ] Export CSV/PDF (PRO users only)
- [ ] App lock with PIN and biometric
- [ ] Notifications appear on time
- [ ] Settings persist correctly
- [ ] Dark mode and light mode themes
- [ ] Navigation between all 11 screens
- [ ] Error states handled gracefully

---

## 💻 Technology Stack

### Complete Dependency List

```kotlin
// Core AndroidX
androidx.core:core-ktx:1.13.1
androidx.lifecycle:lifecycle-runtime-ktx:2.8.4
androidx.lifecycle:lifecycle-runtime-compose:2.8.4
androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4
androidx.activity:activity-compose:1.9.1
androidx.appcompat:appcompat:1.7.0

// Jetpack Compose (BOM 2024.08.00)
androidx.compose.ui:ui
androidx.compose.ui:ui-graphics
androidx.compose.ui:ui-tooling-preview
androidx.compose.material3:material3
androidx.compose.material:material-icons-extended

// Room Database
androidx.room:room-runtime:2.6.1
androidx.room:room-ktx:2.6.1
androidx.room:room-compiler:2.6.1 (KSP)

// DataStore
androidx.datastore:datastore-preferences:1.1.1

// WorkManager
androidx.work:work-runtime-ktx:2.9.0

// Navigation
androidx.navigation:navigation-compose:2.7.7

// Kotlin Coroutines
org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1
org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1

// Google Play Billing
com.android.billingclient:billing-ktx:7.0.0

// Biometric Authentication
androidx.biometric:biometric:1.2.0-alpha05

// Material Design
com.google.android.material:material:1.12.0

// Testing
junit:junit:4.13.2
androidx.test.ext:junit:1.2.1
androidx.test.espresso:espresso-core:3.6.1
androidx.compose.ui:ui-test-junit4

// Debug Tools
androidx.compose.ui:ui-tooling
androidx.compose.ui:ui-test-manifest

// Build Tools
com.google.devtools.ksp:2.0.0-1.0.23 (Kotlin Symbol Processing)
```

### Architecture Components

- **MVVM Pattern**: Separation of concerns with ViewModel and UI layers
- **Repository Pattern**: Abstraction layer for data sources
- **Use Cases**: Business logic encapsulation in domain layer
- **StateFlow**: Reactive state management for UI
- **Coroutines**: Asynchronous programming
- **Room + KSP**: Type-safe database with compile-time verification
- **DataStore**: Modern key-value storage replacing SharedPreferences
- **Dependency Injection**: Ready for Hilt/Koin integration

### Design Patterns Used

- **Singleton**: Database, Repository instances
- **Factory**: ViewModel creation
- **Observer**: StateFlow/LiveData for reactive UI
- **Strategy**: Different export formats (CSV, PDF)
- **Builder**: Notification creation
- **Adapter**: Room type converters

---

## 📄 License

This project is licensed under the **MIT License**.

```
MIT License

Copyright (c) 2024 Zaheer

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

**Alternative**: If you prefer to keep this proprietary, replace with:

```
Copyright (c) 2024 Zaheer. All Rights Reserved.

This software is proprietary and confidential. Unauthorized copying, 
modification, distribution, or use of this software is strictly prohibited.
```

---

## 👨‍💻 Author & Contact

**Developer**: Zaheer  
**Package**: `com.zaheer.autodebitdetective`  
**Version**: 1.0.0  
**Email**: [your-email@example.com]  
**GitHub**: [@yourusername](https://github.com/yourusername)  
**Play Store**: [Link when published]

---

## 🤝 Contributing

Contributions are welcome! Whether it's bug fixes, new features, or documentation improvements, feel free to submit a Pull Request.

### How to Contribute

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/AmazingFeature
   ```
3. **Make your changes** and commit
   ```bash
   git commit -m "Add some AmazingFeature"
   ```
4. **Push to your fork**
   ```bash
   git push origin feature/AmazingFeature
   ```
5. **Open a Pull Request** with a detailed description

### Development Guidelines

- Follow Kotlin coding conventions
- Use Material 3 design guidelines
- Write unit tests for new features
- Update documentation for API changes
- Test on multiple Android versions (API 24-34)
- Ensure no lint warnings before submitting

---

## 📞 Support

### Getting Help

- **GitHub Issues**: [Report bugs or request features](https://github.com/yourusername/AutoDebit-Detective/issues)
- **Discussions**: [Ask questions or share ideas](https://github.com/yourusername/AutoDebit-Detective/discussions)
- **Email**: [your-email@example.com] for PRO user support

### FAQ

**Q: Why does the app need SMS permission?**  
A: It's the only way to automatically detect recurring charges from transaction messages. All processing happens locally on your device.

**Q: Is my SMS data uploaded to servers?**  
A: Absolutely not. 100% of your data stays on your device. We don't have servers to upload to!

**Q: What's the difference between FREE and PRO?**  
A: FREE users can track up to 5 recurring items. PRO unlocks unlimited items, upcoming predictions, export, and advanced analytics for ₹499/year.

**Q: Can I get a refund?**  
A: Yes, Google Play offers refunds within the first 48 hours. Contact us for assistance.

**Q: Does this work with all banks?**  
A: The app supports major Indian banks and payment services. If we miss a transaction, please report it so we can improve our parser.

---

## 🗺️ Roadmap

### Planned Features

- [ ] **Notification Channels**: Customizable alert types
- [ ] **Widgets**: Home screen widgets for quick overview
- [ ] **Manual Entry**: Add non-SMS subscriptions manually
- [ ] **Budget Alerts**: Set spending limits per category
- [ ] **Multi-Currency**: Support for international transactions
- [ ] **Cloud Backup**: Optional encrypted cloud backup (with user consent)
- [ ] **Family Sharing**: Share subscription tracking with family (PRO+)
- [ ] **Bill Splitting**: Track shared subscriptions
- [ ] **Auto-Cancel Assistant**: Direct links to cancel subscriptions

### Version History

**v1.0.0** (Current) - Initial Release
- SMS-based recurring charge detection
- 11 complete screens with Material 3 UI
- FREE/PRO tier with billing integration
- Export to CSV/PDF
- Biometric app lock
- Category-based insights

---

## 🙏 Acknowledgments

- **Android Team** for Jetpack Compose and modern Android libraries
- **Material Design** for the beautiful design system
- **Open Source Community** for inspiration and code examples
- **Beta Testers** for valuable feedback

---

<div align="center">

### 🌟 Star this repository if you find it helpful!

**Made with ❤️ using Kotlin and Jetpack Compose**

[Report Bug](https://github.com/yourusername/AutoDebit-Detective/issues) · 
[Request Feature](https://github.com/yourusername/AutoDebit-Detective/issues) · 
[Documentation](https://github.com/yourusername/AutoDebit-Detective/wiki)

---

© 2024 Zaheer. All rights reserved.

</div>
