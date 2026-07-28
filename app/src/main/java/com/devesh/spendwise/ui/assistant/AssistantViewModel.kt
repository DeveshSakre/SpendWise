package com.devesh.spendwise.ui.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devesh.spendwise.data.local.ExpenseEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val metricHighlight: String? = null,
    val relatedExpenses: List<ExpenseEntity> = emptyList()
)

class AssistantViewModel(
    private val assistantEngine: AssistantEngine
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                isUser = false,
                text = "Hello! I am your SpendWise Financial Assistant. Ask me anything about your expenses, categories, or budget!"
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    val suggestionChips = listOf(
        "How much did I spend this month?",
        "Food spend?",
        "Remaining budget?",
        "Expenses > ₹1000",
        "Top merchant?"
    )

    fun onInputTextChange(newText: String) {
        _inputText.value = newText
    }

    fun sendMessage(queryText: String? = null) {
        val query = queryText ?: _inputText.value.trim()
        if (query.isBlank()) return

        val userMsg = ChatMessage(isUser = true, text = query)
        _messages.update { it + userMsg }
        _inputText.value = ""

        viewModelScope.launch {
            val response = assistantEngine.processQuery(query)
            val assistantMsg = ChatMessage(
                isUser = false,
                text = response.replyText,
                metricHighlight = response.metricHighlight,
                relatedExpenses = response.relatedExpenses
            )
            _messages.update { it + assistantMsg }
        }
    }
}
