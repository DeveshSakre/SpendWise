package com.devesh.spendwise.ui.budget

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.devesh.spendwise.data.local.AppDatabase
import com.devesh.spendwise.data.local.BudgetEntity
import com.devesh.spendwise.data.repository.BudgetRepository
import com.devesh.spendwise.data.repository.ExpenseRepository
import com.devesh.spendwise.navigation.NavRoutes
import com.devesh.spendwise.ui.theme.StitchColors
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val budgetRepository = remember { BudgetRepository(db.budgetDao()) }
    val expenseRepository = remember { ExpenseRepository(db.expenseDao()) }
    val viewModel = remember { BudgetViewModel(budgetRepository, expenseRepository) }

    val budgetStatus by viewModel.budgetStatus.collectAsState()
    val categoryBreakdowns by viewModel.categoryBreakdowns.collectAsState()
    val currentBudgetEntity by viewModel.currentBudgetEntity.collectAsState()

    var showSetBudgetDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = StitchColors.Background,
        topBar = {
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
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
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
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header with Month Selector
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "OVERVIEW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchColors.Primary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Monthly Budget",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchColors.OnSurface
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = StitchColors.SurfaceContainerHigh.copy(alpha = 0.5f),
                        modifier = Modifier.clickable { showSetBudgetDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Current Month",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = StitchColors.OnSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = StitchColors.Primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Summary Gauge Hero Card
            item {
                BudgetSummaryGaugeCard(
                    budgetStatus = budgetStatus,
                    onEditClick = { showSetBudgetDialog = true }
                )
            }

            // Smart Insights Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Smart Insights",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.OnSurface
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        item {
                            InsightCard(
                                title = "Budget Status",
                                message = when (budgetStatus.colorState) {
                                    BudgetColorState.GREEN -> "Great job! You've used only ${budgetStatus.progressPercentage.toInt()}% of your budget."
                                    BudgetColorState.ORANGE -> "Careful! You have used ${budgetStatus.progressPercentage.toInt()}% of your monthly limit."
                                    BudgetColorState.RED -> "Alert! You have exceeded ${budgetStatus.progressPercentage.toInt()}% of your budget!"
                                },
                                icon = when (budgetStatus.colorState) {
                                    BudgetColorState.GREEN -> Icons.Default.TrendingDown
                                    BudgetColorState.ORANGE -> Icons.Default.Warning
                                    BudgetColorState.RED -> Icons.Default.Error
                                },
                                iconBg = when (budgetStatus.colorState) {
                                    BudgetColorState.GREEN -> Color(0xFFE6F4EA)
                                    BudgetColorState.ORANGE -> Color(0xFFFEF3C7)
                                    BudgetColorState.RED -> Color(0xFFFEE2E2)
                                },
                                iconTint = when (budgetStatus.colorState) {
                                    BudgetColorState.GREEN -> Color(0xFF10B981)
                                    BudgetColorState.ORANGE -> Color(0xFFD97706)
                                    BudgetColorState.RED -> Color(0xFFEF4444)
                                }
                            )
                        }

                        item {
                            InsightCard(
                                title = "Savings Potential",
                                message = "Remaining budget is ₹ ${budgetStatus.remainingBudget.toInt().coerceAtLeast(0)} for this month.",
                                icon = Icons.Default.AccountBalanceWallet,
                                iconBg = Color(0xFFEFF6FF),
                                iconTint = Color(0xFF2563EB)
                            )
                        }
                    }
                }
            }

            // Category Budgets Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category Budgets",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.OnSurface
                    )
                    IconButton(onClick = { showSetBudgetDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Set Budget",
                            tint = StitchColors.Primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            items(categoryBreakdowns) { breakdown ->
                CategoryBudgetCard(breakdown = breakdown)
            }

            // Quick Actions Footer
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showSetBudgetDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StitchColors.Primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = StitchColors.Primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Edit", color = StitchColors.Primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val current = currentBudgetEntity
                            if (current != null) {
                                viewModel.saveBudget(current.copy(monthlyBudget = 0.0))
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StitchColors.OutlineVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = StitchColors.OnSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Reset", color = StitchColors.OnSurfaceVariant, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Spacer(Modifier.height(40.dp))
            }
        }
    }

    if (showSetBudgetDialog) {
        StitchSetBudgetDialog(
            currentBudget = currentBudgetEntity,
            onDismiss = { showSetBudgetDialog = false },
            onSave = { updatedBudget ->
                viewModel.saveBudget(updatedBudget)
                showSetBudgetDialog = false
            }
        )
    }
}

