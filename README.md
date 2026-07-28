# 💰 SpendWise

<p align="center">
  <img src="screenshots/logo.png" width="120" alt="SpendWise Logo"/>
</p>

<p align="center">
  <b>A Modern AI-Powered Personal Finance & Expense Tracker built with Kotlin, Jetpack Compose and Material 3.</b>
</p>

<p align="center">
  Track expenses automatically, manage budgets, analyse spending patterns, generate monthly reports and gain intelligent financial insights — all with a beautiful native Android experience.
</p>

---

## ✨ Features

### 💸 Expense Management

- Add, edit and delete expenses
- View complete expense details
- Categorise expenses
- Support for UPI, Cash and Card payments
- Manual and automatic expense tracking

---

### 📩 Automatic UPI SMS Detection

- Detects UPI debit SMS automatically
- Extracts:
  - Amount
  - Merchant / Receiver
  - Payment Method
  - Date
- Automatically creates expense entries
- Intelligent merchant categorisation
- Works completely offline

---

### 🎯 Budget Management

- Monthly budget planning
- Category-wise budgets
- Budget utilisation tracking
- Remaining budget calculation
- Budget progress indicators
- Smart budget status

---

### 📊 Analytics Dashboard

- Monthly spending overview
- Category-wise spending
- Budget utilisation
- Spending trends
- Top merchants
- Highest spending categories
- Interactive charts and visualisations

---

### 🤖 AI Financial Insights

Generate intelligent insights from your spending history:

- Spending trends
- Budget analysis
- Highest spending category
- Most visited merchant
- Average daily spending
- Budget predictions
- Financial recommendations

---

### 💬 AI Financial Assistant

Offline AI-powered financial assistant capable of answering questions like:

- How much did I spend this month?
- What is my remaining budget?
- Which category has the highest spending?
- Show my Food expenses.
- Show expenses above ₹1000.

---

### 📄 Monthly Financial Reports

Generate detailed monthly reports including:

- Total spending
- Budget
- Remaining balance
- Category breakdown
- Top merchants
- Highest transaction
- Average daily spending
- AI summary

Export reports as:

- PDF
- CSV

---

### 🔍 Search & Smart Filters

Search expenses by:

- Merchant
- Notes
- Category
- Payment Method

Advanced filters:

- Today
- This Week
- This Month
- Custom Date
- Highest Amount
- Lowest Amount
- Newest
- Oldest

---

### 🏠 Android Home Screen Widget

Quickly access your financial summary directly from the home screen.

Displays:

- Current month spending
- Remaining budget
- Budget progress
- Quick Add Expense
- Analytics shortcut

---

## 🏗 Architecture

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

## 📂 Project Structure

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

## 🛠 Tech Stack

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
- Home Screen Widget
- SMS Manager
- App Widgets

### Development Tools

- Android Studio
- Stitch (UI Design)
- Antigravity (AI-assisted Development)

---

## 📸 Screenshots

<p align="center">

<img src="screenshots/HomeScreen.png" width="260"/>

<img src="screenshots/AddExpense.png" width="260"/>

<img src="screenshots/Analytics.png" width="260"/>

<img src="screenshots/Budget.png" width="260"/>

<img src="screenshots/Search.png" width="260"/>

<img src="screenshots/AIInsights.png" width="260"/>

</p>

---

## 🚀 Installation

Clone the repository

```bash
git clone https://github.com/DeveshSakre/SpendWise.git
```

Open the project in Android Studio.

Build and Run on an Android device.

---

## 📌 Permissions

SpendWise uses the following Android permissions:

- RECEIVE_SMS
- READ_SMS

These permissions are used only for automatic UPI transaction detection.

---

## ⚠ Disclaimer

This project is intended for educational and personal-use purposes.

Automatic SMS parsing depends on the SMS format provided by different banks and payment providers.

Google Play currently restricts SMS permissions for general-purpose finance apps, so this implementation is designed for learning and personal use.

---

## 🚀 Future Enhancements

- Better merchant recognition
- Subscription detection
- Savings goals
- Financial health score
- Material You improvements
- Cloud backup
- Multi-device sync
- Voice expense entry
- OCR receipt scanning
- Wear OS companion app

---

## 🤝 Contributing

Contributions, suggestions and feature requests are always welcome.

Feel free to fork the repository and submit a Pull Request.

---

## ⭐ Support

If you found this project useful, please consider giving it a ⭐ on GitHub.

It helps the project reach more developers.

---

## 👨‍💻 Author

**Devesh Sakre**

Android Developer | Kotlin | Jetpack Compose | AI Enthusiast

GitHub:
https://github.com/DeveshSakre

---

<p align="center">
Made with ❤️ using Kotlin, Jetpack Compose and Android.
</p>

