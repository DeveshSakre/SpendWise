package com.devesh.spendwise.ui.add

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.devesh.spendwise.data.repository.ExpenseRepository
import com.devesh.spendwise.navigation.NavRoutes
import com.devesh.spendwise.ui.theme.StitchColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class CategoryItem(
    val name: String,
    val icon: ImageVector,
    val bgTint: Color,
    val iconTint: Color
)

private data class PaymentMethodItem(
    val name: String,
    val icon: ImageVector
)

private fun getTodayDateShort(): String {
    val formatter = SimpleDateFormat("d MMMM", Locale.getDefault())
    return formatter.format(Date())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val repository = ExpenseRepository(db.expenseDao())
    val viewModel = remember { AddExpenseViewModel(repository) }
    val showAmountDialog = remember { mutableStateOf(false) }

    val categories = remember {
        listOf(
            CategoryItem("Food", Icons.Default.Restaurant, Color(0xFFFFF7ED), Color(0xFFEA580C)),
            CategoryItem("Shopping", Icons.Default.ShoppingBag, Color(0xFFFDF2F8), Color(0xFFDB2777)),
            CategoryItem("Fuel", Icons.Default.LocalGasStation, Color(0xFFFEF3C7), Color(0xFFD97706)),
            CategoryItem("Transport", Icons.Default.DirectionsCar, Color(0xFFEFF6FF), Color(0xFF2563EB)),
            CategoryItem("Bills", Icons.Default.ReceiptLong, Color(0xFFFAF5FF), Color(0xFF9333EA)),
            CategoryItem("Others", Icons.Default.Category, Color(0xFFF1F5F9), Color(0xFF475569))
        )
    }

    val paymentMethods = remember {
        listOf(
            PaymentMethodItem("UPI", Icons.Default.AccountBalanceWallet),
            PaymentMethodItem("Cash", Icons.Default.Payments),
            PaymentMethodItem("Card", Icons.Default.CreditCard)
        )
    }

    Scaffold(
        containerColor = StitchColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Expense",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.OnSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = StitchColors.OnSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
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
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Amount Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAmountDialog.value = true },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = StitchColors.SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(StitchColors.Primary.copy(alpha = 0.05f), CircleShape)
                            .align(Alignment.TopEnd)
                            .offset(x = 20.dp, y = (-20).dp)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "AMOUNT SPENT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchColors.OnSurfaceVariant,
                            letterSpacing = 1.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "₹",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = StitchColors.Primary
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = viewModel.amount.value.ifBlank { "0" },
                                fontSize = 44.sp,
                                fontWeight = FontWeight.Bold,
                                color = StitchColors.OnSurface
                            )
                        }
                    }
                }
            }

            // Category Selector
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Category",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchColors.OnSurface
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = viewModel.category.value.equals(cat.name, ignoreCase = true)
                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.1f else 1.0f,
                            label = "cat-scale"
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { viewModel.onCategoryChange(cat.name) }
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(cat.bgTint, CircleShape)
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) StitchColors.Primary else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = cat.name,
                                    tint = cat.iconTint,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
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

            // Payment Method Selector
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = StitchColors.SurfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    paymentMethods.forEach { method ->
                        val isSelected = viewModel.paymentMode.value.equals(method.name, ignoreCase = true)
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.paymentMode.value = method.name },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color.White else Color.Transparent
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (isSelected) 2.dp else 0.dp
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(vertical = 12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = method.icon,
                                    contentDescription = method.name,
                                    tint = if (isSelected) StitchColors.Primary else StitchColors.OnSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = method.name,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) StitchColors.Primary else StitchColors.OnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Details Section (Date & Notes)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Date Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StitchColors.SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(StitchColors.SecondaryContainer.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = StitchColors.Secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Date",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StitchColors.OnSurfaceVariant
                            )
                            Text(
                                text = "Today, ${getTodayDateShort()}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = StitchColors.OnSurface
                            )
                        }
                    }
                }

                // Notes Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StitchColors.SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(StitchColors.Tertiary.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                tint = StitchColors.Tertiary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notes",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StitchColors.OnSurfaceVariant
                            )
                            Spacer(Modifier.height(2.dp))
                            OutlinedTextField(
                                value = viewModel.note.value,
                                onValueChange = { input -> viewModel.onNoteChange(input) },
                                placeholder = {
                                    Text(
                                        text = "e.g. Lunch, Fuel, Bills",
                                        color = StitchColors.OutlineVariant,
                                        fontSize = 14.sp
                                    )
                                },
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(
                                    color = StitchColors.OnSurface,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    disabledBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Save Expense Action Button
            Button(
                onClick = {
                    viewModel.saveExpense(context)
                    navController.navigate(NavRoutes.EXPENSE_LIST) {
                        popUpTo(NavRoutes.ADD_EXPENSE) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                enabled = viewModel.amount.value.isNotBlank()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = if (viewModel.amount.value.isNotBlank()) {
                                    listOf(StitchColors.Primary, StitchColors.Secondary)
                                } else {
                                    listOf(StitchColors.OutlineVariant, StitchColors.OutlineVariant)
                                }
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "SAVE EXPENSE",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }

    // Redesigned Amount Dialog
    if (showAmountDialog.value) {
        StitchAmountEntryDialog(
            amount = viewModel.amount.value,
            onAmountChange = { input -> viewModel.onAmountChange(input) },
            onDismiss = { showAmountDialog.value = false },
            onConfirm = { showAmountDialog.value = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StitchAmountEntryDialog(
    amount: String,
    onAmountChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = StitchColors.SurfaceContainerLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Enter Amount",
                    color = StitchColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "₹",
                        color = StitchColors.Primary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.width(8.dp))

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                onAmountChange(input)
                            }
                        },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            color = StitchColors.OnSurface,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StitchColors.Primary,
                            unfocusedBorderColor = StitchColors.OutlineVariant,
                            focusedContainerColor = StitchColors.SurfaceContainerLow,
                            unfocusedContainerColor = StitchColors.SurfaceContainerLow
                        ),
                        modifier = Modifier
                            .width(180.dp)
                            .focusRequester(focusRequester)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text(
                            text = "Cancel",
                            color = StitchColors.Primary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Button(
                        onClick = onConfirm,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            StitchColors.Primary,
                                            StitchColors.Secondary
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Done",
                                color = Color.White,
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
