package com.devesh.spendwise.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devesh.spendwise.data.local.BudgetEntity
import com.devesh.spendwise.data.local.ExpenseEntity
import com.devesh.spendwise.data.repository.BudgetRepository
import com.devesh.spendwise.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

enum class BudgetColorState {
    GREEN,
    ORANGE,
    RED
}

data class BudgetStatus(
    val monthlyBudget: Double = 0.0,
    val currentMonthExpenses: Double = 0.0,
    val remainingBudget: Double = 0.0,
    val progressPercentage: Double = 0.0,
    val colorState: BudgetColorState = BudgetColorState.GREEN
)

data class CategoryBudgetBreakdown(
    val categoryName: String,
    val spent: Double,
    val budgetLimit: Double,
    val percentage: Double,
    val colorState: BudgetColorState
)

class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _currentMonthYear = MutableStateFlow(getCurrentMonthYearPair())
    val currentMonthYear: StateFlow<Pair<Int, Int>> = _currentMonthYear

    val currentBudgetEntity: StateFlow<BudgetEntity?> = combine(
        _currentMonthYear
    ) { monthYear ->
        monthYear
    }.combine(budgetRepository.observeLatestBudget()) { _, latestBudget ->
        latestBudget
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val budgetStatus: StateFlow<BudgetStatus> = combine(
        currentBudgetEntity,
        expenseRepository.getAllExpenses()
    ) { budget, expenses ->
        computeBudgetStatus(budget, expenses, _currentMonthYear.value.first, _currentMonthYear.value.second)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BudgetStatus()
    )

    val categoryBreakdowns: StateFlow<List<CategoryBudgetBreakdown>> = combine(
        currentBudgetEntity,
        expenseRepository.getAllExpenses()
    ) { budget, expenses ->
        computeCategoryBreakdowns(budget, expenses, _currentMonthYear.value.first, _currentMonthYear.value.second)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun saveBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            budgetRepository.saveBudget(budget)
        }
    }

    fun updateBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            budgetRepository.updateBudget(budget)
        }
    }

    fun loadCurrentBudget(month: Int, year: Int) {
        _currentMonthYear.value = Pair(month, year)
    }

    fun calculateRemainingBudget(monthlyBudget: Double, expenses: Double): Double {
        return monthlyBudget - expenses
    }

    fun calculateBudgetPercentage(monthlyBudget: Double, expenses: Double): Double {
        return if (monthlyBudget > 0.0) {
            (expenses / monthlyBudget) * 100.0
        } else {
            0.0
        }
    }

    fun getBudgetColorState(percentage: Double): BudgetColorState {
        return when {
            percentage < 70.0 -> BudgetColorState.GREEN
            percentage in 70.0..90.0 -> BudgetColorState.ORANGE
            else -> BudgetColorState.RED
        }
    }

    fun validateBudgetInput(
        monthlyTotal: Double,
        food: Double,
        shopping: Double,
        transport: Double,
        fuel: Double,
        others: Double
    ): String? {
        if (monthlyTotal < 0.0 || food < 0.0 || shopping < 0.0 || transport < 0.0 || fuel < 0.0 || others < 0.0) {
            return "Budget values cannot be negative."
        }
        if (food > monthlyTotal || shopping > monthlyTotal || transport > monthlyTotal || fuel > monthlyTotal || others > monthlyTotal) {
            return "Category budget cannot exceed the total monthly budget."
        }
        val totalCategoryAllocated = food + shopping + transport + fuel + others
        if (totalCategoryAllocated > monthlyTotal) {
            return "Total category allocations (₹ ${totalCategoryAllocated.toInt()}) exceed monthly budget (₹ ${monthlyTotal.toInt()})."
        }
        return null
    }

    private fun computeBudgetStatus(
        budget: BudgetEntity?,
        expenses: List<ExpenseEntity>,
        month: Int,
        year: Int
    ): BudgetStatus {
        val monthlyBudget = budget?.monthlyBudget ?: 0.0

        val currentMonthExpenses = expenses.filter { expense ->
            val date = Instant.ofEpochMilli(expense.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            date.monthValue == month && date.year == year
        }.sumOf { it.amount }

        val remaining = calculateRemainingBudget(monthlyBudget, currentMonthExpenses)
        val percentage = calculateBudgetPercentage(monthlyBudget, currentMonthExpenses)
        val colorState = getBudgetColorState(percentage)

        return BudgetStatus(
            monthlyBudget = monthlyBudget,
            currentMonthExpenses = currentMonthExpenses,
            remainingBudget = remaining,
            progressPercentage = percentage,
            colorState = colorState
        )
    }

    private fun computeCategoryBreakdowns(
        budget: BudgetEntity?,
        expenses: List<ExpenseEntity>,
        month: Int,
        year: Int
    ): List<CategoryBudgetBreakdown> {
        val monthExpenses = expenses.filter { expense ->
            val date = Instant.ofEpochMilli(expense.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            date.monthValue == month && date.year == year
        }

        val categorySpentMap = monthExpenses.groupBy { it.category.lowercase(Locale.ROOT) }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val categories = listOf(
            Triple("Food", budget?.foodBudget ?: 0.0, categorySpentMap["food"] ?: 0.0),
            Triple("Shopping", budget?.shoppingBudget ?: 0.0, categorySpentMap["shopping"] ?: 0.0),
            Triple("Transport", budget?.transportBudget ?: 0.0, categorySpentMap["transport"] ?: 0.0),
            Triple("Fuel", budget?.fuelBudget ?: 0.0, categorySpentMap["fuel"] ?: 0.0),
            Triple("Others", budget?.othersBudget ?: 0.0, categorySpentMap["others"] ?: 0.0)
        )

        return categories.map { (name, limit, spent) ->
            val percentage = calculateBudgetPercentage(limit, spent)
            val colorState = getBudgetColorState(percentage)
            CategoryBudgetBreakdown(
                categoryName = name,
                spent = spent,
                budgetLimit = limit,
                percentage = percentage,
                colorState = colorState
            )
        }
    }

    private fun getCurrentMonthYearPair(): Pair<Int, Int> {
        val now = LocalDate.now()
        return Pair(now.monthValue, now.year)
    }
}
