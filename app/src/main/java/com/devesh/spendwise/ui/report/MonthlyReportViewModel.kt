package com.devesh.spendwise.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devesh.spendwise.data.local.ExpenseEntity
import com.devesh.spendwise.data.repository.BudgetRepository
import com.devesh.spendwise.data.repository.ExpenseRepository
import com.devesh.spendwise.util.MonthlyReportData
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class MonthYearSelection(
    val month: Int,
    val year: Int,
    val displayName: String
)

class MonthlyReportViewModel(
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val now = LocalDate.now()
    private val _selectedMonthYear = MutableStateFlow(Pair(now.monthValue, now.year))
    val selectedMonthYear: StateFlow<Pair<Int, Int>> = _selectedMonthYear.asStateFlow()

    val availableMonths: List<MonthYearSelection> = (0..11).map { offset ->
        val date = now.minusMonths(offset.toLong())
        val name = date.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US))
        MonthYearSelection(date.monthValue, date.year, name)
    }

    val reportData: StateFlow<MonthlyReportData> = combine(
        expenseRepository.getAllExpenses(),
        budgetRepository.observeLatestBudget(),
        _selectedMonthYear
    ) { expenses, budget, (month, year) ->
        computeMonthlyReport(expenses, budget, month, year)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = computeMonthlyReport(emptyList(), null, now.monthValue, now.year)
    )

    fun selectMonthYear(month: Int, year: Int) {
        _selectedMonthYear.value = Pair(month, year)
    }

    private fun computeMonthlyReport(
        allExpenses: List<ExpenseEntity>,
        budgetEntity: com.devesh.spendwise.data.local.BudgetEntity?,
        month: Int,
        year: Int
    ): MonthlyReportData {
        val targetMonthExpenses = allExpenses.filter { expense ->
            val d = Instant.ofEpochMilli(expense.date).atZone(ZoneId.systemDefault()).toLocalDate()
            d.monthValue == month && d.year == year
        }

        val totalSpending = targetMonthExpenses.sumOf { it.amount }
        val monthlyBudget = budgetEntity?.monthlyBudget ?: 0.0
        val remainingBudget = monthlyBudget - totalSpending
        val savings = (monthlyBudget - totalSpending).coerceAtLeast(0.0)
        val budgetPercentage = if (monthlyBudget > 0.0) (totalSpending / monthlyBudget) * 100.0 else 0.0

        val categoryGrouped = targetMonthExpenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }

        val topCategory = categoryGrouped.firstOrNull()?.first ?: "N/A"
        val topCategoryAmount = categoryGrouped.firstOrNull()?.second ?: 0.0

        val merchantGrouped = targetMonthExpenses.groupBy { it.note.ifBlank { it.category }.trim() }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
        val topMerchantEntry = merchantGrouped.maxByOrNull { it.value }
        val topMerchant = topMerchantEntry?.key ?: "N/A"
        val topMerchantAmount = topMerchantEntry?.value ?: 0.0

        val highestExpense = targetMonthExpenses.maxByOrNull { it.amount }

        val daysInMonth = if (month == now.monthValue && year == now.year) now.dayOfMonth else 30
        val dailyAvgSpending = if (daysInMonth > 0 && totalSpending > 0) totalSpending / daysInMonth else 0.0
        val avgTransactionValue = if (targetMonthExpenses.isNotEmpty()) totalSpending / targetMonthExpenses.size else 0.0

        val dayFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.US)
        val highestDayEntry = targetMonthExpenses.groupBy { expense ->
            Instant.ofEpochMilli(expense.date).atZone(ZoneId.systemDefault()).toLocalDate()
        }.mapValues { entry -> entry.value.sumOf { it.amount } }.maxByOrNull { it.value }

        val highestSpendingDay = highestDayEntry?.key?.format(dayFormatter) ?: "N/A"
        val monthName = LocalDate.of(year, month, 1).format(DateTimeFormatter.ofPattern("MMMM", Locale.US))

        return MonthlyReportData(
            monthName = monthName,
            year = year,
            totalSpending = totalSpending,
            monthlyBudget = monthlyBudget,
            remainingBudget = remainingBudget,
            savings = savings,
            budgetPercentage = budgetPercentage,
            topCategory = topCategory,
            topCategoryAmount = topCategoryAmount,
            topMerchant = topMerchant,
            topMerchantAmount = topMerchantAmount,
            highestExpense = highestExpense,
            dailyAvgSpending = dailyAvgSpending,
            avgTransactionValue = avgTransactionValue,
            highestSpendingDay = highestSpendingDay,
            totalTransactions = targetMonthExpenses.size,
            categoryBreakdown = categoryGrouped,
            expenses = targetMonthExpenses
        )
    }
}
