package com.devesh.spendwise.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devesh.spendwise.data.repository.DashboardRepository
import com.devesh.spendwise.data.repository.DashboardState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AIInsightsViewModel(
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    val dashboardState: StateFlow<DashboardState> = dashboardRepository.dashboardState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardState()
        )
}
