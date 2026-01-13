# SpendWise 

SpendWise is a personal expense tracking Android application built using **Kotlin** and **Jetpack Compose**.  
It allows users to track expenses manually and **automatically detect UPI debit transactions via SMS**.

>  This project is built for **personal learning and experimentation**.

---

##  Features

###  Manual Expense Tracking
- Add expenses with amount, category, payment mode, date, and notes
- Clean and modern UI inspired by real finance apps

###  Automatic UPI Expense Detection
- Reads bank SMS messages
- Detects **only debit / paid / Dr** UPI transactions
- Automatically adds expenses to the app
- Ignores credited / received money
- Extracts:
    - Amount
    - Receiver / merchant
    - Reference / UTR (to avoid duplicates)

### 🧠 Smart Category Guessing
- Zomato / Swiggy → Food
- Uber / Ola → Transport
- Amazon / Flipkart → Shopping
- Petrol / Fuel → Fuel
- Others → Default category

### 🧾 Expense List
- Shows total expense
- Swipe to delete expenses
- Clean grouped UI

---

## 🛠️ Tech Stack

- **Kotlin**
- **Jetpack Compose**
- **Room Database**
- **MVVM Architecture**
- **Coroutines**
- **Material 3**
- **BroadcastReceiver (SMS)**

---

## 📂 Project Structure
com.devesh.spendwise
│
├── data
│ ├── local
│ │ ├── ExpenseEntity.kt
│ │ ├── ExpenseDao.kt
│ │ └── AppDatabase.kt
│
├── sms
│ └── UpiSmsReceiver.kt
│
├── util
│ └── UpiSmsParser.kt
│
├── ui
│ ├── add
│ └── list
│
└── MainActivity.kt

---
## ⚠️ Disclaimer

- SMS parsing is bank-dependent and may vary across different banks
- This app is **not Play Store compliant** due to SMS permissions
- Intended strictly for **personal use and learning purposes**

---

## 🚀 Future Improvements

- Duplicate transaction protection (UTR-based)
- Manual edit after auto-added expense
- Better merchant name cleanup
- Analytics & charts
- Cloud sync
- Export to CSV

---

## 👨‍💻 Author

**Devesh Sakre**  
Android Developer | Kotlin | Jetpack Compose

## 📸 Screenshots

<p align="center">
  <img src="screenshots/expense_list.jpg" width="260" />
  <img src="screenshots/add_expense_screen.jpg" width="260" />
  <img src="screenshots/add_expense.jpg" width="260" />
</p>



## 🔐 Permissions Used
 ```xml
<uses-permission android:name="android.permission.READ_SMS"/>
<uses-permission android:name="android.permission.RECEIVE_SMS"/>


