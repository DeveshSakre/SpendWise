package com.devesh.spendwise.ui.list

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.clickable
import com.devesh.spendwise.data.local.AppDatabase
import com.devesh.spendwise.data.local.ExpenseEntity
import com.devesh.spendwise.data.repository.BudgetRepository
import com.devesh.spendwise.data.repository.ExpenseRepository
import com.devesh.spendwise.navigation.NavRoutes
import com.devesh.spendwise.ui.budget.BudgetColorState
import com.devesh.spendwise.ui.budget.BudgetStatus
import com.devesh.spendwise.ui.budget.BudgetViewModel
import com.devesh.spendwise.ui.theme.StitchColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

@RequiresApi(Build.VERSION_CODES.O)
fun getToday(): LocalDate = LocalDate.now()

@RequiresApi(Build.VERSION_CODES.O)
fun getYesterday(): LocalDate = LocalDate.now().minusDays(1)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val repository = remember { ExpenseRepository(db.expenseDao()) }
    val budgetRepository = remember { BudgetRepository(db.budgetDao()) }
    val budgetViewModel = remember { BudgetViewModel(budgetRepository, repository) }

    val budgetStatus by budgetViewModel.budgetStatus.collectAsState()

    val viewModel = remember { ExpenseListViewModel(repository) }

    val expenses by viewModel.expenses.observeAsState(emptyList())

    val today = getToday()
    val yesterday = getYesterday()

    val todayExpenses = expenses.filter { it.date.toLocalDate() == today }
    val yesterdayExpenses = expenses.filter { it.date.toLocalDate() == yesterday }
    val earlierExpenses = expenses.filter { it.date.toLocalDate().isBefore(yesterday) }

    val totalExpenseInt by remember(expenses) {
        mutableStateOf(expenses.sumOf { it.amount }.toInt())
    }

    val animatedTotal by animateIntAsState(
        targetValue = totalExpenseInt,
        animationSpec = tween(
            durationMillis = 900,
            easing = FastOutSlowInEasing
        ),
        label = "TotalExpenseAnimation"
    )

    val dailyAvg by remember(expenses) {
        mutableStateOf(if (expenses.isNotEmpty()) expenses.sumOf { it.amount } / 30.0 else 0.0)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var recentlyDeleted by remember { mutableStateOf<ExpenseEntity?>(null) }

    Scaffold(
        containerColor = StitchColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SpendWiseTopBar(
                onBudgetClick = { navController.navigate(NavRoutes.BUDGET) },
                onAnalyticsClick = { navController.navigate(NavRoutes.ANALYTICS) }
            )
        },

        floatingActionButton = {
            GradientFab(onClick = { navController.navigate(NavRoutes.ADD_EXPENSE) })
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
        ) {
            item {
                TotalExpenseHeroCard(totalAmount = animatedTotal)
            }

            item {
                QuickStatsBentoRow(dailyAvg = dailyAvg)
            }

            item {
                Spacer(Modifier.height(12.dp))
                HomeBudgetSummaryCard(
                    budgetStatus = budgetStatus,
                    onCardClick = { navController.navigate(NavRoutes.BUDGET) }
                )
            }


            if (todayExpenses.isNotEmpty()) {
                item {
                    DateGroupHeader(
                        title = "Today",
                        badgeText = "${todayExpenses.size} Items"
                    )
                }
                items(todayExpenses, key = { it.id }) { expense ->
                    SwipeExpenseItem(
                        expense = expense,
                        viewModel = viewModel,
                        onDelete = { recentlyDeleted = it }
                    )
                }
            }

            if (yesterdayExpenses.isNotEmpty()) {
                item {
                    DateGroupHeader(
                        title = "Yesterday",
                        badgeText = formatShortDate(yesterday)
                    )
                }
                items(yesterdayExpenses, key = { it.id }) { expense ->
                    SwipeExpenseItem(
                        expense = expense,
                        viewModel = viewModel,
                        onDelete = { recentlyDeleted = it }
                    )
                }
            }

            if (earlierExpenses.isNotEmpty()) {
                item {
                    DateGroupHeader(
                        title = "Earlier",
                        badgeText = "Past"
                    )
                }
                items(earlierExpenses, key = { it.id }) { expense ->
                    SwipeExpenseItem(
                        expense = expense,
                        viewModel = viewModel,
                        onDelete = { recentlyDeleted = it }
                    )
                }
            }

            item {
                Spacer(Modifier.height(80.dp))
            }
        }
    }

    LaunchedEffect(recentlyDeleted) {
        recentlyDeleted?.let { expense ->
            val result = snackbarHostState.showSnackbar(
                message = "Expense deleted",
                actionLabel = "UNDO"
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.addExpense(expense)
            }
            recentlyDeleted = null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendWiseTopBar(
    onBudgetClick: (() -> Unit)? = null,
    onAnalyticsClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Text(
                text = "SpendWise",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = StitchColors.OnSurface,
                letterSpacing = (-0.5).sp
            )
        },
        actions = {
            if (onAnalyticsClick != null) {
                IconButton(onClick = onAnalyticsClick) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(StitchColors.PrimaryContainer.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Analytics",
                            tint = StitchColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (onBudgetClick != null) {
                IconButton(onClick = onBudgetClick) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(StitchColors.PrimaryContainer.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Budget",
                            tint = StitchColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .padding(start = 4.dp, end = 16.dp)
                    .size(36.dp)
                    .background(StitchColors.Primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = StitchColors.Background
        )
    )
}



@Composable
fun TotalExpenseHeroCard(totalAmount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            StitchColors.Primary,
                            StitchColors.Secondary,
                            StitchColors.SecondaryContainer
                        )
                    )
                )
                .padding(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Payments,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.15f),
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 16.dp, y = (-16).dp)
            )

            Column {
                Text(
                    text = "TOTAL EXPENSE",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "₹ $totalAmount",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(14.dp))
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.15f),
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Text(
                            text = "Spendings for ${getCurrentMonthYear()}",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickStatsBentoRow(dailyAvg: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = StitchColors.SurfaceContainerLow),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = StitchColors.Tertiary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "DAILY AVG",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchColors.OnSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "₹ ${String.format(Locale.US, "%.2f", dailyAvg)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchColors.OnSurface
                )
            }
        }
    }
}

