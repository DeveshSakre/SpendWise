package com.devesh.spendwise.data.repository

import com.devesh.spendwise.data.local.BudgetEntity
import com.devesh.spendwise.data.local.ExpenseEntity
import com.devesh.spendwise.ui.budget.BudgetColorState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DashboardAIInsights(
    val topCategory: String = "N/A",
    val topCategoryAmount: Double = 0.0,
    val lowestCategory: String = "N/A",
    val lowestCategoryAmount: Double = 0.0,
    val topMerchant: String = "N/A",
    val topMerchantAmount: Double = 0.0,
    val topMerchantCount: Int = 0,
    val dailyAvgSpending: Double = 0.0,
    val avgTransactionValue: Double = 0.0,
    val momGrowthPercentage: Double = 0.0,
    val isBudgetExceeded: Boolean = false,
    val budgetRemaining: Double = 0.0,
    val highestSpendingDay: String = "N/A",
    val highestSpendingDayAmount: Double = 0.0,
    val totalTransactions: Int = 0
)

data class DashboardState(
    val currentMonthExpenses: Double = 0.0,
    val monthlyBudget: Double = 0.0,
    val remainingBudget: Double = 0.0,
    val progressPercentage: Double = 0.0,
    val budgetColorState: BudgetColorState = BudgetColorState.GREEN,
    val recentExpenses: List<ExpenseEntity> = emptyList(),
    val allExpenses: List<ExpenseEntity> = emptyList(),
    val budgetEntity: BudgetEntity? = null,
    val aiInsights: DashboardAIInsights = DashboardAIInsights()
)

class DashboardRepository(
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository
) {

    val dashboardState: Flow<DashboardState> = combine(
        expenseRepository.getAllExpenses(),
        budgetRepository.observeLatestBudget()
    ) { expenses, budget ->
        computeDashboardState(expenses, budget)
    }

    private fun computeDashboardState(expenses: List<ExpenseEntity>, budget: BudgetEntity?): DashboardState {
        val now = LocalDate.now()
        val currentMonth = now.monthValue
        val currentYear = now.year

        val currentMonthExpensesList = expenses.filter { expense ->
            val date = Instant.ofEpochMilli(expense.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            date.monthValue == currentMonth && date.year == currentYear
        }

        val totalCurrentMonthSpent = currentMonthExpensesList.sumOf { it.amount }
        val monthlyBudget = budget?.monthlyBudget ?: 0.0
        val remainingBudget = monthlyBudget - totalCurrentMonthSpent
        val progressPercentage = if (monthlyBudget > 0.0) (totalCurrentMonthSpent / monthlyBudget) * 100.0 else 0.0

        val colorState = when {
            progressPercentage < 70.0 -> BudgetColorState.GREEN
            progressPercentage in 70.0..90.0 -> BudgetColorState.ORANGE
            else -> BudgetColorState.RED
        }

        // Previous Month comparison for MoM Growth
        val prevMonthDate = now.minusMonths(1)
        val prevMonthExpensesList = expenses.filter { expense ->
            val date = Instant.ofEpochMilli(expense.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            date.monthValue == prevMonthDate.monthValue && date.year == prevMonthDate.year
        }
        val prevMonthSpent = prevMonthExpensesList.sumOf { it.amount }
        val momGrowthPercentage = if (prevMonthSpent > 0.0) {
            ((totalCurrentMonthSpent - prevMonthSpent) / prevMonthSpent) * 100.0
        } else {
            0.0
        }

        // Categories
        val categoryGrouped = currentMonthExpensesList.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }

        val topCategory = categoryGrouped.firstOrNull()?.first ?: "N/A"
        val topCategoryAmount = categoryGrouped.firstOrNull()?.second ?: 0.0
        val lowestCategory = categoryGrouped.lastOrNull()?.first ?: "N/A"
        val lowestCategoryAmount = categoryGrouped.lastOrNull()?.second ?: 0.0

        // Merchants
        val merchantGrouped = currentMonthExpensesList.groupBy { it.note.ifBlank { it.category }.trim() }
        val topMerchantEntry = merchantGrouped.maxByOrNull { entry -> entry.value.sumOf { it.amount } }
        val topMerchant = topMerchantEntry?.key ?: "N/A"
        val topMerchantAmount = topMerchantEntry?.value?.sumOf { it.amount } ?: 0.0
        val topMerchantCount = topMerchantEntry?.value?.size ?: 0

        // Averages
        val dayOfMonth = now.dayOfMonth.coerceAtLeast(1)
        val dailyAvgSpending = if (totalCurrentMonthSpent > 0.0) totalCurrentMonthSpent / dayOfMonth else 0.0
        val avgTransactionValue = if (currentMonthExpensesList.isNotEmpty()) totalCurrentMonthSpent / currentMonthExpensesList.size else 0.0

        // Highest Spending Day
        val dayFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.US)
        val dayGrouped = currentMonthExpensesList.groupBy { expense ->
            Instant.ofEpochMilli(expense.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }.mapValues { entry -> entry.value.sumOf { it.amount } }

        val highestDayEntry = dayGrouped.maxByOrNull { it.value }
        val highestSpendingDay = highestDayEntry?.key?.format(dayFormatter) ?: "N/A"
        val highestSpendingDayAmount = highestDayEntry?.value ?: 0.0

        val insights = DashboardAIInsights(
            topCategory = topCategory,
            topCategoryAmount = topCategoryAmount,
            lowestCategory = lowestCategory,
            lowestCategoryAmount = lowestCategoryAmount,
            topMerchant = topMerchant,
            topMerchantAmount = topMerchantAmount,
            topMerchantCount = topMerchantCount,
            dailyAvgSpending = dailyAvgSpending,
            avgTransactionValue = avgTransactionValue,
            momGrowthPercentage = momGrowthPercentage,
            isBudgetExceeded = remainingBudget < 0.0,
            budgetRemaining = remainingBudget,
            highestSpendingDay = highestSpendingDay,
            highestSpendingDayAmount = highestSpendingDayAmount,
            totalTransactions = currentMonthExpensesList.size
        )

        return DashboardState(
            currentMonthExpenses = totalCurrentMonthSpent,
            monthlyBudget = monthlyBudget,
            remainingBudget = remainingBudget,
            progressPercentage = progressPercentage,
            budgetColorState = colorState,
            recentExpenses = expenses.take(10),
            allExpenses = expenses,
            budgetEntity = budget,
            aiInsights = insights
        )
    }
}
