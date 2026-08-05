package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemType: String, // "PACKAGE", "FLIGHT", "HOTEL", "CAB", "TRAIN"
    val title: String,
    val subtitle: String,
    val dateOrDuration: String,
    val price: Double,
    val imageUrl: String,
    val detailsJson: String,
    val isBooked: Boolean = false,
    val addedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "wishlist_items")
data class WishlistEntity(
    @PrimaryKey val id: String, // e.g. "package_1", "post_3", "hotel_101"
    val itemType: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val price: Double = 0.0,
    val addedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val agentId: String,
    val sender: String, // "USER" or "AGENT"
    val messageText: String,
    val timestampText: String,
    val quotePrice: Double? = null,
    val quoteTitle: String? = null,
    val isEscrowPaid: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
