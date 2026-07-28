package com.devesh.spendwise

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.rememberNavController
import com.devesh.spendwise.navigation.NavGraph
import com.devesh.spendwise.ui.theme.SpendWiseTheme

import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.lifecycleScope
import com.devesh.spendwise.widget.WidgetUpdater
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS
            ),
            100
        )

        lifecycleScope.launch {
            if (android.os.Build.VERSION_CODES.O <= android.os.Build.VERSION.SDK_INT) {
                WidgetUpdater.updateWidget(applicationContext)
            }
        }

        val targetRoute = intent?.getStringExtra("route")

        setContent {
            SpendWiseTheme {
                val navController = rememberNavController()

                LaunchedEffect(targetRoute) {
                    if (!targetRoute.isNullOrBlank() && targetRoute != com.devesh.spendwise.navigation.NavRoutes.EXPENSE_LIST) {
                        navController.navigate(targetRoute)
                    }
                }

                NavGraph(navController = navController)
            }
        }
    }
}