@Composable
fun BudgetSummaryGaugeCard(
    budgetStatus: BudgetStatus,
    onEditClick: () -> Unit
) {
    val progressColor = when (budgetStatus.colorState) {
        BudgetColorState.GREEN -> Color(0xFF10B981)
        BudgetColorState.ORANGE -> Color(0xFFF59E0B)
        BudgetColorState.RED -> Color(0xFFEF4444)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = (budgetStatus.progressPercentage / 100.0).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000),
        label = "gauge-progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = StitchColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular Progress Canvas
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                val trackColor = StitchColors.SurfaceContainerHighest
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    // Background Track
                    drawArc(
                        color = trackColor,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    // Progress Arc
                    drawArc(
                        color = progressColor,
                        startAngle = 135f,
                        sweepAngle = 270f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${budgetStatus.progressPercentage.toInt()}%",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.OnSurface
                    )
                    Text(
                        text = "USED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.OnSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "₹ ${budgetStatus.currentMonthExpenses.toInt()}",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchColors.OnSurface
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "/ ₹ ${budgetStatus.monthlyBudget.toInt()}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = StitchColors.OnSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))

            Surface(
                shape = CircleShape,
                color = progressColor.copy(alpha = 0.12f),
                modifier = Modifier.clickable { onEditClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = progressColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Remaining: ₹ ${budgetStatus.remainingBudget.toInt()}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryBudgetCard(breakdown: CategoryBudgetBreakdown) {
    val (icon, bgTint, iconTint) = getCategoryStyleTriple(breakdown.categoryName)
    val progressColor = when (breakdown.colorState) {
        BudgetColorState.GREEN -> Color(0xFF10B981)
        BudgetColorState.ORANGE -> Color(0xFFF59E0B)
        BudgetColorState.RED -> Color(0xFFEF4444)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StitchColors.SurfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(bgTint, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = breakdown.categoryName,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = breakdown.categoryName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.OnSurface
                    )
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "₹ ${breakdown.spent.toInt()}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.OnSurface
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = "/ ₹ ${breakdown.budgetLimit.toInt()}",
                        fontSize = 12.sp,
                        color = StitchColors.OnSurfaceVariant
                    )
                }
            }

            // Custom Linear Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(StitchColors.SurfaceContainerHighest)
            ) {
                val fraction = (breakdown.percentage / 100.0).toFloat().coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction)
                        .clip(CircleShape)
                        .background(progressColor)
                )
            }
        }
    }
}

