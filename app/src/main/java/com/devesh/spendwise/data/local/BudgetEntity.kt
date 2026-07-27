package com.devesh.spendwise.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val month: Int,
    val year: Int,
    val monthlyBudget: Double,
    val foodBudget: Double = 0.0,
    val shoppingBudget: Double = 0.0,
    val transportBudget: Double = 0.0,
    val fuelBudget: Double = 0.0,
    val othersBudget: Double = 0.0
)
