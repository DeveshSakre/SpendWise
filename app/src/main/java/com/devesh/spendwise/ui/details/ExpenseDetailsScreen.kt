package com.devesh.spendwise.ui.details

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.devesh.spendwise.data.local.AppDatabase
import com.devesh.spendwise.data.local.ExpenseEntity
import com.devesh.spendwise.data.repository.ExpenseRepository
import com.devesh.spendwise.navigation.NavRoutes
import com.devesh.spendwise.ui.theme.StitchColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailsScreen(
    navController: NavController,
    expenseId: Int
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val repository = remember { ExpenseRepository(db.expenseDao()) }
    val viewModel = remember(expenseId) { ExpenseDetailsViewModel(repository, expenseId) }

    val expenseState by viewModel.expense.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = StitchColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Expense Details",
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
        },
        bottomBar = {
            expenseState?.let { expense ->
                DetailsBottomActionBar(
                    onDeleteClick = { showDeleteDialog = true },
                    onShareClick = { shareExpenseDetails(context, expense) },
                    onEditClick = { navController.navigate(NavRoutes.editExpenseRoute(expense.id)) }
                )
            }
        }
    ) { innerPadding ->
        val expense = expenseState

        if (expense == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = StitchColors.Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                // Header Section: Ambient Background & Amount Display
                ExpenseHeaderSection(expense = expense)

                Spacer(Modifier.height(16.dp))

                // Merchant & Category Card
                MerchantCategoryCard(expense = expense)

                Spacer(Modifier.height(16.dp))

                // 2x2 Bento Details Grid
                ExpenseDetailsGrid(expense = expense, context = context)

                Spacer(Modifier.height(16.dp))

                // Personal Notes Card
                if (expense.note.isNotBlank()) {
                    PersonalNotesCard(note = expense.note)
                    Spacer(Modifier.height(16.dp))
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Expense", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this expense? This action will update your budget and analytics automatically.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteExpense(context) {
                            navController.popBackStack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StitchColors.Error)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = StitchColors.Primary)
                }
            }
        )
    }
}

@Composable
fun ExpenseHeaderSection(expense: ExpenseEntity) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Soft Glow Radial Background
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            StitchColors.Primary.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Text(
                text = "TOTAL SPENT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = StitchColors.OnSurfaceVariant,
                letterSpacing = 1.5.sp
            )

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "₹ ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchColors.OnSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = String.format(Locale.US, "%.2f", expense.amount),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchColors.OnSurface
                )
            }

            // Status Chip
            Surface(
                shape = CircleShape,
                color = StitchColors.Primary.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = StitchColors.Primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (expense.reference != null) "SMS Verified" else "Verified Transaction",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StitchColors.Primary
                    )
                }
            }
        }
    }
}

@Composable
fun MerchantCategoryCard(expense: ExpenseEntity) {
    val (icon, bgContainer, iconTint) = getCategoryStyle(expense.category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = StitchColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = expense.category,
                    tint = iconTint,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (expense.note.isNotBlank()) expense.note else expense.category,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchColors.OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = expense.category,
                        fontSize = 14.sp,
                        color = StitchColors.OnSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(StitchColors.OutlineVariant)
                    )
                    Text(
                        text = (expense.paymentMode ?: "UPI").uppercase(),
                        fontSize = 13.sp,
                        color = StitchColors.Primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExpenseDetailsGrid(expense: ExpenseEntity, context: Context) {
    val clipboardManager = LocalClipboardManager.current

    val dateFormatted = remember(expense.date) {
        val ldt = Instant.ofEpochMilli(expense.date).atZone(ZoneId.systemDefault())
        DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.US).format(ldt)
    }

    val timeFormatted = remember(expense.date) {
        val ldt = Instant.ofEpochMilli(expense.date).atZone(ZoneId.systemDefault())
        DateTimeFormatter.ofPattern("hh:mm a", Locale.US).format(ldt)
    }

    val refCode = expense.reference ?: "TXN${expense.id * 892341}"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Date & Time
            DetailBentoTile(
                modifier = Modifier.weight(1f),
                label = "Date & Time",
                title = dateFormatted,
                subtitle = timeFormatted,
                icon = Icons.Default.CalendarToday,
                iconTint = StitchColors.Secondary
            )

            // Payment Method
            DetailBentoTile(
                modifier = Modifier.weight(1f),
                label = "Payment Method",
                title = (expense.paymentMode ?: "UPI").uppercase(),
                subtitle = "Verified",
                icon = Icons.Default.Payments,
                iconTint = StitchColors.Primary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Source
            DetailBentoTile(
                modifier = Modifier.weight(1f),
                label = "Detection Source",
                title = if (expense.reference != null) "SMS Alert" else "Manual Entry",
                subtitle = if (expense.reference != null) "Auto-parsed" else "User Added",
                icon = if (expense.reference != null) Icons.Default.Sms else Icons.Default.Edit,
                iconTint = StitchColors.Tertiary
            )

            // Reference No
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = StitchColors.SurfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Reference No",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.OnSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = refCode,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchColors.OnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(refCode))
                                Toast.makeText(context, "Reference copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Reference",
                                tint = StitchColors.Primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailBentoTile(
    modifier: Modifier = Modifier,
    label: String,
    title: String,
    subtitle: String,
    icon: ImageVector,
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
                fontSize = 14.sp,
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

@Composable
fun PersonalNotesCard(note: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StitchColors.SurfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Notes,
                    contentDescription = null,
                    tint = StitchColors.OnSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "PERSONAL NOTES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchColors.OnSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = "\"$note\"",
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = StitchColors.OnSurface,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun DetailsBottomActionBar(
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Delete button
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .size(52.dp)
                    .background(StitchColors.ErrorContainer, RoundedCornerShape(16.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Expense",
                    tint = StitchColors.OnErrorContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Share button
            IconButton(
                onClick = onShareClick,
                modifier = Modifier
                    .size(52.dp)
                    .background(StitchColors.SurfaceContainerHigh, RoundedCornerShape(16.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Expense",
                    tint = StitchColors.OnSurface,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Edit primary action button
            Button(
                onClick = onEditClick,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StitchColors.Primary,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Edit Expense",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun getCategoryStyle(category: String): Triple<ImageVector, Color, Color> {
    return when (category.lowercase(Locale.ROOT)) {
        "food", "dining" -> Triple(Icons.Default.Restaurant, Color(0xFFFFEDD5), Color(0xFFEA580C))
        "transport", "travel" -> Triple(Icons.Default.DirectionsCar, Color(0xFFDCFCE7), Color(0xFF16A34A))
        "shopping" -> Triple(Icons.Default.ShoppingBag, Color(0xFFDBEAFE), Color(0xFF0284C7))
        "fuel" -> Triple(Icons.Default.LocalGasStation, Color(0xFFFEF3C7), Color(0xFFD97706))
        "bills", "utilities" -> Triple(Icons.Default.ReceiptLong, Color(0xFFF3E8FF), Color(0xFF9333EA))
        "health" -> Triple(Icons.Default.MedicalServices, Color(0xFFFEE2E2), Color(0xFFDC2626))
        else -> Triple(Icons.Default.Category, StitchColors.SurfaceContainer, StitchColors.Primary)
    }
}

private fun shareExpenseDetails(context: Context, expense: ExpenseEntity) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            "SpendWise Expense Details:\n" +
                    "Amount: ₹${expense.amount}\n" +
                    "Category: ${expense.category}\n" +
                    "Mode: ${expense.paymentMode}\n" +
                    "Note: ${expense.note.ifBlank { "N/A" }}"
        )
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Expense Details"))
}
