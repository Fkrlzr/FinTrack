package com.fintrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fintrack.ui.dashboard.DashboardScreen
import com.fintrack.ui.dashboard.DashboardViewModel
import com.fintrack.ui.theme.FinTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinTrackTheme {
                val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
                DashboardScreen(viewModel)
            }
        }
    }
}
