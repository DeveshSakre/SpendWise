package com.devesh.spendwise.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.devesh.spendwise.data.local.ExpenseEntity
import com.devesh.spendwise.data.repository.ExpenseRepository
import kotlinx.coroutines.launch

class ExpenseListViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    val expenses = repository.getAllExpenses().asLiveData()

    fun deleteExpense(expense: ExpenseEntity, context: android.content.Context? = null) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            if (context != null && android.os.Build.VERSION_CODES.O <= android.os.Build.VERSION.SDK_INT) {
                com.devesh.spendwise.widget.WidgetUpdater.updateWidget(context.applicationContext)
            }
        }
    }

    // ✅ REQUIRED FOR UNDO
    fun addExpense(expense: ExpenseEntity, context: android.content.Context? = null) {
        viewModelScope.launch {
            repository.insertExpense(expense)
            if (context != null && android.os.Build.VERSION_CODES.O <= android.os.Build.VERSION.SDK_INT) {
                com.devesh.spendwise.widget.WidgetUpdater.updateWidget(context.applicationContext)
            }
        }
    }
}
