package com.devesh.spendwise.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devesh.spendwise.data.local.ExpenseEntity
import com.devesh.spendwise.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditExpenseViewModel(
    private val repository: ExpenseRepository,
    val expenseId: Int
) : ViewModel() {

    private val _expense = MutableStateFlow<ExpenseEntity?>(null)
    val expense: StateFlow<ExpenseEntity?> = _expense.asStateFlow()

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    private val _category = MutableStateFlow("Food")
    val category: StateFlow<String> = _category.asStateFlow()

    private val _paymentMode = MutableStateFlow("UPI")
    val paymentMode: StateFlow<String> = _paymentMode.asStateFlow()

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    private val _date = MutableStateFlow(System.currentTimeMillis())
    val date: StateFlow<Long> = _date.asStateFlow()

    private val _reference = MutableStateFlow<String?>(null)
    val reference: StateFlow<String?> = _reference.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getExpenseById(expenseId).collect { item ->
                if (item != null && _expense.value == null) {
                    _expense.value = item
                    _amount.value = if (item.amount % 1.0 == 0.0) item.amount.toInt().toString() else item.amount.toString()
                    _category.value = item.category
                    _paymentMode.value = item.paymentMode
                    _note.value = item.note
                    _date.value = item.date
                    _reference.value = item.reference
                }
            }
        }
    }

    fun onAmountChange(newAmount: String) {
        _amount.value = newAmount
        _errorMessage.value = null
    }

    fun onCategoryChange(newCategory: String) {
        _category.value = newCategory
    }

    fun onPaymentModeChange(newMode: String) {
        _paymentMode.value = newMode
    }

    fun onNoteChange(newNote: String) {
        _note.value = newNote
    }

    fun onDateChange(newDate: Long) {
        _date.value = newDate
    }

    fun saveExpense(onSuccess: () -> Unit) {
        val parsedAmount = _amount.value.toDoubleOrNull()
        if (parsedAmount == null || parsedAmount <= 0.0) {
            _errorMessage.value = "Please enter a valid amount greater than 0"
            return
        }

        val currentExpense = _expense.value ?: return

        val updated = currentExpense.copy(
            amount = parsedAmount,
            category = _category.value,
            paymentMode = _paymentMode.value,
            note = _note.value,
            date = _date.value,
            reference = _reference.value
        )

        viewModelScope.launch {
            repository.updateExpense(updated)
            onSuccess()
        }
    }

    fun deleteExpense(onDeleted: () -> Unit) {
        val currentExpense = _expense.value ?: return
        viewModelScope.launch {
            repository.deleteExpense(currentExpense)
            onDeleted()
        }
    }
}
