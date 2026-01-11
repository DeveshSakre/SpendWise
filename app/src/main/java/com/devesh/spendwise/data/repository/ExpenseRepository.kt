package com.devesh.spendwise.data.repository

import com.devesh.spendwise.data.local.ExpenseDao
import com.devesh.spendwise.data.local.ExpenseEntity

class ExpenseRepository(
    private val dao: ExpenseDao
) {

    fun getAllExpenses() = dao.getAllExpenses()

    suspend fun insertExpense(expense: ExpenseEntity) {
        dao.insertExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) { // ADD
        dao.deleteExpense(expense)
    }
}
