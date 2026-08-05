package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChatMessageEntity
import com.example.data.repository.TravyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AgentChatViewModel(
    private val repository: TravyRepository,
    private val agentId: String
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val messages: StateFlow<List<ChatMessageEntity>> = _messages.asStateFlow()

    private val _messageInput = MutableStateFlow("")
    val messageInput: StateFlow<String> = _messageInput.asStateFlow()

    private val _toastMsg = MutableStateFlow<String?>(null)
    val toastMsg: StateFlow<String?> = _toastMsg.asStateFlow()

    init {
        observeMessages()
        ensureWelcomeMessage()
    }

    private fun observeMessages() {
        viewModelScope.launch {
            repository.getChatMessages(agentId).collect { msgList ->
                _messages.value = msgList
            }
        }
    }

    private fun ensureWelcomeMessage() {
        viewModelScope.launch {
            val agent = repository.getAgents().find { it.id == agentId } ?: repository.getAgents().first()
            if (_messages.value.isEmpty()) {
                repository.sendChatMessage(
                    agentId = agentId,
                    sender = "AGENT",
                    messageText = "Hello! 👋 Welcome to ${agent.name}. How can we customize your trip today?",
                    timestampText = "10:00 AM"
                )
            }
        }
    }

    fun onInputChanged(text: String) {
        _messageInput.value = text
    }

    fun sendMessage() {
        val text = _messageInput.value.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            repository.sendChatMessage(
                agentId = agentId,
                sender = "USER",
                messageText = text
            )
            _messageInput.value = ""

            // Automated Agent response simulation
            val agent = repository.getAgents().find { it.id == agentId } ?: repository.getAgents().first()
            if (text.contains("price", ignoreCase = true) || text.contains("discount", ignoreCase = true) || text.contains("quote", ignoreCase = true)) {
                repository.sendChatMessage(
                    agentId = agentId,
                    sender = "AGENT",
                    messageText = "Thanks for asking! I can offer an exclusive 15% discount for early booking on our ${agent.specializedRegions.firstOrNull() ?: "tropical"} packages.",
                    quotePrice = 850.0,
                    quoteTitle = "Special Direct Chat Discount Quote"
                )
            } else {
                repository.sendChatMessage(
                    agentId = agentId,
                    sender = "AGENT",
                    messageText = "Got it! Let me double check live availability for you right now."
                )
            }
        }
    }

    fun payEscrowQuote(msg: ChatMessageEntity) {
        viewModelScope.launch {
            repository.approveEscrow(msg.id)
            repository.addToCart(
                itemType = "PACKAGE",
                title = msg.quoteTitle ?: "Travel Agent Custom Quote",
                subtitle = "Escrow Held • Agent $agentId",
                dateOrDuration = "Milestone Payment Verified",
                price = msg.quotePrice ?: 0.0,
                imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800"
            )
            _toastMsg.value = "Escrow Payment Held Safely! Package added to Cart 🔒"
        }
    }

    fun clearToast() {
        _toastMsg.value = null
    }
}

class AgentChatViewModelFactory(
    private val repository: TravyRepository,
    private val agentId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgentChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AgentChatViewModel(repository, agentId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
