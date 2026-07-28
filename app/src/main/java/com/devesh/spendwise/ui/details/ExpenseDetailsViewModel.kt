package com.devesh.spendwise.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devesh.spendwise.data.local.ExpenseEntity
import com.devesh.spendwise.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseDetailsViewModel(
    private val repository: ExpenseRepository,
    val expenseId: Int
) : ViewModel() {

    val expense: StateFlow<ExpenseEntity?> = repository.getExpenseById(expenseId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun deleteExpense(onDeleted: () -> Unit) {
        val currentExpense = expense.value ?: return
        viewModelScope.launch {
            repository.deleteExpense(currentExpense)
            onDeleted()
        }
    }
}
