package com.devesh.spendwise.ui.add

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devesh.spendwise.data.local.ExpenseEntity
import com.devesh.spendwise.data.repository.ExpenseRepository
import kotlinx.coroutines.launch

class AddExpenseViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    var amount = mutableStateOf("")
        private set

    var category = mutableStateOf("Food")
        private set

    var paymentMode = mutableStateOf("UPI")
        private set

    var note = mutableStateOf("")
        private set

    fun onAmountChange(value: String) {
        amount.value = value
    }

    fun onCategoryChange(value: String) {
        category.value = value
    }

    fun onNoteChange(value: String) {
        note.value = value
    }

    fun saveExpense() {
        if (amount.value.isBlank()) return

        val expense = ExpenseEntity(
            amount = amount.value.toDouble(),
            category = category.value,
            note = note.value,
            paymentMode = paymentMode.value,
            date = System.currentTimeMillis()
        )


        viewModelScope.launch {
            repository.insertExpense(expense)
        }
    }
}
