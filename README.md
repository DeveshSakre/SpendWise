# 💰 SpendWise

![Kotlin](https://img.shields.io/badge/Kotlin-2.1-purple?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![Android](https://img.shields.io/badge/Android-15-3DDC84?logo=android&logoColor=white)
![Room Database](https://img.shields.io/badge/Room-Database-FF9800)
![MVVM](https://img.shields.io/badge/Architecture-MVVM-2196F3)
![Material 3](https://img.shields.io/badge/UI-Material%203-6200EE)
![License](https://img.shields.io/badge/License-MIT-green)

<p align="center">
  <b>A modern AI-powered personal finance and expense tracking application built with Kotlin, Jetpack Compose and Material Design 3.</b>
</p>

<p align="center">
Track expenses automatically through UPI SMS detection, manage monthly budgets, analyse spending habits, generate financial reports and gain AI-powered insights — all in a beautiful native Android experience.
</p>

---

# ✨ Features

## 💸 Expense Management

- Add, Edit & Delete Expenses
- Expense Details Screen
- Manual Expense Entry
- Category Management
- Multiple Payment Methods (UPI, Cash, Card)
- Merchant Notes
- Real-time Expense Updates

---

## 📩 Automatic UPI SMS Detection

- Automatically detects UPI debit transactions
- Parses SMS locally on the device
- Detects:
  - Amount
  - Merchant
  - Payment Method
  - Transaction Date
- Automatically creates expense entries
- Intelligent merchant categorisation
- Completely offline processing

---

## 🎯 Budget Management

- Monthly Budget Planning
- Category-wise Budgets
- Budget Utilisation Tracking
- Remaining Budget Calculation
- Budget Progress Indicators
- Budget Status Monitoring

---

## 📊 Analytics Dashboard

- Monthly Spending Overview
- Category-wise Analysis
- Budget Utilisation
- Spending Trends
- Top Spending Categories
- Top Merchants
- Interactive Charts
- Financial Statistics

---

## 🤖 AI Financial Insights

Generate intelligent insights using local expense data.

Examples:

- Spending Trends
- Budget Analysis
- Highest Spending Category
- Average Daily Spending
- Most Frequent Merchant
- Spending Behaviour Analysis
- Budget Predictions
- Financial Recommendations

---

## 💬 AI Financial Assistant

Offline AI-powered financial assistant capable of answering questions like:

- How much did I spend this month?
- Which category has the highest spending?
- Show my food expenses.
- What is my remaining budget?
- Show expenses above ₹1000.
- Which merchant did I spend the most at?

---

## 📄 Monthly Financial Reports

Generate comprehensive monthly reports including:

- Total Spending
- Monthly Budget
- Remaining Budget
- Category Breakdown
- Top Merchant
- Highest Expense
- Average Daily Spending
- AI Summary

Export Reports as:

- PDF
- CSV

---

## 🔍 Search & Smart Filters

Search Expenses by:

- Merchant
- Notes
- Category
- Payment Method

Advanced Filters:

- Today
- This Week
- This Month
- Custom Date Range
- Highest Amount
- Lowest Amount
- Newest
- Oldest

---

## 🏠 Android Home Screen Widget

Quickly view financial information directly from your Home Screen.

Displays:

- Monthly Spending
- Remaining Budget
- Budget Progress
- Quick Add Expense
- Analytics Shortcut

---

# 🏗 Architecture

```
                 MVVM Architecture

        UI (Jetpack Compose)
                 │
             ViewModel
                 │
            Repository
                 │
          Room Database
```

---

# 📂 Project Structure

```
com.devesh.spendwise
│
├── data
│   ├── local
│   ├── repository
│
├── navigation
│
├── sms
│
├── util
│
├── widget
│
├── ui
│   ├── add
│   ├── analytics
│   ├── assistant
│   ├── budget
│   ├── details
│   ├── list
│   ├── reports
│   ├── search
│   └── theme
│
└── MainActivity.kt
```

---

# 🛠 Tech Stack

### Language

- Kotlin

### UI

- Jetpack Compose
- Material Design 3
- Navigation Compose

### Architecture

- MVVM
- Repository Pattern
- StateFlow
- Kotlin Coroutines

### Database

- Room Database

### Android Components

- Broadcast Receiver
- SMS Receiver
- Android Home Screen Widget

### Development Tools

- Android Studio
- Stitch (UI Design)
- Antigravity (AI-Assisted Development)

---

# 📸 Screenshots

<p align="center">

<img src="screenshots/HomeScreen.png" width="250"/>

<img src="screenshots/AddExpense.png" width="250"/>

<img src="screenshots/Analytics.png" width="250"/>

<img src="screenshots/Budget.png" width="250"/>

<img src="screenshots/Search.png" width="250"/>

<img src="screenshots/AIInsights.png" width="250"/>

</p>

---

# 🚀 Getting Started

### Clone Repository

```bash
git clone https://github.com/DeveshSakre/SpendWise.git
```

### Open Project

Open the project using Android Studio.

### Run

Build and run on an Android device or emulator.

---

# 📱 Minimum Requirements

- Android 8.0 (API 26) or above
- Kotlin
- Android Studio Narwhal or newer

---

# 🔐 Permissions

SpendWise requires:

- RECEIVE_SMS
- READ_SMS

These permissions are used only for automatic UPI expense detection.

---

# ⚠ Disclaimer

- SMS parsing depends on the transaction message format used by different banks and payment providers.
- This project is intended for educational and personal use.
- Automatic SMS reading may not comply with Google Play policies for general-purpose finance applications.

---

# 🚀 Future Enhancements

- Financial Health Score
- Savings Goals
- Better Merchant Recognition
- Subscription Detection
- Material You Improvements
- Voice Expense Entry
- OCR Receipt Scanner
- Wear OS Companion
- Local Backup & Restore
- Cloud Synchronisation

---

# 🤝 Contributing

Contributions, feature suggestions and pull requests are welcome.

If you have ideas to improve SpendWise, feel free to fork the repository and submit a Pull Request.

---

# ⭐ Support

If you like this project, please consider giving it a **⭐ Star** on GitHub.

It helps the project reach more developers.

---

# 👨‍💻 Author

### **Devesh Sakre**

Android Developer • Kotlin • Jetpack Compose • AI Enthusiast

GitHub: https://github.com/DeveshSakre

---

<p align="center">
Made with ❤️ using Kotlin, Jetpack Compose and Material Design 3.
</p>
