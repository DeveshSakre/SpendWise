package com.devesh.spendwise.ui.report

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.devesh.spendwise.data.local.AppDatabase
import com.devesh.spendwise.data.repository.BudgetRepository
import com.devesh.spendwise.data.repository.ExpenseRepository
import com.devesh.spendwise.ui.theme.StitchColors
import com.devesh.spendwise.util.ReportExporter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyReportScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val expenseRepo = remember { ExpenseRepository(db.expenseDao()) }
    val budgetRepo = remember { BudgetRepository(db.budgetDao()) }
    val viewModel = remember { MonthlyReportViewModel(expenseRepo, budgetRepo) }

    val reportData by viewModel.reportData.collectAsState()
    val (selectedMonth, selectedYear) = viewModel.selectedMonthYear.collectAsState().value
    var showMonthMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = StitchColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Financial Report",
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
                    Box(modifier = Modifier.padding(end = 12.dp)) {
                        FilterChip(
                            selected = true,
                            onClick = { showMonthMenu = true },
                            label = {
                                Text(
                                    text = "${reportData.monthName} ${reportData.year}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StitchColors.PrimaryContainer.copy(alpha = 0.15f),
                                selectedLabelColor = StitchColors.Primary
                            ),
                            border = null
                        )

                        DropdownMenu(
                            expanded = showMonthMenu,
                            onDismissRequest = { showMonthMenu = false }
                        ) {
                            viewModel.availableMonths.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.displayName) },
                                    onClick = {
                                        viewModel.selectMonthYear(item.month, item.year)
                                        showMonthMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StitchColors.Background
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Export PDF Button
                    Button(
                        onClick = {
                            ReportExporter.exportPdf(context, reportData)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StitchColors.Primary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("PDF", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Export CSV Button
                    OutlinedButton(
                        onClick = {
                            ReportExporter.exportCsv(context, reportData)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StitchColors.Primary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("CSV", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Share Button
                    IconButton(
                        onClick = {
                            val csv = ReportExporter.exportCsv(context, reportData)
                            if (csv != null) {
                                ReportExporter.shareFile(context, csv, "text/csv")
                            } else {
                                ReportExporter.shareSummaryText(context, reportData)
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(StitchColors.SurfaceContainerHigh, RoundedCornerShape(16.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Report",
                            tint = StitchColors.OnSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
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
            // Report Hero Banner
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
                        Text(
                            text = "${reportData.monthName} ${reportData.year}".uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f),
                            letterSpacing = 1.5.sp
                        )

                        Text(
                            text = "₹ ${String.format(Locale.US, "%.2f", reportData.totalSpending)}",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Budget: ₹ ${reportData.monthlyBudget.toInt()}",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )

                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "Savings: ₹ ${String.format(Locale.US, "%.0f", reportData.savings)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Key Highlights Grid
            Text(
                text = "Key Highlights",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = StitchColors.OnSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReportBentoTile(
                    modifier = Modifier.weight(1f),
                    label = "Top Merchant",
                    title = reportData.topMerchant,
                    subtitle = "₹ ${String.format(Locale.US, "%.2f", reportData.topMerchantAmount)}",
                    icon = Icons.Default.Storefront,
                    iconTint = StitchColors.Primary
                )

                ReportBentoTile(
                    modifier = Modifier.weight(1f),
                    label = "Highest Expense",
                    title = reportData.highestExpense?.category ?: "N/A",
                    subtitle = "₹ ${reportData.highestExpense?.amount ?: 0.0}",
                    icon = Icons.Default.ShoppingBag,
                    iconTint = StitchColors.Secondary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReportBentoTile(
                    modifier = Modifier.weight(1f),
                    label = "Daily Average",
                    title = "₹ ${String.format(Locale.US, "%.2f", reportData.dailyAvgSpending)}",
                    subtitle = "Per day in month",
                    icon = Icons.Default.Payments,
                    iconTint = StitchColors.Tertiary
                )

                ReportBentoTile(
                    modifier = Modifier.weight(1f),
                    label = "Transactions",
                    title = "${reportData.totalTransactions} Items",
                    subtitle = "Avg: ₹ ${String.format(Locale.US, "%.0f", reportData.avgTransactionValue)}",
                    icon = Icons.Default.ReceiptLong,
                    iconTint = StitchColors.Primary
                )
            }

            // Category Breakdown Section
            Text(
                text = "Category Breakdown",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = StitchColors.OnSurface
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = StitchColors.SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (reportData.categoryBreakdown.isEmpty()) {
                        Text(
                            text = "No category data available for this month.",
                            fontSize = 13.sp,
                            color = StitchColors.OnSurfaceVariant
                        )
                    } else {
                        reportData.categoryBreakdown.forEach { (cat, amount) ->
                            val percentage = if (reportData.totalSpending > 0) (amount / reportData.totalSpending) * 100.0 else 0.0
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = StitchColors.OnSurface
                                    )
                                    Text(
                                        text = "₹ ${String.format(Locale.US, "%.2f", amount)} (${percentage.toInt()}%)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StitchColors.Primary
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape)
                                        .background(StitchColors.SurfaceContainerHighest)
                                ) {
                                    val fraction = (percentage / 100.0).toFloat().coerceIn(0f, 1f)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(fraction)
                                            .clip(CircleShape)
                                            .background(StitchColors.Primary)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun ReportBentoTile(
    modifier: Modifier = Modifier,
    label: String,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StitchColors.SurfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchColors.OnSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
            }
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = StitchColors.OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = StitchColors.OnSurfaceVariant
            )
        }
    }
}
