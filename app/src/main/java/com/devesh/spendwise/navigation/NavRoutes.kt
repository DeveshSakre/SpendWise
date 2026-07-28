package com.devesh.spendwise.navigation

object NavRoutes {
    const val ADD_EXPENSE = "add_expense"
    const val EXPENSE_LIST = "expense_list"
    const val BUDGET = "budget"
    const val ANALYTICS = "analytics"
    const val EXPENSE_DETAILS = "expense_details/{expenseId}"
    const val EDIT_EXPENSE = "edit_expense/{expenseId}"
    const val SEARCH = "search"
    const val AI_INSIGHTS = "ai_insights"
    const val MONTHLY_REPORTS = "monthly_reports"
    const val AI_ASSISTANT = "ai_assistant"

    fun expenseDetailsRoute(expenseId: Int): String = "expense_details/$expenseId"
    fun editExpenseRoute(expenseId: Int): String = "edit_expense/$expenseId"
}
