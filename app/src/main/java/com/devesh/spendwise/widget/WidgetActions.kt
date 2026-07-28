package com.devesh.spendwise.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.appwidget.action.ActionCallback
import com.devesh.spendwise.MainActivity
import com.devesh.spendwise.navigation.NavRoutes

object WidgetActions {

    fun createHomeIntent(context: Context): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("route", NavRoutes.EXPENSE_LIST)
        }
    }

    fun createAddExpenseIntent(context: Context): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("route", NavRoutes.ADD_EXPENSE)
        }
    }

    fun createAnalyticsIntent(context: Context): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("route", NavRoutes.ANALYTICS)
        }
    }
}

class RefreshWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: androidx.glance.action.ActionParameters
    ) {
        WidgetUpdater.updateWidget(context)
    }
}
