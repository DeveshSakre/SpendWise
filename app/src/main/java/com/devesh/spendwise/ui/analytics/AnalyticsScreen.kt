package com.devesh.spendwise.ui.analytics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.devesh.spendwise.data.local.AppDatabase
import com.devesh.spendwise.data.repository.BudgetRepository
import com.devesh.spendwise.data.repository.ExpenseRepository
import com.devesh.spendwise.navigation.NavRoutes
import com.devesh.spendwise.ui.theme.StitchColors
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val expenseRepository = remember { ExpenseRepository(db.expenseDao()) }
    val budgetRepository = remember { BudgetRepository(db.budgetDao()) }
    val viewModel = remember { AnalyticsViewModel(expenseRepository, budgetRepository) }

    val state by viewModel.analyticsState.collectAsState()

    Scaffold(
        containerColor = StitchColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Analytics",
                        fontSize = 22.sp,
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
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(36.dp)
                            .background(StitchColors.Primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
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
            // Header Overview Title
            item {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = "FINANCIAL INSIGHTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.Primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Spending Dashboard",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.OnSurface
                    )
                }
            }

            // Hero Budget Utilisation Banner
            item {
                AnalyticsHeroBudgetCard(
                    state = state,
                    onBudgetClick = { navController.navigate(NavRoutes.BUDGET) }
                )
            }

            // 2x2 Bento Summary Grid Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BentoStatCard(
                            title = "Daily",
                            value = "₹ ${state.dailySpending.toInt()}",
                            icon = Icons.AutoMirrored.Filled.TrendingDown,
                            iconBg = Color(0xFFEFF6FF),
                            iconTint = Color(0xFF2563EB),
                            modifier = Modifier.weight(1f)
                        )
                        BentoStatCard(
                            title = "Weekly",
                            value = "₹ ${state.weeklySpending.toInt()}",
                            icon = Icons.Default.CalendarToday,
                            iconBg = Color(0xFFF3E8FF),
                            iconTint = Color(0xFF9333EA),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BentoStatCard(
                            title = "Monthly",
                            value = "₹ ${state.totalMonthlySpending.toInt()}",
                            icon = Icons.Default.AccountBalanceWallet,
                            iconBg = Color(0xFFECFDF5),
                            iconTint = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                        BentoStatCard(
                            title = "Daily Avg",
                            value = "₹ ${state.averageDailySpending.toInt()}",
                            icon = Icons.Default.Speed,
                            iconBg = Color(0xFFFFF7E6),
                            iconTint = Color(0xFFD97706),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Highlights Row (Top Category & Top Merchant)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Highlights",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.OnSurface
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        item {
                            HighlightCard(
                                label = "Top Category",
                                name = state.topSpendingCategory,
                                amount = "₹ ${state.topSpendingCategoryAmount.toInt()}",
                                icon = Icons.Default.Category,
                                iconBg = Color(0xFFFFF0E6),
                                iconTint = Color(0xFFFF8A00)
                            )
                        }

                        item {
                            HighlightCard(
                                label = "Top Merchant",
                                name = state.topMerchant,
                                amount = "₹ ${state.topMerchantAmount.toInt()}",
                                icon = Icons.Default.Storefront,
                                iconBg = Color(0xFFFFEBF5),
                                iconTint = Color(0xFFFF4DA6)
                            )
                        }
                    }
                }
            }

            // 7-Day Spending Trend Bar Chart
            item {
                SpendingTrendChartCard(trends = state.dailySpendingTrends)
            }

            // Category Breakdown Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category Distribution",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.OnSurface
                    )
                    Text(
                        text = "${state.categoryBreakdowns.size} Categories",
                        fontSize = 12.sp,
                        color = StitchColors.OnSurfaceVariant
                    )
                }
            }

            if (state.categoryBreakdowns.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = StitchColors.SurfaceContainerLow)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No expense data for this month",
                                fontSize = 14.sp,
                                color = StitchColors.OnSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(state.categoryBreakdowns) { item ->
                    CategoryAnalyticsRowCard(item = item)
                }
            }

            item {
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun AnalyticsHeroBudgetCard(
    state: AnalyticsState,
    onBudgetClick: () -> Unit
) {
    val progressFraction = (state.budgetUtilisationPercentage / 100.0).toFloat().coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 1000),
        label = "hero-progress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBudgetClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            StitchColors.Primary,
                            StitchColors.Secondary,
                            StitchColors.SecondaryContainer
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "MONTHLY SPENDING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "₹ ${state.totalMonthlySpending.toInt()}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color.Green, CircleShape)
                            )
                            Text(
                                text = "${state.budgetUtilisationPercentage.toInt()}% Used",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Progress Bar
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "REMAINING BUDGET",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "₹ ${state.remainingBudget.toInt().coerceAtLeast(0)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Budget: ₹ ${state.monthlyBudget.toInt()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BentoStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = StitchColors.SurfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title.uppercase(Locale.ROOT),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchColors.OnSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchColors.OnSurface
                )
            }
        }
    }
}