@Composable
fun DateGroupHeader(title: String, badgeText: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = StitchColors.OnSurface
        )
        if (badgeText != null) {
            Surface(
                shape = CircleShape,
                color = StitchColors.PrimaryContainer.copy(alpha = 0.12f),
                contentColor = StitchColors.Primary
            ) {
                Text(
                    text = badgeText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeExpenseItem(
    expense: ExpenseEntity,
    viewModel: ExpenseListViewModel,
    onDelete: (ExpenseEntity) -> Unit
) {
    val dismissState = rememberDismissState(
        confirmStateChange = { dismissValue ->
            if (dismissValue == DismissValue.DismissedToStart) {
                onDelete(expense)
                viewModel.deleteExpense(expense)
                true
            } else {
                false
            }
        }
    )

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart),
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(24.dp)),
        background = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFEF4444)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 20.dp)
                )
            }
        },
        dismissContent = {
            ExpenseItemCard(expense)
        }
    )
}

@Composable
fun ExpenseItemCard(expense: ExpenseEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = StitchColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        val (icon, containerBg, iconTint) = getCategoryStyle(expense.category)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(containerBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = expense.category,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (expense.note.isNotBlank()) expense.note else expense.category,
                    color = StitchColors.OnSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = expense.category,
                    color = StitchColors.OnSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹ ${expense.amount}",
                    color = StitchColors.OnSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(4.dp))

                Surface(
                    shape = CircleShape,
                    color = StitchColors.SurfaceContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(StitchColors.Primary.copy(alpha = 0.4f), CircleShape)
                        )
                        Text(
                            text = (expense.paymentMode ?: "UPI").uppercase(),
                            color = StitchColors.OnSurfaceVariant,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

private fun getCategoryStyle(category: String): Triple<ImageVector, Color, Color> {
    return when (category.lowercase(Locale.ROOT)) {
        "food", "dining" -> Triple(Icons.Default.Restaurant, Color(0xFFD1FAE5), Color(0xFF059669))
        "transport", "travel" -> Triple(Icons.Default.DirectionsCar, Color(0xFFDCFCE7), Color(0xFF16A34A))
        "shopping" -> Triple(Icons.Default.ShoppingBag, Color(0xFFDBEAFE), Color(0xFF0284C7))
        "fuel" -> Triple(Icons.Default.LocalGasStation, Color(0xFFFEF3C7), Color(0xFFD97706))
        "bills", "utilities" -> Triple(Icons.Default.ReceiptLong, Color(0xFFF3E8FF), Color(0xFF9333EA))
        else -> Triple(Icons.Default.Category, StitchColors.SurfaceContainer, StitchColors.Primary)
    }
}

@Composable
fun GradientFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = Color.Transparent,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF5B5FEF),
                            Color(0xFF7C4DFF)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Expense",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

fun getCurrentMonthYear(): String {
    val formatter = java.text.SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    return formatter.format(java.util.Date())
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatShortDate(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault())
    return date.format(formatter)
}

@Composable
fun HomeBudgetSummaryCard(
    budgetStatus: BudgetStatus,
    onCardClick: () -> Unit
) {
    val progressColor = when (budgetStatus.colorState) {
        BudgetColorState.GREEN -> Color(0xFF10B981)
        BudgetColorState.ORANGE -> Color(0xFFF59E0B)
        BudgetColorState.RED -> Color(0xFFEF4444)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = StitchColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(StitchColors.PrimaryContainer.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = StitchColors.Primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Monthly Budget",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchColors.OnSurface
                        )
                        Text(
                            text = "${budgetStatus.progressPercentage.toInt()}% Used",
                            fontSize = 11.sp,
                            color = StitchColors.OnSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Details",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StitchColors.Primary
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View Budget",
                        tint = StitchColors.Primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Custom Linear Progress Gauge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(StitchColors.SurfaceContainerHighest)
            ) {
                val fraction = (budgetStatus.progressPercentage / 100.0).toFloat().coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction)
                        .clip(CircleShape)
                        .background(progressColor)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "₹ ${budgetStatus.currentMonthExpenses.toInt()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.OnSurface
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = "/ ₹ ${budgetStatus.monthlyBudget.toInt()}",
                        fontSize = 12.sp,
                        color = StitchColors.OnSurfaceVariant
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = progressColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Remaining: ₹ ${budgetStatus.remainingBudget.toInt().coerceAtLeast(0)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = progressColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

