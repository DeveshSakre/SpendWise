package com.devesh.spendwise.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devesh.spendwise.data.local.BudgetEntity
import com.devesh.spendwise.data.local.ExpenseEntity
import com.devesh.spendwise.data.repository.BudgetRepository
import com.devesh.spendwise.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class CategoryAnalyticsItem(
    val categoryName: String,
    val totalAmount: Double,
    val percentage: Double
)

data class DailyTrendItem(
    val dayName: String,
    val amount: Double
)

data class AnalyticsState(
    val totalMonthlySpending: Double = 0.0,
    val dailySpending: Double = 0.0,
    val weeklySpending: Double = 0.0,
    val monthlyBudget: Double = 0.0,
    val remainingBudget: Double = 0.0,
    val budgetUtilisationPercentage: Double = 0.0,
    val averageDailySpending: Double = 0.0,
    val topSpendingCategory: String = "N/A",
    val topSpendingCategoryAmount: Double = 0.0,
    val topMerchant: String = "N/A",
    val topMerchantAmount: Double = 0.0,
    val categoryBreakdowns: List<CategoryAnalyticsItem> = emptyList(),
    val dailySpendingTrends: List<DailyTrendItem> = emptyList()
)

class AnalyticsViewModel(
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    val analyticsState: StateFlow<AnalyticsState> = combine(
        expenseRepository.getAllExpenses(),
        budgetRepository.observeLatestBudget()
    ) { expenses, budget ->
        computeAnalytics(expenses, budget)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsState()
    )

    private fun computeAnalytics(expenses: List<ExpenseEntity>, budget: BudgetEntity?): AnalyticsState {
        val now = LocalDate.now()
        val currentMonth = now.monthValue
        val currentYear = now.year

        // Filter current month expenses
        val currentMonthExpenses = expenses.filter { expense ->
            val date = Instant.ofEpochMilli(expense.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            date.monthValue == currentMonth && date.year == currentYear
        }

        val totalMonthlySpending = currentMonthExpenses.sumOf { it.amount }

        // Daily spending (today)
        val dailySpending = expenses.filter { expense ->
            val date = Instant.ofEpochMilli(expense.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            date == now
        }.sumOf { it.amount }

        // Weekly spending (past 7 days including today)
        val sevenDaysAgo = now.minusDays(6)
        val weeklySpending = expenses.filter { expense ->
            val date = Instant.ofEpochMilli(expense.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            !date.isBefore(sevenDaysAgo) && !date.isAfter(now)
        }.sumOf { it.amount }

        // Average daily spending (for current month)
        val dayOfMonth = now.dayOfMonth.coerceAtLeast(1)
        val averageDailySpending = if (totalMonthlySpending > 0) totalMonthlySpending / dayOfMonth else 0.0

        // Budget calculations
        val monthlyBudget = budget?.monthlyBudget ?: 0.0
        val remainingBudget = monthlyBudget - totalMonthlySpending
        val budgetUtilisationPercentage = if (monthlyBudget > 0) (totalMonthlySpending / monthlyBudget) * 100.0 else 0.0

        // Category breakdown & percentages
        val categoryGrouped = currentMonthExpenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val categoryBreakdowns = categoryGrouped.map { (cat, amount) ->
            val percentage = if (totalMonthlySpending > 0) (amount / totalMonthlySpending) * 100.0 else 0.0
            CategoryAnalyticsItem(
                categoryName = cat,
                totalAmount = amount,
                percentage = percentage
            )
        }.sortedByDescending { it.totalAmount }

        // Top spending category
        val topCategoryItem = categoryBreakdowns.firstOrNull()
        val topSpendingCategory = topCategoryItem?.categoryName ?: "N/A"
        val topSpendingCategoryAmount = topCategoryItem?.totalAmount ?: 0.0

        // Top merchant
        val merchantGrouped = currentMonthExpenses.groupBy { it.note.ifBlank { it.category }.trim() }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
        val topMerchantEntry = merchantGrouped.maxByOrNull { it.value }
        val topMerchant = topMerchantEntry?.key ?: "N/A"
        val topMerchantAmount = topMerchantEntry?.value ?: 0.0


        // 7-day spending trend history
        val dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
        val dailySpendingTrends = (0..6).map { offset ->
            val targetDate = now.minusDays((6 - offset).toLong())
            val dayName = targetDate.format(dayFormatter)
            val dayTotal = expenses.filter { expense ->
                val date = Instant.ofEpochMilli(expense.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                date == targetDate
            }.sumOf { it.amount }

            DailyTrendItem(dayName = dayName, amount = dayTotal)
        }

        return AnalyticsState(
            totalMonthlySpending = totalMonthlySpending,
            dailySpending = dailySpending,
            weeklySpending = weeklySpending,
            monthlyBudget = monthlyBudget,
            remainingBudget = remainingBudget,
            budgetUtilisationPercentage = budgetUtilisationPercentage,
            averageDailySpending = averageDailySpending,
            topSpendingCategory = topSpendingCategory,
            topSpendingCategoryAmount = topSpendingCategoryAmount,
            topMerchant = topMerchant,
            topMerchantAmount = topMerchantAmount,
            categoryBreakdowns = categoryBreakdowns,
            dailySpendingTrends = dailySpendingTrends
        )
    }
}
