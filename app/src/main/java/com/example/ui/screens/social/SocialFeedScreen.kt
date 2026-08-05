package com.example.ui.screens.social

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.SocialPost
import com.example.ui.viewmodel.SocialFeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialFeedScreen(
    viewModel: SocialFeedViewModel,
    onNavigateToPackageDetail: (String) -> Unit
) {
    val posts by viewModel.posts.collectAsState()
    val commentsMap by viewModel.comments.collectAsState()
    val toastMsg by viewModel.toastMsg.collectAsState()

    var activeCommentPostId by remember { mutableStateOf<String?>(null) }
    var isReelMode by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(toastMsg) {
        toastMsg?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    if (activeCommentPostId != null) {
        val postId = activeCommentPostId!!
        val postComments = commentsMap[postId] ?: emptyList()
        var newCommentText by remember { mutableStateOf("") }

        ModalBottomSheet(onDismissRequest = { activeCommentPostId = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Comments",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(postComments) { comment ->
                        Row(verticalAlignment = Alignment.Top) {
                            AsyncImage(
                                model = comment.avatarUrl,
                                contentDescription = comment.authorName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = comment.authorName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(text = comment.text, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(text = comment.timeAgo, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("Add a comment...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("comment_input_field"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            viewModel.addComment(postId, newCommentText)
                            newCommentText = ""
                        },
                        modifier = Modifier.testTag("submit_comment_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 90.dp)
    ) {
        // --- HEADER ---
        Surface(shadowElevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Travel Discovery Feed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Inspiration, Reels & Direct Package Links",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilterChip(
                    selected = isReelMode,
                    onClick = { isReelMode = !isReelMode },
                    label = { Text(if (isReelMode) "Reels Mode 🎬" else "Card Feed 📱") },
                    leadingIcon = { Icon(imageVector = Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }

        if (isReelMode) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(posts) { post ->
                    ReelFullItemCard(
                        post = post,
                        onLikeClick = { viewModel.toggleLike(post.id) },
                        onCommentClick = { activeCommentPostId = post.id },
                        onSaveClick = { viewModel.toggleSave(post) },
                        onBookPackageClick = { pkgId -> onNavigateToPackageDetail(pkgId) }
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(posts) { post ->
                    SocialFeedCard(
                        post = post,
                        onLikeClick = { viewModel.toggleLike(post.id) },
                        onCommentClick = { activeCommentPostId = post.id },
                        onSaveClick = { viewModel.toggleSave(post) },
                        onBookPackageClick = { pkgId -> onNavigateToPackageDetail(pkgId) }
                    )
                }
            }
        }
    }
}

@Composable
fun SocialFeedCard(
    post: SocialPost,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onSaveClick: () -> Unit,
    onBookPackageClick: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = post.authorAvatarUrl,
                    contentDescription = post.authorName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = post.authorName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "📍 ${post.destinationTag}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Media
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                AsyncImage(
                    model = post.mediaUrl,
                    contentDescription = post.caption,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (post.mediaType == "REEL") {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Reel",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(54.dp)
                    )
                }

                if (post.linkedPackageId != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .clickable { onBookPackageClick(post.linkedPackageId) },
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.ConfirmationNumber, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Book Trip: \$${post.linkedPackagePrice?.toInt() ?: 999}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Action Buttons Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLikeClick) {
                        Icon(
                            imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (post.isLiked) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(text = "${post.likesCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(onClick = onCommentClick) {
                        Icon(imageVector = Icons.Outlined.ChatBubbleOutline, contentDescription = "Comment")
                    }
                    Text(text = "${post.commentsCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = onSaveClick) {
                    Icon(
                        imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (post.isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Caption
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp).padding(bottom = 12.dp)) {
                Text(text = post.caption, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun ReelFullItemCard(
    post: SocialPost,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onSaveClick: () -> Unit,
    onBookPackageClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .background(Color.Black)
    ) {
        AsyncImage(
            model = post.mediaUrl,
            contentDescription = post.caption,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
        )

        // Overlay Action Icons on Right Side
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = onLikeClick) {
                Icon(
                    imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (post.isLiked) Color.Red else Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(text = "${post.likesCount}", color = Color.White, fontSize = 11.sp)

            Spacer(modifier = Modifier.height(12.dp))

            IconButton(onClick = onCommentClick) {
                Icon(imageVector = Icons.Outlined.Chat, contentDescription = "Comment", tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Text(text = "${post.commentsCount}", color = Color.White, fontSize = 11.sp)

            Spacer(modifier = Modifier.height(12.dp))

            IconButton(onClick = onSaveClick) {
                Icon(
                    imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Save",
                    tint = if (post.isSaved) MaterialTheme.colorScheme.primary else Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Overlay Caption & Package CTA on Bottom Left
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .fillMaxWidth(0.75f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = post.authorAvatarUrl,
                    contentDescription = post.authorName,
                    modifier = Modifier.size(32.dp).clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = post.authorName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = post.caption, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, maxLines = 2)

            if (post.linkedPackageId != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { onBookPackageClick(post.linkedPackageId) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Book This Trip (\$${post.linkedPackagePrice?.toInt()})", fontSize = 12.sp)
                }
            }
        }
    }
}
