package com.devesh.spendwise.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devesh.spendwise.data.local.ExpenseEntity
import com.devesh.spendwise.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

enum class DateFilterOption {
    ALL, TODAY, THIS_WEEK, THIS_MONTH, CUSTOM
}

enum class SortOption {
    NEWEST_FIRST, OLDEST_FIRST, HIGHEST_AMOUNT, LOWEST_AMOUNT
}

data class SearchFilterState(
    val query: String = "",
    val dateFilter: DateFilterOption = DateFilterOption.ALL,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    val selectedCategories: Set<String> = emptySet(),
    val selectedPaymentModes: Set<String> = emptySet(),
    val sortOption: SortOption = SortOption.NEWEST_FIRST
)

class SearchViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _filterState = MutableStateFlow(SearchFilterState())
    val filterState: StateFlow<SearchFilterState> = _filterState.asStateFlow()

    val filteredExpenses: StateFlow<List<ExpenseEntity>> = combine(
        repository.getAllExpenses(),
        _filterState
    ) { expenses, filter ->
        applyFilters(expenses, filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onQueryChange(newQuery: String) {
        _filterState.update { it.copy(query = newQuery) }
    }

    fun onDateFilterSelect(option: DateFilterOption) {
        _filterState.update { it.copy(dateFilter = option) }
    }

    fun onCustomDateRangeSelect(startDate: Long?, endDate: Long?) {
        _filterState.update {
            it.copy(
                dateFilter = DateFilterOption.CUSTOM,
                customStartDate = startDate,
                customEndDate = endDate
            )
        }
    }

    fun toggleCategory(category: String) {
        _filterState.update { current ->
            val set = current.selectedCategories.toMutableSet()
            if (set.contains(category)) set.remove(category) else set.add(category)
            current.copy(selectedCategories = set)
        }
    }

    fun togglePaymentMode(mode: String) {
        _filterState.update { current ->
            val set = current.selectedPaymentModes.toMutableSet()
            if (set.contains(mode)) set.remove(mode) else set.add(mode)
            current.copy(selectedPaymentModes = set)
        }
    }

    fun onSortOptionSelect(option: SortOption) {
        _filterState.update { it.copy(sortOption = option) }
    }

    fun clearAllFilters() {
        _filterState.value = SearchFilterState()
    }

    private fun applyFilters(expenses: List<ExpenseEntity>, filter: SearchFilterState): List<ExpenseEntity> {
        var result = expenses

        // 1. Text Search (Merchant, Notes, Category, Payment Method)
        if (filter.query.isNotBlank()) {
            val q = filter.query.trim().lowercase(Locale.ROOT)
            result = result.filter { expense ->
                expense.note.lowercase(Locale.ROOT).contains(q) ||
                        expense.category.lowercase(Locale.ROOT).contains(q) ||
                        expense.paymentMode.lowercase(Locale.ROOT).contains(q) ||
                        (expense.reference?.lowercase(Locale.ROOT)?.contains(q) == true)
            }
        }

        // 2. Date Filter
        val now = LocalDate.now()
        result = when (filter.dateFilter) {
            DateFilterOption.ALL -> result
            DateFilterOption.TODAY -> result.filter {
                Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate() == now
            }
            DateFilterOption.THIS_WEEK -> {
                val sevenDaysAgo = now.minusDays(6)
                result.filter {
                    val d = Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
                    !d.isBefore(sevenDaysAgo) && !d.isAfter(now)
                }
            }
            DateFilterOption.THIS_MONTH -> result.filter {
                val d = Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
                d.monthValue == now.monthValue && d.year == now.year
            }
            DateFilterOption.CUSTOM -> {
                val start = filter.customStartDate
                val end = filter.customEndDate
                result.filter { expense ->
                    val afterStart = start == null || expense.date >= start
                    val beforeEnd = end == null || expense.date <= (end + 86400000L)
                    afterStart && beforeEnd
                }
            }
        }

        // 3. Category Filter
        if (filter.selectedCategories.isNotEmpty()) {
            result = result.filter { expense ->
                filter.selectedCategories.any { cat -> cat.equals(expense.category, ignoreCase = true) }
            }
        }

        // 4. Payment Mode Filter
        if (filter.selectedPaymentModes.isNotEmpty()) {
            result = result.filter { expense ->
                filter.selectedPaymentModes.any { mode -> mode.equals(expense.paymentMode, ignoreCase = true) }
            }
        }

        // 5. Sorting
        return when (filter.sortOption) {
            SortOption.NEWEST_FIRST -> result.sortedByDescending { it.date }
            SortOption.OLDEST_FIRST -> result.sortedBy { it.date }
            SortOption.HIGHEST_AMOUNT -> result.sortedByDescending { it.amount }
            SortOption.LOWEST_AMOUNT -> result.sortedBy { it.amount }
        }
    }
}
