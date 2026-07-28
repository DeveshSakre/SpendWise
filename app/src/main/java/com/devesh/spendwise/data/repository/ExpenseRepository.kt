package com.devesh.spendwise.data.repository

import com.devesh.spendwise.data.local.ExpenseDao
import com.devesh.spendwise.data.local.ExpenseEntity

class ExpenseRepository(
    private val dao: ExpenseDao
) {

    fun getAllExpenses() = dao.getAllExpenses()

    fun getExpenseById(id: Int) = dao.getExpenseById(id)

    suspend fun insertExpense(expense: ExpenseEntity) {
        dao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        dao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        dao.deleteExpense(expense)
    }
}
