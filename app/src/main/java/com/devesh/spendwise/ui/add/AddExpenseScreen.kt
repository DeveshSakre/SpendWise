package com.devesh.spendwise.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.devesh.spendwise.data.local.AppDatabase
import com.devesh.spendwise.data.repository.ExpenseRepository
import com.devesh.spendwise.navigation.NavRoutes
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.LocalTextStyle
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.border
import androidx.compose.runtime.getValue
import com.devesh.spendwise.ui.theme.AppTextColor


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(navController: NavController) {

    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val repository = ExpenseRepository(db.expenseDao())
    val viewModel = remember { AddExpenseViewModel(repository) }
    val showAmountDialog = remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    text = "Add Expense",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1F2A44)
                )
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF4F6FA))
                .padding(16.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    /* ---------------- AMOUNT CARD ---------------- */

                    Card(
                        shape = RoundedCornerShape(16.dp),

                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FB))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAmountDialog.value = true }
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                        Text("Amount",
                            color = AppTextColor.Secondary)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("₹", fontSize = 20.sp, fontWeight = FontWeight.Bold,color = AppTextColor.Secondary)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = viewModel.amount.value.ifBlank { "0" },
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTextColor.Primary
                                )
                            }
                        }
                    }

                    if (showAmountDialog.value) {
                        Dialog(
                            onDismissRequest = { showAmountDialog.value = false }
                        ) {
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {

                                    Text(
                                        text = "Enter Amount",
                                        color = AppTextColor.Secondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )

                                    Spacer(Modifier.height(20.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "₹",
                                            color = AppTextColor.Secondary,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(Modifier.width(8.dp))

                                        OutlinedTextField(
                                            value = viewModel.amount.value,
                                            onValueChange = { input ->
                                                if (input.all { it.isDigit() }) {
                                                    viewModel.onAmountChange(input)
                                                }
                                            },
                                            singleLine = true,
                                            textStyle = LocalTextStyle.current.copy(
                                                color = AppTextColor.Secondary,
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Number
                                            ),
                                            modifier = Modifier.width(180.dp)
                                        )
                                    }

                                    Spacer(Modifier.height(24.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        TextButton(
                                            onClick = { showAmountDialog.value = false }
                                        ) {
                                            Text("Cancel",
                                                color = AppTextColor.Secondary)
                                        }

                                        Button(
                                            onClick = { showAmountDialog.value = false },
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text("Done",
                                                color = AppTextColor.Secondary)
                                        }
                                    }
                                }
                            }
                        }
                    }



                    Spacer(Modifier.height(20.dp))

                    /* ---------------- CATEGORY ---------------- */

                    Text(
                        text = "Category",
                        color = AppTextColor.Secondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(12.dp))

                    val categories = listOf(
                        Triple("Food", Icons.Default.Restaurant, Color(0xFFFFB26B)),
                        Triple("Transport", Icons.Default.DirectionsCar, Color(0xFF9CCC65)),
                        Triple("Shopping", Icons.Default.ShoppingBag, Color(0xFFF48FB1)),
                        Triple("Fuel", Icons.Default.LocalGasStation, Color(0xFF64B5F6)),
                        Triple("Others", Icons.Default.MoreHoriz, Color(0xFF4DB6AC))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        categories.forEach { (name, icon, color) ->

                            val isSelected = viewModel.category.value == name

                            val scale by animateFloatAsState(
                                targetValue = if (isSelected) 1.1f else 1f,
                                label = "category-scale"
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { viewModel.onCategoryChange(name) }
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(
                                            color = color,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) Color.Black else Color.Transparent,
                                            shape = RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, contentDescription = null, tint = Color.White)
                                }

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    text = name,
                                    color = AppTextColor.Primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                    }

                    Spacer(Modifier.height(20.dp))

                    /* ---------------- PAYMENT METHOD ---------------- */


                    Text(
                        text = "Payment Method",
                        color = AppTextColor.Secondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(12.dp))

                    Row {
                        listOf("UPI", "Cash", "Card").forEach { method ->
                            FilterChip(
                                selected = viewModel.paymentMode.value == method,
                                onClick = { viewModel.paymentMode.value = method },
                                label = { Text(method) },
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    /* ---------------- DATE ---------------- */

                    Divider()
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Date",
                            color = AppTextColor.Secondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Today",
                                color = AppTextColor.Secondary)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    /* ---------------- NOTE ---------------- */


                    Text(
                        text = "Note",
                        color = AppTextColor.Secondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = viewModel.note.value,
                        onValueChange = { viewModel.onNoteChange(it) },
                        placeholder = { Text(
                            text = "e.g. Lunch, Fuel, Bills",
                            color = AppTextColor.Secondary,
                            fontSize = 13.sp
                        )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedContainerColor = Color(0xFFF5F7FB),
                            focusedContainerColor = Color(0xFFF5F7FB),
                            focusedTextColor =  Color(0xFF000000)
                        )
                    )

                    Spacer(Modifier.height(28.dp))

                    /* ---------------- SAVE BUTTON ---------------- */

                    Button(
                        onClick = {
                            viewModel.saveExpense()
                            navController.navigate(NavRoutes.EXPENSE_LIST) {
                                popUpTo(NavRoutes.ADD_EXPENSE) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        enabled = viewModel.amount.value.isNotBlank()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF5C6BC0),
                                            Color(0xFF26A69A)
                                        )
                                    ),
                                    shape = RoundedCornerShape(28.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "SAVE",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
