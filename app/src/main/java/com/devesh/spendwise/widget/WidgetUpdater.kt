package com.devesh.spendwise.widget

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import com.devesh.spendwise.data.local.AppDatabase
import com.devesh.spendwise.data.repository.BudgetRepository
import com.devesh.spendwise.data.repository.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object WidgetUpdater {

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateWidget(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(context)
                val expenseRepo = ExpenseRepository(db.expenseDao())
                val budgetRepo = BudgetRepository(db.budgetDao())

                val now = LocalDate.now()
                val monthName = now.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US))

                val allExpenses = expenseRepo.getAllExpenses().first()
                val latestBudget = budgetRepo.observeLatestBudget().first()

                val currentMonthExpenses = allExpenses.filter { expense ->
                    val d = Instant.ofEpochMilli(expense.date).atZone(ZoneId.systemDefault()).toLocalDate()
                    d.monthValue == now.monthValue && d.year == now.year
                }

                val totalSpent = currentMonthExpenses.sumOf { it.amount }
                val monthlyBudget = latestBudget?.monthlyBudget ?: 0.0
                val remainingBudget = monthlyBudget - totalSpent
                val utilization = if (monthlyBudget > 0.0) (totalSpent / monthlyBudget) * 100.0 else 0.0

                val statusLevel = when {
                    monthlyBudget <= 0.0 -> WidgetStatusLevel.NO_BUDGET
                    remainingBudget < 0.0 -> WidgetStatusLevel.EXCEEDED
                    utilization >= 80.0 -> WidgetStatusLevel.NEAR_LIMIT
                    else -> WidgetStatusLevel.ON_TRACK
                }

                val statusText = when (statusLevel) {
                    WidgetStatusLevel.NO_BUDGET -> "No Budget Set"
                    WidgetStatusLevel.EXCEEDED -> "Over Budget"
                    WidgetStatusLevel.NEAR_LIMIT -> "Near Limit"
                    WidgetStatusLevel.ON_TRACK -> "On Track"
                }

                val manager = GlanceAppWidgetManager(context)
                val glanceIds = manager.getGlanceIds(SpendWiseWidget::class.java)

                glanceIds.forEach { glanceId ->
                    updateAppWidgetState(context, glanceId) { prefs ->
                        prefs[WidgetKeys.KEY_MONTH_NAME] = monthName
                        prefs[WidgetKeys.KEY_TOTAL_SPENT] = totalSpent
                        prefs[WidgetKeys.KEY_MONTHLY_BUDGET] = monthlyBudget
                        prefs[WidgetKeys.KEY_REMAINING_BUDGET] = remainingBudget
                        prefs[WidgetKeys.KEY_BUDGET_UTILIZATION] = utilization
                        prefs[WidgetKeys.KEY_STATUS_TEXT] = statusText
                        prefs[WidgetKeys.KEY_STATUS_LEVEL] = statusLevel.name
                    }
                }

                SpendWiseWidget().updateAll(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
