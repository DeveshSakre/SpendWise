package com.devesh.spendwise.util

import android.R.id.message
import android.content.Context
import com.devesh.spendwise.data.local.AppDatabase
import com.devesh.spendwise.data.local.ExpenseEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object UpiSmsParser {





    private val amountRegex =
        Regex("(rs\\.?|₹)\\s?(\\d+(\\.\\d{1,2})?)", RegexOption.IGNORE_CASE)

    private val receiverRegex =
        Regex("(to|paid to|cr\\. to)\\s([a-zA-Z0-9@._\\- ]+)", RegexOption.IGNORE_CASE)

    fun parseAndSave(context: Context, message: String) {
        val lower = message.lowercase()


        if (
            lower.contains("credit") ||
            lower.contains("cr.") ||
            lower.contains("received")
        ) return


        if (
            !(
                    lower.contains("upi") ||
                            lower.contains("sent") ||
                            lower.contains("paid") ||
                            lower.contains("dr.")
                    )
        ) return

        
        val amountMatch = amountRegex.find(message)
        val receiverMatch = receiverRegex.find(message)

        val amount = amountMatch?.groupValues?.get(2)?.toDoubleOrNull() ?: return
        val receiver = receiverMatch?.groupValues?.get(2)?.trim()
            ?: "UPI Payment"

        saveExpense(context, amount, receiver)
    }

    private fun saveExpense(
        context: Context,
        amount: Double,
        receiver: String
    ) {
        val db = AppDatabase.getDatabase(context)

        val expense = ExpenseEntity(
            amount = amount,
            category = guessCategory(receiver),
            paymentMode = "UPI",
            note = receiver,
            date = System.currentTimeMillis()
        )

        CoroutineScope(Dispatchers.IO).launch {
            db.expenseDao().insertExpense(expense)
        }
    }

    private fun guessCategory(receiver: String): String {
        val r = receiver.lowercase()

        return when {
            r.contains("zomato") || r.contains("swiggy") -> "Food"
            r.contains("uber") || r.contains("ola") -> "Transport"
            r.contains("amazon") || r.contains("flipkart") -> "Shopping"
            r.contains("petrol") || r.contains("fuel") -> "Fuel"
            else -> "Others"
        }
    }
}
