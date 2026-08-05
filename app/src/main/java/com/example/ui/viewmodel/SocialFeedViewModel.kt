package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.SocialComment
import com.example.data.model.SocialPost
import com.example.data.repository.TravyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SocialFeedViewModel(private val repository: TravyRepository) : ViewModel() {

    private val _posts = MutableStateFlow<List<SocialPost>>(emptyList())
    val posts: StateFlow<List<SocialPost>> = _posts.asStateFlow()

    private val _comments = MutableStateFlow<Map<String, List<SocialComment>>>(emptyMap())
    val comments: StateFlow<Map<String, List<SocialComment>>> = _comments.asStateFlow()

    private val _toastMsg = MutableStateFlow<String?>(null)
    val toastMsg: StateFlow<String?> = _toastMsg.asStateFlow()

    init {
        loadPosts()
    }

    private fun loadPosts() {
        _posts.value = repository.getSocialPosts()
        // Initialize mock comments
        _comments.value = mapOf(
            "post_01" to listOf(
                SocialComment("c1", "post_01", "Aria Vance", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200", "The water color looks absolutely surreal!! 😍", "2h ago"),
                SocialComment("c2", "post_01", "Traveler Sam", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200", "Is the speedboat transfer included in the package?", "1h ago")
            )
        )
    }

    fun toggleLike(postId: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val newLiked = !post.isLiked
                val newCount = if (newLiked) post.likesCount + 1 else post.likesCount - 1
                post.copy(isLiked = newLiked, likesCount = newCount)
            } else post
        }
    }

    fun toggleSave(post: SocialPost) {
        viewModelScope.launch {
            val updatedPosts = _posts.value.map {
                if (it.id == post.id) it.copy(isSaved = !it.isSaved) else it
            }
            _posts.value = updatedPosts

            repository.toggleWishlist(
                id = post.id,
                itemType = "POST",
                title = "Spot: ${post.destinationTag}",
                subtitle = post.caption.take(40) + "...",
                imageUrl = post.mediaUrl,
                price = post.linkedPackagePrice ?: 0.0
            )

            _toastMsg.value = if (!post.isSaved) "Saved to Wishlist! ⭐" else "Removed from Wishlist"
        }
    }

    fun addComment(postId: String, text: String) {
        if (text.isBlank()) return
        val newComment = SocialComment(
            id = "c_${System.currentTimeMillis()}",
            postId = postId,
            authorName = "You (Traveler)",
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
            text = text,
            timeAgo = "Just now"
        )
        val currentList = _comments.value[postId] ?: emptyList()
        _comments.value = _comments.value + (postId to (currentList + newComment))

        // Update comment count
        _posts.value = _posts.value.map {
            if (it.id == postId) it.copy(commentsCount = it.commentsCount + 1) else it
        }
    }

    fun clearToast() {
        _toastMsg.value = null
    }
}

class SocialFeedViewModelFactory(private val repository: TravyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SocialFeedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SocialFeedViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
