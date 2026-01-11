package com.devesh.spendwise

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.rememberNavController
import com.devesh.spendwise.navigation.NavGraph
import com.devesh.spendwise.ui.theme.SpendWiseTheme

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


        setContent {
            SpendWiseTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }


}
