package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TravyDao {
    // Cart operations
    @Query("SELECT * FROM cart_items ORDER BY addedTimestamp DESC")
    fun getAllCartItems(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity): Long

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItem(id: Long)

    @Query("UPDATE cart_items SET isBooked = 1 WHERE id = :id")
    suspend fun markAsBooked(id: Long)

    @Query("DELETE FROM cart_items WHERE isBooked = 0")
    suspend fun clearCart()

    // Wishlist operations
    @Query("SELECT * FROM wishlist_items ORDER BY addedTimestamp DESC")
    fun getAllWishlist(): Flow<List<WishlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlist(item: WishlistEntity)

    @Query("DELETE FROM wishlist_items WHERE id = :id")
    suspend fun deleteWishlist(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist_items WHERE id = :id)")
    suspend fun isWishlisted(id: String): Boolean

    // Chat operations
    @Query("SELECT * FROM chat_messages WHERE agentId = :agentId ORDER BY timestamp ASC")
    fun getMessagesForAgent(agentId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity): Long

    @Query("UPDATE chat_messages SET isEscrowPaid = 1 WHERE id = :messageId")
    suspend fun markEscrowPaid(messageId: Long)
}

@Database(
    entities = [CartItemEntity::class, WishlistEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TravyDatabase : RoomDatabase() {
    abstract fun travyDao(): TravyDao
}
