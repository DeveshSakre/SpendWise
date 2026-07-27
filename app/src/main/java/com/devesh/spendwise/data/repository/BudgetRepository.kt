package com.devesh.spendwise.data.repository

import com.devesh.spendwise.data.local.BudgetDao
import com.devesh.spendwise.data.local.BudgetEntity
import kotlinx.coroutines.flow.Flow

class BudgetRepository(
    private val budgetDao: BudgetDao
) {
    suspend fun saveBudget(budget: BudgetEntity) {
        budgetDao.insertBudget(budget)
    }

    suspend fun updateBudget(budget: BudgetEntity) {
        budgetDao.updateBudget(budget)
    }

    suspend fun getBudgetForMonth(month: Int, year: Int): BudgetEntity? {
        return budgetDao.getBudgetForMonth(month, year)
    }

    suspend fun deleteBudget(budget: BudgetEntity) {
        budgetDao.deleteBudget(budget)
    }

    fun observeCurrentBudget(month: Int, year: Int): Flow<BudgetEntity?> {
        return budgetDao.observeCurrentBudget(month, year)
    }

    fun observeLatestBudget(): Flow<BudgetEntity?> {
        return budgetDao.observeLatestBudget()
    }
}
