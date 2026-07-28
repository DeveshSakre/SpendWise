package com.devesh.spendwise.ui.edit

import android.app.DatePickerDialog
import android.os.Build
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.devesh.spendwise.data.local.AppDatabase
import com.devesh.spendwise.data.repository.ExpenseRepository
import com.devesh.spendwise.navigation.NavRoutes
import com.devesh.spendwise.ui.theme.StitchColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

data class CategoryOption(
    val name: String,
    val icon: ImageVector
)

data class PaymentMethodOption(
    val name: String,
    val icon: ImageVector
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseScreen(
    navController: NavController,
    expenseId: Int
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val repository = remember { ExpenseRepository(db.expenseDao()) }
    val viewModel = remember(expenseId) { EditExpenseViewModel(repository, expenseId) }

    val expenseState by viewModel.expense.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val selectedCategory by viewModel.category.collectAsState()
    val selectedPaymentMode by viewModel.paymentMode.collectAsState()
    val note by viewModel.note.collectAsState()
    val dateMillis by viewModel.date.collectAsState()
    val reference by viewModel.reference.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    val categories = remember {
        listOf(
            CategoryOption("Food", Icons.Default.Restaurant),
            CategoryOption("Shopping", Icons.Default.ShoppingBag),
            CategoryOption("Travel", Icons.Default.Commute),
            CategoryOption("Bills", Icons.Default.ReceiptLong),
            CategoryOption("Health", Icons.Default.MedicalServices),
            CategoryOption("Fuel", Icons.Default.LocalGasStation),
            CategoryOption("Others", Icons.Default.Category)
        )
    }

    val paymentMethods = remember {
        listOf(
            PaymentMethodOption("UPI", Icons.Default.AccountBalanceWallet),
            PaymentMethodOption("Cash", Icons.Default.Payments),
            PaymentMethodOption("Card", Icons.Default.CreditCard)
        )
    }

    Scaffold(
        containerColor = StitchColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Expense",
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
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .background(StitchColors.ErrorContainer.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = StitchColors.Error,
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
        if (expenseState == null) {
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
                    .padding(bottom = 32.dp)
            ) {
                // SMS Detection Banner (If detected from SMS)
                if (reference != null) {
                    SmsDetectionInfoBanner()
                    Spacer(Modifier.height(16.dp))
                }

                // Total Amount Editable Hero Card
                EditableAmountHeroCard(
                    amount = amount,
                    onAmountChange = viewModel::onAmountChange,
                    errorMessage = errorMessage
                )

                Spacer(Modifier.height(24.dp))

                // Category Selector Section
                CategorySelectorSection(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelect = viewModel::onCategoryChange
                )

                Spacer(Modifier.height(24.dp))

                // Payment Method Section
                PaymentMethodSelectorSection(
                    paymentMethods = paymentMethods,
                    selectedPaymentMode = selectedPaymentMode,
                    onPaymentModeSelect = viewModel::onPaymentModeChange
                )

                Spacer(Modifier.height(24.dp))

                // Transaction Details Card
                TransactionDetailsSection(
                    dateMillis = dateMillis,
                    onDateClick = {
                        showDatePicker(context, dateMillis) { selectedDateMillis ->
                            viewModel.onDateChange(selectedDateMillis)
                        }
                    },
                    note = note,
                    onNoteChange = viewModel::onNoteChange
                )

                Spacer(Modifier.height(32.dp))

                // Action Buttons
                ActionButtonsSection(
                    onSaveClick = {
                        viewModel.saveExpense(context) {
                            navController.popBackStack()
                        }
                    },
                    onDiscardClick = {
                        navController.popBackStack()
                    }
                )
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
                            navController.navigate(NavRoutes.EXPENSE_LIST) {
                                popUpTo(NavRoutes.EXPENSE_LIST) { inclusive = true }
                            }
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
fun SmsDetectionInfoBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StitchColors.SurfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = StitchColors.Primary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "This expense was automatically detected from your SMS. You can refine any value below.",
                fontSize = 13.sp,
                color = StitchColors.OnSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun EditableAmountHeroCard(
    amount: String,
    onAmountChange: (String) -> Unit,
    errorMessage: String?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = StitchColors.PrimaryContainer.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "TOTAL AMOUNT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = StitchColors.Primary,
                letterSpacing = 1.5.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "₹",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchColors.Primary,
                    modifier = Modifier.padding(end = 4.dp)
                )

                TextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.OnSurface,
                        textAlign = TextAlign.Center
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    modifier = Modifier.width(IntrinsicSize.Min)
                )
            }

            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(StitchColors.Primary.copy(alpha = 0.3f))
            )

            val err = errorMessage
            if (!err.isNullOrBlank()) {
                Text(
                    text = err,
                    color = StitchColors.Error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun CategorySelectorSection(
    categories: List<CategoryOption>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Category",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = StitchColors.OnSurface
            )
            Text(
                text = selectedCategory,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = StitchColors.Primary
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = cat.name.equals(selectedCategory, ignoreCase = true)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .width(72.dp)
                        .clickable { onCategorySelect(cat.name) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) StitchColors.Primary else StitchColors.SurfaceContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = cat.icon,
                            contentDescription = cat.name,
                            tint = if (isSelected) Color.White else StitchColors.OnSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = cat.name,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) StitchColors.Primary else StitchColors.OnSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentMethodSelectorSection(
    paymentMethods: List<PaymentMethodOption>,
    selectedPaymentMode: String,
    onPaymentModeSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Payment Method",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = StitchColors.OnSurface
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            paymentMethods.forEach { method ->
                val isSelected = method.name.equals(selectedPaymentMode, ignoreCase = true)
                Button(
                    onClick = { onPaymentModeSelect(method.name) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) StitchColors.Primary else StitchColors.SurfaceContainer,
                        contentColor = if (isSelected) Color.White else StitchColors.OnSurfaceVariant
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isSelected) 4.dp else 0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = method.icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = method.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionDetailsSection(
    dateMillis: Long,
    onDateClick: () -> Unit,
    note: String,
    onNoteChange: (String) -> Unit
) {
    val formattedDateStr = remember(dateMillis) {
        val ldt = Instant.ofEpochMilli(dateMillis).atZone(ZoneId.systemDefault())
        DateTimeFormatter.ofPattern("MMM dd, yyyy • hh:mm a", Locale.US).format(ldt)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Date Card Tile
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDateClick() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = StitchColors.SurfaceContainerLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(StitchColors.SecondaryContainer.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = StitchColors.Secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Transaction Date",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchColors.OnSurfaceVariant
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = formattedDateStr,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StitchColors.OnSurface
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Select Date",
                    tint = StitchColors.Outline,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Note Card Area
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = StitchColors.SurfaceContainerLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(StitchColors.TertiaryContainer.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = StitchColors.Tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "Merchant / Note",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.OnSurfaceVariant
                    )
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    placeholder = { Text("e.g. Starbucks - Morning Coffee") },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = StitchColors.SurfaceContainerLow,
                        unfocusedContainerColor = StitchColors.SurfaceContainerLow,
                        focusedBorderColor = StitchColors.Primary.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        }
    }
}

@Composable
fun ActionButtonsSection(
    onSaveClick: () -> Unit,
    onDiscardClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = StitchColors.Primary,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Save Changes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        TextButton(
            onClick = onDiscardClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = "Discard Changes",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = StitchColors.Primary
            )
        }
    }
}

private fun showDatePicker(context: android.content.Context, currentMillis: Long, onDateSelected: (Long) -> Unit) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = currentMillis

    val dpd = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val updatedCal = Calendar.getInstance().apply {
                timeInMillis = currentMillis
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            onDateSelected(updatedCal.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    dpd.show()
}
