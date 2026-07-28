package com.devesh.spendwise.ui.insights

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.devesh.spendwise.data.local.AppDatabase
import com.devesh.spendwise.data.repository.BudgetRepository
import com.devesh.spendwise.data.repository.DashboardRepository
import com.devesh.spendwise.data.repository.ExpenseRepository
import com.devesh.spendwise.ui.theme.StitchColors
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIInsightsScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val expenseRepo = remember { ExpenseRepository(db.expenseDao()) }
    val budgetRepo = remember { BudgetRepository(db.budgetDao()) }
    val dashboardRepo = remember { DashboardRepository(expenseRepo, budgetRepo) }
    val viewModel = remember { AIInsightsViewModel(dashboardRepo) }

    val dashboardState by viewModel.dashboardState.collectAsState()
    val insights = dashboardState.aiInsights

    Scaffold(
        containerColor = StitchColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AI Insights",
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
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(36.dp)
                            .background(StitchColors.PrimaryContainer.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Active",
                            tint = StitchColors.Primary,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Smart Spending Analysis Header Hero
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    StitchColors.Primary,
                                    StitchColors.Secondary
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "SMART SPENDING ENGINE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f),
                                letterSpacing = 1.2.sp
                            )
                        }

                        Text(
                            text = "Automated Spending Insights",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "Intelligent analysis derived from your local Room database. Updates in real-time as expenses change.",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Row 1: Top Category & Lowest Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InsightBentoCard(
                    modifier = Modifier.weight(1f),
                    title = "Top Category",
                    subtitle = insights.topCategory,
                    value = "₹ ${String.format(Locale.US, "%.2f", insights.topCategoryAmount)}",
                    icon = Icons.Default.Category,
                    iconTint = StitchColors.Primary,
                    containerColor = StitchColors.SurfaceContainerLowest
                )

                InsightBentoCard(
                    modifier = Modifier.weight(1f),
                    title = "Lowest Category",
                    subtitle = insights.lowestCategory,
                    value = "₹ ${String.format(Locale.US, "%.2f", insights.lowestCategoryAmount)}",
                    icon = Icons.Default.Category,
                    iconTint = StitchColors.Secondary,
                    containerColor = StitchColors.SurfaceContainerLowest
                )
            }

            // Row 2: Top Merchant & Visit Frequency
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = StitchColors.SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(StitchColors.TertiaryContainer.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = StitchColors.Tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "TOP MERCHANT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StitchColors.OnSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = insights.topMerchant,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = StitchColors.OnSurface
                            )
                            Text(
                                text = "${insights.topMerchantCount} transactions",
                                fontSize = 12.sp,
                                color = StitchColors.OnSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "₹ ${String.format(Locale.US, "%.2f", insights.topMerchantAmount)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.Primary
                    )
                }
            }

            // Row 3: MoM Spending Trend & Budget Alert Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val isIncrease = insights.momGrowthPercentage >= 0
                InsightBentoCard(
                    modifier = Modifier.weight(1f),
                    title = "MoM Spend Trend",
                    subtitle = if (isIncrease) "Increased vs last month" else "Decreased vs last month",
                    value = "${if (isIncrease) "+" else ""}${String.format(Locale.US, "%.1f", insights.momGrowthPercentage)}%",
                    icon = if (isIncrease) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    iconTint = if (isIncrease) StitchColors.Error else Color(0xFF10B981),
                    containerColor = StitchColors.SurfaceContainerLow
                )

                InsightBentoCard(
                    modifier = Modifier.weight(1f),
                    title = "Budget Status",
                    subtitle = if (insights.isBudgetExceeded) "Budget Exceeded!" else "Within Budget",
                    value = "₹ ${String.format(Locale.US, "%.0f", insights.budgetRemaining)}",
                    icon = Icons.Default.Warning,
                    iconTint = if (insights.isBudgetExceeded) StitchColors.Error else StitchColors.Primary,
                    containerColor = StitchColors.SurfaceContainerLow
                )
            }

            // Row 4: Daily Average & Avg Transaction Value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InsightBentoCard(
                    modifier = Modifier.weight(1f),
                    title = "Daily Average",
                    subtitle = "Current month daily spend",
                    value = "₹ ${String.format(Locale.US, "%.2f", insights.dailyAvgSpending)}",
                    icon = Icons.Default.Payments,
                    iconTint = StitchColors.Primary,
                    containerColor = StitchColors.SurfaceContainerLowest
                )

                InsightBentoCard(
                    modifier = Modifier.weight(1f),
                    title = "Avg Txn Value",
                    subtitle = "${insights.totalTransactions} transactions total",
                    value = "₹ ${String.format(Locale.US, "%.2f", insights.avgTransactionValue)}",
                    icon = Icons.Default.ReceiptLong,
                    iconTint = StitchColors.Secondary,
                    containerColor = StitchColors.SurfaceContainerLowest
                )
            }

            // Row 5: Highest Spending Day
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = StitchColors.SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(StitchColors.PrimaryContainer.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = StitchColors.Primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "PEAK SPENDING DAY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StitchColors.OnSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = insights.highestSpendingDay,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = StitchColors.OnSurface
                            )
                        }
                    }

                    Text(
                        text = "₹ ${String.format(Locale.US, "%.2f", insights.highestSpendingDayAmount)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.Primary
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun InsightBentoCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    containerColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchColors.OnSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
            }

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = StitchColors.OnSurface
            )

            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = StitchColors.OnSurfaceVariant
            )
        }
    }
}
