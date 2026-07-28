package com.devesh.spendwise.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.devesh.spendwise.ui.add.AddExpenseScreen
import com.devesh.spendwise.ui.analytics.AnalyticsScreen
import com.devesh.spendwise.ui.budget.BudgetScreen
import com.devesh.spendwise.ui.assistant.AssistantScreen
import com.devesh.spendwise.ui.details.ExpenseDetailsScreen
import com.devesh.spendwise.ui.edit.EditExpenseScreen
import com.devesh.spendwise.ui.insights.AIInsightsScreen
import com.devesh.spendwise.ui.list.ExpenseListScreen
import com.devesh.spendwise.ui.report.MonthlyReportScreen
import com.devesh.spendwise.ui.search.SearchScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = NavRoutes.EXPENSE_LIST
    ) {

        composable(NavRoutes.EXPENSE_LIST) {
            ExpenseListScreen(navController)
        }

        composable(NavRoutes.ADD_EXPENSE) {
            AddExpenseScreen(navController)
        }

        composable(NavRoutes.BUDGET) {
            BudgetScreen(navController)
        }

        composable(NavRoutes.ANALYTICS) {
            AnalyticsScreen(navController)
        }

        composable(
            route = NavRoutes.EXPENSE_DETAILS,
            arguments = listOf(navArgument("expenseId") { type = NavType.IntType })
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getInt("expenseId") ?: 0
            ExpenseDetailsScreen(navController = navController, expenseId = expenseId)
        }

        composable(
            route = NavRoutes.EDIT_EXPENSE,
            arguments = listOf(navArgument("expenseId") { type = NavType.IntType })
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getInt("expenseId") ?: 0
            EditExpenseScreen(navController = navController, expenseId = expenseId)
        }

        composable(NavRoutes.SEARCH) {
            SearchScreen(navController)
        }

        composable(NavRoutes.AI_INSIGHTS) {
            AIInsightsScreen(navController)
        }

        composable(NavRoutes.MONTHLY_REPORTS) {
            MonthlyReportScreen(navController)
        }

        composable(NavRoutes.AI_ASSISTANT) {
            AssistantScreen(navController)
        }
    }
}