@Composable
fun InsightCard(
    title: String,
    message: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color
) {
    Card(
        modifier = Modifier.width(240.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchColors.OnSurface
                )
                Text(
                    text = message,
                    fontSize = 12.sp,
                    color = StitchColors.OnSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StitchSetBudgetDialog(
    currentBudget: BudgetEntity?,
    onDismiss: () -> Unit,
    onSave: (BudgetEntity) -> Unit
) {
    var monthlyTotal by remember(currentBudget) { mutableStateOf(currentBudget?.monthlyBudget?.toInt()?.toString() ?: "25000") }
    var foodLimit by remember(currentBudget) { mutableStateOf(currentBudget?.foodBudget?.toInt()?.toString() ?: "8000") }
    var shoppingLimit by remember(currentBudget) { mutableStateOf(currentBudget?.shoppingBudget?.toInt()?.toString() ?: "6000") }
    var transportLimit by remember(currentBudget) { mutableStateOf(currentBudget?.transportBudget?.toInt()?.toString() ?: "4000") }
    var fuelLimit by remember(currentBudget) { mutableStateOf(currentBudget?.fuelBudget?.toInt()?.toString() ?: "3000") }
    var othersLimit by remember(currentBudget) { mutableStateOf(currentBudget?.othersBudget?.toInt()?.toString() ?: "4000") }

    val monthlyVal = monthlyTotal.toDoubleOrNull() ?: 0.0
    val foodVal = foodLimit.toDoubleOrNull() ?: 0.0
    val shoppingVal = shoppingLimit.toDoubleOrNull() ?: 0.0
    val transportVal = transportLimit.toDoubleOrNull() ?: 0.0
    val fuelVal = fuelLimit.toDoubleOrNull() ?: 0.0
    val othersVal = othersLimit.toDoubleOrNull() ?: 0.0

    val validationError = remember(monthlyVal, foodVal, shoppingVal, transportVal, fuelVal, othersVal) {
        if (monthlyVal < 0.0 || foodVal < 0.0 || shoppingVal < 0.0 || transportVal < 0.0 || fuelVal < 0.0 || othersVal < 0.0) {
            "Budget amounts cannot be negative."
        } else if (foodVal > monthlyVal || shoppingVal > monthlyVal || transportVal > monthlyVal || fuelVal > monthlyVal || othersVal > monthlyVal) {
            "Category budget cannot exceed the total monthly budget."
        } else if ((foodVal + shoppingVal + transportVal + fuelVal + othersVal) > monthlyVal) {
            "Category allocations (₹ ${(foodVal + shoppingVal + transportVal + fuelVal + othersVal).toInt()}) exceed total monthly budget (₹ ${monthlyVal.toInt()})."
        } else {
            null
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = StitchColors.SurfaceContainerLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Set Budget",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchColors.OnSurface
                        )
                        Text(
                            text = "Define spending limits",
                            fontSize = 12.sp,
                            color = StitchColors.OnSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(StitchColors.PrimaryContainer.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = StitchColors.Primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Total Monthly Budget Input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "TOTAL MONTHLY BUDGET",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.Primary,
                        letterSpacing = 1.sp
                    )
                    OutlinedTextField(
                        value = monthlyTotal,
                        onValueChange = { if (it.all { char -> char.isDigit() }) monthlyTotal = it },
                        singleLine = true,
                        prefix = { Text("₹ ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StitchColors.Primary) },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchColors.OnSurface
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StitchColors.Primary,
                            unfocusedBorderColor = StitchColors.OutlineVariant,
                            focusedContainerColor = StitchColors.SurfaceContainerLow,
                            unfocusedContainerColor = StitchColors.SurfaceContainerLow
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text(
                    text = "Category Limits",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchColors.OnSurface
                )

                // Category Limit Inputs
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryLimitInputRow("Food", foodLimit, { foodLimit = it }, Color(0xFFFFF0E6), Color(0xFFFF8A00))
                    CategoryLimitInputRow("Shopping", shoppingLimit, { shoppingLimit = it }, Color(0xFFFFEBF5), Color(0xFFFF4DA6))
                    CategoryLimitInputRow("Transport", transportLimit, { transportLimit = it }, Color(0xFFE6F0FF), Color(0xFF0066FF))
                    CategoryLimitInputRow("Fuel", fuelLimit, { fuelLimit = it }, Color(0xFFFFF7E6), Color(0xFFFFBF00))
                    CategoryLimitInputRow("Others", othersLimit, { othersLimit = it }, Color(0xFFF2EBFF), Color(0xFF7C4DFF))
                }

                // Validation Warning Banner
                if (validationError != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEE2E2),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = validationError,
                                fontSize = 12.sp,
                                color = Color(0xFFB91C1C),
                                fontWeight = FontWeight.Medium,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = StitchColors.OnSurfaceVariant, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(Modifier.width(12.dp))

                    Button(
                        enabled = validationError == null,
                        onClick = {
                            val now = java.time.LocalDate.now()
                            val updated = BudgetEntity(
                                id = currentBudget?.id ?: 0,
                                month = now.monthValue,
                                year = now.year,
                                monthlyBudget = monthlyVal,
                                foodBudget = foodVal,
                                shoppingBudget = shoppingVal,
                                transportBudget = transportVal,
                                fuelBudget = fuelVal,
                                othersBudget = othersVal
                            )
                            onSave(updated)
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = if (validationError == null) {
                                        Brush.horizontalGradient(colors = listOf(StitchColors.Primary, StitchColors.Secondary))
                                    } else {
                                        Brush.horizontalGradient(colors = listOf(Color.LightGray, Color.Gray))
                                    },
                                    shape = CircleShape
                                )
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Save Budget",
                                color = if (validationError == null) Color.White else Color.DarkGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun CategoryLimitInputRow(
    categoryName: String,
    value: String,
    onValueChange: (String) -> Unit,
    bgTint: Color,
    iconTint: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(bgTint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (categoryName.lowercase(Locale.ROOT)) {
                    "food" -> Icons.Default.Restaurant
                    "shopping" -> Icons.Default.ShoppingBag
                    "transport" -> Icons.Default.DirectionsCar
                    "fuel" -> Icons.Default.LocalGasStation
                    else -> Icons.Default.MoreHoriz
                },
                contentDescription = categoryName,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = categoryName,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = StitchColors.OnSurface,
            modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
            value = value,
            onValueChange = { if (it.all { char -> char.isDigit() }) onValueChange(it) },
            singleLine = true,
            prefix = { Text("₹ ", fontSize = 13.sp, color = StitchColors.OnSurfaceVariant) },
            textStyle = LocalTextStyle.current.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = StitchColors.OnSurface
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = StitchColors.Primary,
                unfocusedBorderColor = StitchColors.OutlineVariant,
                focusedContainerColor = StitchColors.SurfaceContainerLow,
                unfocusedContainerColor = StitchColors.SurfaceContainerLow
            ),
            modifier = Modifier.width(120.dp)
        )
    }
}

private fun getCategoryStyleTriple(category: String): Triple<ImageVector, Color, Color> {
    return when (category.lowercase(Locale.ROOT)) {
        "food", "dining" -> Triple(Icons.Default.Restaurant, Color(0xFFFFF0E6), Color(0xFFFF8A00))
        "transport", "travel" -> Triple(Icons.Default.DirectionsCar, Color(0xFFE6F0FF), Color(0xFF0066FF))
        "shopping" -> Triple(Icons.Default.ShoppingBag, Color(0xFFFFEBF5), Color(0xFFFF4DA6))
        "fuel" -> Triple(Icons.Default.LocalGasStation, Color(0xFFFFF7E6), Color(0xFFFFBF00))
        "others" -> Triple(Icons.Default.MoreHoriz, Color(0xFFF2EBFF), Color(0xFF7C4DFF))
        else -> Triple(Icons.Default.Category, StitchColors.SurfaceContainer, StitchColors.Primary)
    }
}
