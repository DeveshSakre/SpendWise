package com.devesh.spendwise.widget

import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

enum class WidgetStatusLevel {
    ON_TRACK,
    NEAR_LIMIT,
    EXCEEDED,
    NO_BUDGET
}

data class SpendWiseWidgetState(
    val monthName: String,
    val totalSpent: Double,
    val monthlyBudget: Double,
    val remainingBudget: Double,
    val utilizationPercentage: Double,
    val statusText: String,
    val statusLevel: WidgetStatusLevel
)

object WidgetKeys {
    val KEY_MONTH_NAME = stringPreferencesKey("widget_month_name")
    val KEY_TOTAL_SPENT = doublePreferencesKey("widget_total_spent")
    val KEY_MONTHLY_BUDGET = doublePreferencesKey("widget_monthly_budget")
    val KEY_REMAINING_BUDGET = doublePreferencesKey("widget_remaining_budget")
    val KEY_BUDGET_UTILIZATION = doublePreferencesKey("widget_budget_utilization")
    val KEY_STATUS_TEXT = stringPreferencesKey("widget_status_text")
    val KEY_STATUS_LEVEL = stringPreferencesKey("widget_status_level")
}
