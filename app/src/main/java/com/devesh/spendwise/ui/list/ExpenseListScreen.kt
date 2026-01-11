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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.devesh.spendwise.data.local.AppDatabase
import com.devesh.spendwise.data.local.ExpenseEntity
import com.devesh.spendwise.data.repository.ExpenseRepository
import com.devesh.spendwise.navigation.NavRoutes
import kotlin.collections.setOf
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberDismissState

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.ui.draw.clip
import com.devesh.spendwise.ui.theme.AppTextColor
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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


private val ExpenseCardShape = RoundedCornerShape(20.dp)


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ExpenseListScreen(navController: NavController) {


    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val repository = ExpenseRepository(db.expenseDao())
    val viewModel = remember { ExpenseListViewModel(repository) }

    val expenses by viewModel.expenses.observeAsState(emptyList())

    val today = getToday()
    val yesterday = getYesterday()

    val todayExpenses = expenses.filter { it.date.toLocalDate() == today }
    val yesterdayExpenses = expenses.filter { it.date.toLocalDate() == yesterday }
    val earlierExpenses = expenses.filter { it.date.toLocalDate().isBefore(yesterday) }

    // Force recomposition when expenses change
    val totalExpenseInt by remember(expenses) {
        mutableStateOf(expenses.sumOf { it.amount }.toInt())
    }

// Animate the total
    val animatedTotal by animateIntAsState(
        targetValue = totalExpenseInt,
        animationSpec = tween(
            durationMillis = 900,
            easing = FastOutSlowInEasing
        ),
        label = "TotalExpenseAnimation"
    )



    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var recentlyDeleted by remember { mutableStateOf<ExpenseEntity?>(null) }





    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("SpendWise", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1F2A44)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(NavRoutes.ADD_EXPENSE) },
                containerColor = Color.Transparent,
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(Color(0xFF5C6BC0), Color(0xFF26A69A))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White)
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF4F6FA))
                .padding(16.dp)
        ) {

            /* ---------- TOTAL EXPENSE ---------- */

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFE8F0FE),
                                    Color(0xFFF1F5FF)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            "Total Expense",
                            color = AppTextColor.Secondary,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "₹ $animatedTotal",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTextColor.Primary
                        )
                    }
                }
            }


            Spacer(Modifier.height(16.dp))

            /* ---------- LIST ---------- */

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    // 📅 DATE HEADER (like reference UI)
                    Text(
                        text = "Today · ${getTodayDate()}",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    // 🔽 YOUR EXISTING LIST (UNCHANGED LOGIC)
                    LazyColumn {

                        if (todayExpenses.isNotEmpty()) {
                            item { DateHeader("Today") }
                            items(todayExpenses, key = { it.id }) { expense ->
                                SwipeExpenseItem(expense, viewModel)
                            }
                        }

                        if (yesterdayExpenses.isNotEmpty()) {
                            item { DateHeader("Yesterday") }
                            items(yesterdayExpenses, key = { it.id }) { expense ->
                                SwipeExpenseItem(expense, viewModel)
                            }
                        }

                        if (earlierExpenses.isNotEmpty()) {
                            item { DateHeader("Earlier") }
                            items(earlierExpenses, key = { it.id }) { expense ->
                                SwipeExpenseItem(expense, viewModel)
                            }
                        }
                    }

                }
            }

        }
    }

    /* ---------- UNDO ---------- */

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

fun getTodayDate(): String {
    val formatter = java.text.SimpleDateFormat(
        "MMMM dd, yyyy",
        java.util.Locale.getDefault()
    )
    return formatter.format(java.util.Date())
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun SwipeExpenseItem(
    expense: ExpenseEntity,
    viewModel: ExpenseListViewModel
) {
    val dismissState = rememberDismissState(
        confirmStateChange = { dismissValue ->
            if (dismissValue == DismissValue.DismissedToStart) {
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
            .padding(bottom = 12.dp)
            .clip(ExpenseCardShape),
        background = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseItemCard(expense: ExpenseEntity) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ExpenseCardShape, // ✅ SAME SHAPE
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {

        val (icon, bgColor) = when (expense.category) {
            "Food" -> Icons.Default.Restaurant to Color(0xFFFFB26B)
            "Transport" -> Icons.Default.DirectionsCar to Color(0xFF9CCC65)
            "Shopping" -> Icons.Default.ShoppingBag to Color(0xFFF48FB1)
            "Fuel" -> Icons.Default.LocalGasStation to Color(0xFF64B5F6)
            else -> Icons.Default.MoreHoriz to Color(0xFF4DB6AC)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(bgColor, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White)
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = expense.category,
                    color = AppTextColor.Primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                if (expense.note.isNotBlank()) {
                    Text(
                        text = expense.note,
                        color = AppTextColor.Secondary,
                        fontSize = 12.sp
                    )

                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹ ${expense.amount}",
                    color = AppTextColor.Primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )


                Spacer(Modifier.height(4.dp))


                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = expense.paymentMode ?: "UPI",
                            color = AppTextColor.Primary,
                            fontSize = 12.sp
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFFF1F5F9)
                    )
                )


            }
        }
    }
}

@Composable
fun DateHeader(title: String) {
    Text(
        text = title,
        color = Color.Gray,
        fontSize = 13.sp,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}




