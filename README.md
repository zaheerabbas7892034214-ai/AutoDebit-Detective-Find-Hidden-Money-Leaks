# AutoDebit Detective - Find Hidden Money Leaks 💰🔍

<div align="center">

![Android](https://img.shields.io/badge/Android-34-green?logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-blue?logo=kotlin)
![MinSdk](https://img.shields.io/badge/MinSdk-24-orange)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.08-brightgreen)
![License](https://img.shields.io/badge/License-MIT-yellow)

**AutoDebit Detective** is an intelligent Android app that helps you discover and manage hidden recurring charges and subscriptions by analyzing your SMS messages.

</div>

---

## 📱 Features

- 🔍 **Smart SMS Scanning**: Automatically detects subscription and recurring payment messages
- 💳 **Subscription Tracking**: Keep track of all your active subscriptions in one place
- 📊 **Analytics Dashboard**: Visualize your spending patterns and identify money leaks
- 🔔 **Payment Reminders**: Get notified before upcoming payments
- 🔐 **Biometric Security**: Secure your financial data with fingerprint/face authentication
- 📤 **Export Reports**: Export your subscription data for analysis
- 🌙 **Material Design 3**: Modern UI with Material You theming

---

## 🏗️ Project Structure

```
AutoDebit Detective/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/zaheer/autodebitdetective/
│   │       │   ├── MainActivity.kt
│   │       │   └── AutoDebitDetectiveApp.kt
│   │       ├── res/
│   │       │   ├── values/
│   │       │   │   ├── strings.xml
│   │       │   │   ├── themes.xml
│   │       │   │   └── colors.xml
│   │       │   └── xml/
│   │       │       ├── file_paths.xml
│   │       │       ├── backup_rules.xml
│   │       │       └── data_extraction_rules.xml
│   │       └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
│       ├── gradle-wrapper.properties
│       └── gradle-wrapper.jar
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

---

## 🛠️ Tech Stack

### Core Technologies
- **Language**: Kotlin 2.0.0
- **UI Framework**: Jetpack Compose (Material 3)
- **Build System**: Gradle 8.7 with Kotlin DSL
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

### Architecture & Libraries

| Category | Library | Version | Purpose |
|----------|---------|---------|---------|
| **Architecture** | AndroidX Core KTX | 1.13.1 | Core Android extensions |
| | Lifecycle | 2.8.4 | Lifecycle-aware components |
| **UI** | Compose BOM | 2024.08.00 | Compose UI toolkit |
| | Material 3 | Latest | Material Design 3 components |
| | Navigation Compose | 2.7.7 | Navigation framework |
| **Database** | Room | 2.6.1 | Local database (SQLite) |
| | KSP | 2.0.0-1.0.23 | Kotlin Symbol Processing |
| **Data Storage** | DataStore | 1.1.1 | Preferences storage |
| **Background Work** | WorkManager | 2.9.0 | Background task scheduling |
| **Monetization** | Billing Client | 7.0.0 | In-app purchases & subscriptions |
| **Security** | Biometric | 1.2.0-alpha05 | Biometric authentication |
| **Async** | Coroutines | 1.8.1 | Asynchronous programming |

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: Java 17 or higher
- **Android SDK**: API 34 (Android 14)
- **Gradle**: 8.7 (included via wrapper)

### Building the Project

1. **Clone the repository**
   ```bash
   git clone https://github.com/zaheer/AutoDebit-Detective.git
   cd AutoDebit-Detective
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Sync Gradle**
   ```bash
   ./gradlew build
   ```

4. **Run the app**
   - Connect an Android device or start an emulator
   - Click "Run" or use `./gradlew installDebug`

### Build Variants

- **Debug**: Development build with debugging enabled
  - Package: `com.zaheer.autodebitdetective.debug`
  - Minification: Disabled

- **Release**: Production build with optimization
  - Package: `com.zaheer.autodebitdetective`
  - Minification: Enabled (R8)
  - ProGuard rules applied

---

## 🔐 Permissions

The app requires the following permissions:

- **READ_SMS**: To scan messages for subscription information
- **POST_NOTIFICATIONS**: To send payment reminders and alerts
- **INTERNET**: For in-app purchases and updates (if applicable)

All sensitive data is stored locally and never transmitted without explicit user consent.

---

## 📦 Key Features Implementation

### SMS Analysis
- Pattern recognition for subscription keywords
- Support for multiple payment providers
- Automatic amount and date extraction

### Database Schema
- Subscriptions table with Room ORM
- Transaction history tracking
- Category management

### Background Processing
- WorkManager for periodic SMS scanning
- Payment reminder notifications
- Database cleanup tasks

### Security
- Biometric authentication for app access
- Encrypted DataStore for preferences
- ProGuard rules for code obfuscation

---

## 🧪 Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# Lint checks
./gradlew lint
```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Zaheer**
- Package: com.zaheer.autodebitdetective
- Version: 1.0.0

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📞 Support

If you encounter any issues or have questions:
- Open an issue on GitHub
- Check the [Wiki](https://github.com/zaheer/AutoDebit-Detective/wiki) for documentation

---

<div align="center">

**Made with ❤️ and Kotlin**

⭐ Star this repo if you find it helpful!

</div>
