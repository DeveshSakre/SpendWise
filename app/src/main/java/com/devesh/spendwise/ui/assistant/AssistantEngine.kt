package com.devesh.spendwise.ui.assistant

import com.devesh.spendwise.data.local.ExpenseEntity
import com.devesh.spendwise.data.repository.BudgetRepository
import com.devesh.spendwise.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class AssistantResponse(
    val replyText: String,
    val relatedExpenses: List<ExpenseEntity> = emptyList(),
    val metricHighlight: String? = null
)

interface AssistantEngine {
    suspend fun processQuery(query: String): AssistantResponse
}

class RuleBasedAssistantEngine(
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository
) : AssistantEngine {

    override suspend fun processQuery(query: String): AssistantResponse {
        val q = query.trim().lowercase(Locale.ROOT)
        val expenses = expenseRepository.getAllExpenses().first()
        val budget = budgetRepository.observeLatestBudget().first()

        val now = LocalDate.now()
        val currentMonthExpenses = expenses.filter { expense ->
            val d = Instant.ofEpochMilli(expense.date).atZone(ZoneId.systemDefault()).toLocalDate()
            d.monthValue == now.monthValue && d.year == now.year
        }

        val totalSpentThisMonth = currentMonthExpenses.sumOf { it.amount }
        val monthlyBudget = budget?.monthlyBudget ?: 0.0
        val remainingBudget = monthlyBudget - totalSpentThisMonth

        return when {
            // 1. "How much did I spend this month?" / "Monthly spend"
            q.contains("this month") || q.contains("monthly spend") -> {
                AssistantResponse(
                    replyText = "You have spent ₹${String.format(Locale.US, "%.2f", totalSpentThisMonth)} in ${now.month.name.lowercase().capitalize(Locale.ROOT)} ${now.year} across ${currentMonthExpenses.size} transactions.",
                    metricHighlight = "₹${String.format(Locale.US, "%.2f", totalSpentThisMonth)}",
                    relatedExpenses = currentMonthExpenses.take(5)
                )
            }

            // 2. "How much did I spend on [category]?"
            q.contains("spend on") || q.contains("spent on") || q.contains("category") -> {
                val categoryName = findCategoryInQuery(q)
                if (categoryName != null) {
                    val catExpenses = currentMonthExpenses.filter { it.category.equals(categoryName, ignoreCase = true) }
                    val catTotal = catExpenses.sumOf { it.amount }
                    AssistantResponse(
                        replyText = "You spent ₹${String.format(Locale.US, "%.2f", catTotal)} on $categoryName this month.",
                        metricHighlight = "₹${String.format(Locale.US, "%.2f", catTotal)}",
                        relatedExpenses = catExpenses
                    )
                } else {
                    val topCat = currentMonthExpenses.groupBy { it.category }
                        .mapValues { entry -> entry.value.sumOf { it.amount } }
                        .maxByOrNull { it.value }

                    if (topCat != null) {
                        AssistantResponse(
                            replyText = "Your highest spending category is ${topCat.key} with a total of ₹${String.format(Locale.US, "%.2f", topCat.value)}.",
                            metricHighlight = topCat.key
                        )
                    } else {
                        AssistantResponse(replyText = "I couldn't find any category spending data for this month.")
                    }
                }
            }

            // 3. "Remaining budget" / "budget left"
            q.contains("budget") || q.contains("remaining") || q.contains("left") -> {
                if (monthlyBudget <= 0) {
                    AssistantResponse(replyText = "You haven't set a monthly budget yet. You can set one in the Budgets section!")
                } else {
                    val statusText = if (remainingBudget >= 0) {
                        "You have ₹${String.format(Locale.US, "%.2f", remainingBudget)} remaining out of your ₹${monthlyBudget.toInt()} monthly budget."
                    } else {
                        "Warning: You have exceeded your monthly budget by ₹${String.format(Locale.US, "%.2f", -remainingBudget)}!"
                    }
                    AssistantResponse(
                        replyText = statusText,
                        metricHighlight = "₹${String.format(Locale.US, "%.2f", remainingBudget.coerceAtLeast(0.0))}"
                    )
                }
            }

            // 4. "Show expenses above ₹1000" / "above 500"
            q.contains("above") || q.contains("greater than") || q.contains(">") -> {
                val threshold = extractNumber(q) ?: 1000.0
                val highValueExpenses = currentMonthExpenses.filter { it.amount >= threshold }
                AssistantResponse(
                    replyText = "Found ${highValueExpenses.size} expense(s) above ₹${threshold.toInt()} this month.",
                    relatedExpenses = highValueExpenses
                )
            }

            // 5. "Which merchant did I spend the most at?" / "top merchant"
            q.contains("merchant") || q.contains("store") || q.contains("most at") -> {
                val topMerchant = currentMonthExpenses.groupBy { it.note.ifBlank { it.category }.trim() }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }
                    .maxByOrNull { it.value }

                if (topMerchant != null) {
                    AssistantResponse(
                        replyText = "Your top merchant is '${topMerchant.key}' with a total spend of ₹${String.format(Locale.US, "%.2f", topMerchant.value)}.",
                        metricHighlight = topMerchant.key
                    )
                } else {
                    AssistantResponse(replyText = "No merchant transaction data found for this month.")
                }
            }

            // 6. "What was my biggest expense?" / "highest expense"
            q.contains("biggest") || q.contains("highest expense") || q.contains("max expense") -> {
                val maxExpense = currentMonthExpenses.maxByOrNull { it.amount }
                if (maxExpense != null) {
                    AssistantResponse(
                        replyText = "Your biggest single expense this month was ₹${maxExpense.amount} for '${maxExpense.note.ifBlank { maxExpense.category }}'.",
                        relatedExpenses = listOf(maxExpense)
                    )
                } else {
                    AssistantResponse(replyText = "No expenses recorded this month yet.")
                }
            }

            // 7. "What did I spend yesterday?" / "today"
            q.contains("yesterday") || q.contains("today") -> {
                val targetDate = if (q.contains("yesterday")) now.minusDays(1) else now
                val dateExpenses = expenses.filter {
                    Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate() == targetDate
                }
                val total = dateExpenses.sumOf { it.amount }
                val label = if (q.contains("yesterday")) "yesterday" else "today"
                AssistantResponse(
                    replyText = "You spent ₹${String.format(Locale.US, "%.2f", total)} $label across ${dateExpenses.size} transaction(s).",
                    relatedExpenses = dateExpenses
                )
            }

            // Fallback response
            else -> {
                AssistantResponse(
                    replyText = "I'm your SpendWise Financial Assistant! You can ask me questions like:\n" +
                            "• 'How much did I spend this month?'\n" +
                            "• 'How much did I spend on Food?'\n" +
                            "• 'What is my remaining budget?'\n" +
                            "• 'Which category has the highest spending?'\n" +
                            "• 'Show expenses above ₹1000'\n" +
                            "• 'Which merchant did I spend the most at?'"
                )
            }
        }
    }

    private fun findCategoryInQuery(query: String): String? {
        val categories = listOf("Food", "Shopping", "Fuel", "Transport", "Travel", "Bills", "Health", "Others")
        return categories.find { query.contains(it.lowercase(Locale.ROOT)) }
    }

    private fun extractNumber(query: String): Double? {
        val regex = Regex("\\d+")
        val match = regex.find(query)
        return match?.value?.toDoubleOrNull()
    }
}