@Composable
fun HighlightCard(
    label: String,
    name: String,
    amount: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color
) {
    Card(
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
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
                    text = label.uppercase(Locale.ROOT),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchColors.OnSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchColors.OnSurface,
                    maxLines = 1
                )
                Text(
                    text = amount,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StitchColors.Primary
                )
            }
        }
    }
}

@Composable
fun SpendingTrendChartCard(trends: List<DailyTrendItem>) {
    val maxAmount = remember(trends) { trends.maxOfOrNull { it.amount }?.coerceAtLeast(1.0) ?: 1.0 }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = StitchColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "7-Day Spending Trend",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchColors.OnSurface
                )
                Text(
                    text = "Past 7 Days",
                    fontSize = 11.sp,
                    color = StitchColors.OnSurfaceVariant
                )
            }

            // Custom Canvas Bar Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val barWidth = 24.dp.toPx()
                    val cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                    val canvasHeight = size.height - 30.dp.toPx()
                    val spacing = (size.width - (trends.size * barWidth)) / (trends.size + 1)

                    trends.forEachIndexed { index, item ->
                        val x = spacing + index * (barWidth + spacing)
                        val heightFraction = (item.amount / maxAmount).toFloat().coerceIn(0.05f, 1.0f)
                        val barHeight = canvasHeight * heightFraction
                        val y = canvasHeight - barHeight

                        // Draw background track
                        drawRoundRect(
                            color = Color(0xFFEFECF9),
                            topLeft = Offset(x, 0f),
                            size = Size(barWidth, canvasHeight),
                            cornerRadius = cornerRadius
                        )

                        // Draw progress bar
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF5B5FEF), Color(0xFF7C4DFF))
                            ),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = cornerRadius
                        )
                    }
                }

                // X-Axis Day Labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    trends.forEach { item ->
                        Text(
                            text = item.dayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StitchColors.OnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryAnalyticsRowCard(item: CategoryAnalyticsItem) {
    val (icon, bgTint, iconTint) = getCategoryStyleTriple(item.categoryName)

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
                            contentDescription = item.categoryName,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = item.categoryName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchColors.OnSurface
                        )
                        Text(
                            text = "${item.percentage.toInt()}% of monthly spend",
                            fontSize = 11.sp,
                            color = StitchColors.OnSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "₹ ${item.totalAmount.toInt()}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchColors.OnSurface
                )
            }

            // Custom Linear Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(StitchColors.SurfaceContainerHighest)
            ) {
                val fraction = (item.percentage / 100.0).toFloat().coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction)
                        .clip(CircleShape)
                        .background(iconTint)
                )
            }
        }
    }
}

private fun getCategoryStyleTriple(category: String): Triple<ImageVector, Color, Color> {
    return when (category.lowercase(Locale.ROOT)) {
        "food", "dining" -> Triple(Icons.Default.Restaurant, Color(0xFFFFF0E6), Color(0xFFFF8A00))
        "transport", "travel" -> Triple(Icons.Default.DirectionsCar, Color(0xFFE6F0FF), Color(0xFF0066FF))
        "shopping" -> Triple(Icons.Default.ShoppingBag, Color(0xFFFFEBF5), Color(0xFFFF4DA6))
        "fuel" -> Triple(Icons.Default.LocalGasStation, Color(0xFFFFF7E6), Color(0xFFFFBF00))
        "bills", "utilities" -> Triple(Icons.Default.ReceiptLong, Color(0xFFF3E8FF), Color(0xFF9333EA))
        "others" -> Triple(Icons.Default.MoreHoriz, Color(0xFFF2EBFF), Color(0xFF7C4DFF))
        else -> Triple(Icons.Default.Category, StitchColors.SurfaceContainer, StitchColors.Primary)
    }
}
