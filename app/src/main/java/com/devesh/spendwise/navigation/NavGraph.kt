package com.devesh.spendwise.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.devesh.spendwise.ui.add.AddExpenseScreen
import com.devesh.spendwise.ui.list.ExpenseListScreen

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
    }
}
