package com.devesh.spendwise.ui.search

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.devesh.spendwise.data.local.AppDatabase
import com.devesh.spendwise.data.repository.ExpenseRepository
import com.devesh.spendwise.navigation.NavRoutes
import com.devesh.spendwise.ui.list.ExpenseItemCard
import com.devesh.spendwise.ui.theme.StitchColors

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val repository = remember { ExpenseRepository(db.expenseDao()) }
    val viewModel = remember { SearchViewModel(repository) }

    val filterState by viewModel.filterState.collectAsState()
    val filteredExpenses by viewModel.filteredExpenses.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = StitchColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Search & Filter",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.OnSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = StitchColors.OnSurface
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.clearAllFilters() }) {
                        Text(
                            text = "Clear All",
                            color = StitchColors.Primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StitchColors.Background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = filterState.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = {
                        Text(
                            text = "Search merchant, note, or category...",
                            fontSize = 14.sp,
                            color = StitchColors.OnSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = StitchColors.Primary
                        )
                    },
                    trailingIcon = {
                        if (filterState.query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = StitchColors.OnSurfaceVariant
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = StitchColors.SurfaceContainerLowest,
                        unfocusedContainerColor = StitchColors.SurfaceContainerLowest,
                        focusedBorderColor = StitchColors.Primary,
                        unfocusedBorderColor = StitchColors.SurfaceContainerHigh
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Filter Chips Carousel
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Date Range Filters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChipItem(
                        text = "All Time",
                        isSelected = filterState.dateFilter == DateFilterOption.ALL,
                        onClick = { viewModel.onDateFilterSelect(DateFilterOption.ALL) }
                    )
                    FilterChipItem(
                        text = "Today",
                        isSelected = filterState.dateFilter == DateFilterOption.TODAY,
                        onClick = { viewModel.onDateFilterSelect(DateFilterOption.TODAY) }
                    )
                    FilterChipItem(
                        text = "This Week",
                        isSelected = filterState.dateFilter == DateFilterOption.THIS_WEEK,
                        onClick = { viewModel.onDateFilterSelect(DateFilterOption.THIS_WEEK) }
                    )
                    FilterChipItem(
                        text = "This Month",
                        isSelected = filterState.dateFilter == DateFilterOption.THIS_MONTH,
                        onClick = { viewModel.onDateFilterSelect(DateFilterOption.THIS_MONTH) }
                    )
                }

                // Category & Payment Filters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sort dropdown button
                    Box {
                        AssistChip(
                            onClick = { showSortMenu = true },
                            label = {
                                Text(
                                    text = when (filterState.sortOption) {
                                        SortOption.NEWEST_FIRST -> "Newest"
                                        SortOption.OLDEST_FIRST -> "Oldest"
                                        SortOption.HIGHEST_AMOUNT -> "Highest ₹"
                                        SortOption.LOWEST_AMOUNT -> "Lowest ₹"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = "Sort",
                                    tint = StitchColors.Primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = StitchColors.PrimaryContainer.copy(alpha = 0.15f),
                                labelColor = StitchColors.Primary
                            ),
                            border = null
                        )

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Newest First") },
                                onClick = {
                                    viewModel.onSortOptionSelect(SortOption.NEWEST_FIRST)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Oldest First") },
                                onClick = {
                                    viewModel.onSortOptionSelect(SortOption.OLDEST_FIRST)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Highest Amount") },
                                onClick = {
                                    viewModel.onSortOptionSelect(SortOption.HIGHEST_AMOUNT)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Lowest Amount") },
                                onClick = {
                                    viewModel.onSortOptionSelect(SortOption.LOWEST_AMOUNT)
                                    showSortMenu = false
                                }
                            )
                        }
                    }

                    // Category Filter Pills
                    val categories = listOf("Food", "Shopping", "Fuel", "Transport", "Bills", "Others")
                    categories.forEach { cat ->
                        val isSelected = filterState.selectedCategories.contains(cat)
                        FilterChipItem(
                            text = cat,
                            isSelected = isSelected,
                            onClick = { viewModel.toggleCategory(cat) }
                        )
                    }

                    // Payment Method Filter Pills
                    val paymentModes = listOf("UPI", "Cash", "Card")
                    paymentModes.forEach { mode ->
                        val isSelected = filterState.selectedPaymentModes.contains(mode)
                        FilterChipItem(
                            text = mode,
                            isSelected = isSelected,
                            onClick = { viewModel.togglePaymentMode(mode) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Results count header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Results (${filteredExpenses.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchColors.OnSurface
                )
            }

            Spacer(Modifier.height(8.dp))

            // Expense List Results
            if (filteredExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = StitchColors.OnSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "No expenses found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchColors.OnSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your search or filters",
                            fontSize = 13.sp,
                            color = StitchColors.Outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredExpenses, key = { it.id }) { expense ->
                        ExpenseItemCard(
                            expense = expense,
                            onClick = { navController.navigate(NavRoutes.expenseDetailsRoute(expense.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        },
        shape = RoundedCornerShape(16.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = StitchColors.Primary,
            selectedLabelColor = Color.White,
            containerColor = StitchColors.SurfaceContainer,
            labelColor = StitchColors.OnSurfaceVariant
        ),
        border = null
    )
}