# 💰 SpendWise

<p align="center">
  <img src="screenshots/logo.png" width="120" alt="SpendWise Logo"/>
</p>

<p align="center">
  <b>A Modern AI-Powered Personal Finance & Expense Tracker built with Kotlin, Jetpack Compose and Material 3.</b>
</p>

<p align="center">
  Track expenses automatically, manage budgets, analyse spending patterns, generate monthly reports and gain intelligent financial insights — all with a beautiful native Android experience.
</p>

---

## ✨ Features

### 💸 Expense Management

- Add, edit and delete expenses
- View complete expense details
- Categorise expenses
- Support for UPI, Cash and Card payments
- Manual and automatic expense tracking

---

### 📩 Automatic UPI SMS Detection

- Detects UPI debit SMS automatically
- Extracts:
  - Amount
  - Merchant / Receiver
  - Payment Method
  - Date
- Automatically creates expense entries
- Intelligent merchant categorisation
- Works completely offline

---

### 🎯 Budget Management

- Monthly budget planning
- Category-wise budgets
- Budget utilisation tracking
- Remaining budget calculation
- Budget progress indicators
- Smart budget status

---

### 📊 Analytics Dashboard

- Monthly spending overview
- Category-wise spending
- Budget utilisation
- Spending trends
- Top merchants
- Highest spending categories
- Interactive charts and visualisations

---

### 🤖 AI Financial Insights

Generate intelligent insights from your spending history:

- Spending trends
- Budget analysis
- Highest spending category
- Most visited merchant
- Average daily spending
- Budget predictions
- Financial recommendations

---

### 💬 AI Financial Assistant

Offline AI-powered financial assistant capable of answering questions like:

- How much did I spend this month?
- What is my remaining budget?
- Which category has the highest spending?
- Show my Food expenses.
- Show expenses above ₹1000.

---

### 📄 Monthly Financial Reports

Generate detailed monthly reports including:

- Total spending
- Budget
- Remaining balance
- Category breakdown
- Top merchants
- Highest transaction
- Average daily spending
- AI summary

Export reports as:

- PDF
- CSV

---

### 🔍 Search & Smart Filters

Search expenses by:

- Merchant
- Notes
- Category
- Payment Method

Advanced filters:

- Today
- This Week
- This Month
- Custom Date
- Highest Amount
- Lowest Amount
- Newest
- Oldest

---

### 🏠 Android Home Screen Widget

Quickly access your financial summary directly from the home screen.

Displays:

- Current month spending
- Remaining budget
- Budget progress
- Quick Add Expense
- Analytics shortcut

---

## 🏗 Architecture

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

## 📂 Project Structure

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

## 🛠 Tech Stack

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
- Home Screen Widget
- SMS Manager
- App Widgets

### Development Tools

- Android Studio
- Stitch (UI Design)
- Antigravity (AI-assisted Development)

---

## 📸 Screenshots

<p align="center">

<img src="screenshots/HomeScreen.png" width="260"/>

<img src="screenshots/AddExpense.png" width="260"/>

<img src="screenshots/Analytics.png" width="260"/>

<img src="screenshots/Budget.png" width="260"/>

<img src="screenshots/Search.png" width="260"/>

<img src="screenshots/AIInsights.png" width="260"/>

</p>

---

## 🚀 Installation

Clone the repository

```bash
git clone https://github.com/DeveshSakre/SpendWise.git
```

Open the project in Android Studio.

Build and Run on an Android device.

---

## 📌 Permissions

SpendWise uses the following Android permissions:

- RECEIVE_SMS
- READ_SMS

These permissions are used only for automatic UPI transaction detection.

---

## ⚠ Disclaimer

This project is intended for educational and personal-use purposes.

Automatic SMS parsing depends on the SMS format provided by different banks and payment providers.

Google Play currently restricts SMS permissions for general-purpose finance apps, so this implementation is designed for learning and personal use.

---

## 🚀 Future Enhancements

- Better merchant recognition
- Subscription detection
- Savings goals
- Financial health score
- Material You improvements
- Cloud backup
- Multi-device sync
- Voice expense entry
- OCR receipt scanning
- Wear OS companion app

---

## 🤝 Contributing

Contributions, suggestions and feature requests are always welcome.

Feel free to fork the repository and submit a Pull Request.

---

## ⭐ Support

If you found this project useful, please consider giving it a ⭐ on GitHub.

It helps the project reach more developers.

---

## 👨‍💻 Author

**Devesh Sakre**

Android Developer | Kotlin | Jetpack Compose | AI Enthusiast

GitHub:
https://github.com/DeveshSakre

---

<p align="center">
Made with ❤️ using Kotlin, Jetpack Compose and Android.
</p>
