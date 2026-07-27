package com.devesh.spendwise.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year LIMIT 1")
    suspend fun getBudgetForMonth(month: Int, year: Int): BudgetEntity?

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year LIMIT 1")
    fun observeCurrentBudget(month: Int, year: Int): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets ORDER BY year DESC, month DESC LIMIT 1")
    fun observeLatestBudget(): Flow<BudgetEntity?>
}
