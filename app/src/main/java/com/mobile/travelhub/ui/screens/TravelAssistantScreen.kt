package com.mobile.travelhub.ui.screens

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mobile.travelhub.data.model.TravelAssistantPlaceReference
import com.mobile.travelhub.ui.components.ChatMarkdownMessage
import com.mobile.travelhub.ui.components.SkeletonBlock
import com.mobile.travelhub.ui.components.SimpleFormTextField
import com.mobile.travelhub.ui.theme.OnSurface
import com.mobile.travelhub.ui.theme.OnSurfaceVariant
import com.mobile.travelhub.ui.theme.PrimaryBlue
import com.mobile.travelhub.ui.theme.SurfaceBg
import com.mobile.travelhub.ui.theme.SurfaceContainerLow
import com.mobile.travelhub.ui.theme.SurfaceContainerLowest
import com.mobile.travelhub.viewmodels.TravelAssistantMessageUi
import com.mobile.travelhub.viewmodels.TravelAssistantRole
import com.mobile.travelhub.viewmodels.TravelAssistantViewModel
import com.mobile.travelhub.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelAssistantScreen(
    tripId: Long? = null,
    groupName: String = "",
    onBack: () -> Unit,
    onPlaceClick: (Long) -> Unit,
    viewModel: TravelAssistantViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(tripId, groupName) {
        viewModel.initialize(tripId = tripId, groupName = groupName)
    }

    LaunchedEffect(state.messages.size, state.isSending) {
        val targetIndex = state.messages.size + if (state.isSending) 1 else 0
        if (targetIndex > 0) {
            listState.animateScrollToItem(targetIndex - 1)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SurfaceBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.ui_910fed801d),
                            color = OnSurface,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = state.groupName.takeIf { it.isNotBlank() } ?: "Travel Hub AI",
                            color = OnSurfaceVariant,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.ui_8a09e03d20),
                            tint = OnSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceBg)
            )
        },
        bottomBar = {
            ChatInputBar(
                value = state.input,
                isSending = state.isSending,
                onValueChange = viewModel::updateInput,
                onSend = {
                    viewModel.sendMessage()
                    focusManager.clearFocus()
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 18.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = state.messages,
                key = { it.id }
            ) { message ->
                ChatMessageBubble(
                    message = message,
                    onPlaceClick = onPlaceClick
                )
            }

            if (state.messages.size == 1 && state.quickPrompts.isNotEmpty()) {
                item(key = "quick-prompts") {
                    QuickPromptRow(
                        prompts = state.quickPrompts,
                        onPromptClick = viewModel::sendMessage
                    )
                }
            }

            if (state.isSending) {
                item(key = "typing") {
                    AssistantTypingBubble()
                }
            }

            state.errorMessage?.let { error ->
                item(key = "error") {
                    Surface(
                        color = Color(0xFFFFEDEA),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = viewModel::clearError)
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(12.dp),
                            color = Color(0xFF9B2C21),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: TravelAssistantMessageUi,
    onPlaceClick: (Long) -> Unit
) {
    val isUser = message.role == TravelAssistantRole.USER
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            if (!isUser) {
                Surface(
                    color = PrimaryBlue,
                    shape = CircleShape,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Surface(
                color = if (isUser) PrimaryBlue else SurfaceContainerLowest,
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isUser) 18.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 18.dp
                ),
                shadowElevation = if (isUser) 0.dp else 1.dp,
                modifier = Modifier.fillMaxWidth(if (isUser) 0.82f else 0.88f)
            ) {
                Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)) {
                    if (isUser) {
                        Text(
                            text = message.content,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        ChatMarkdownMessage(markdown = message.content)
                    }
                }
            }
        }

        if (!isUser && message.places.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 42.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                message.places.forEach { place ->
                    PlaceReferenceCard(
                        place = place,
                        onClick = { onPlaceClick(place.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceReferenceCard(
    place: TravelAssistantPlaceReference,
    onClick: () -> Unit
) {
    Surface(
        color = SurfaceContainerLow,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(20.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 9.dp)
            ) {
                Text(
                    text = place.name,
                    color = OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val detail = buildList {
                    place.province?.takeIf { it.isNotBlank() }?.let(::add)
                    place.averageRating?.let { add(String.format("%.1f/5", it)) }
                    if (place.reviewCount > 0) add("${place.reviewCount} review")
                }.joinToString(" • ")
                if (detail.isNotBlank()) {
                    Text(
                        text = detail,
                        color = OnSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
            Text(
                text = stringResource(R.string.ui_c088a919fc),
                color = PrimaryBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun QuickPromptRow(
    prompts: List<String>,
    onPromptClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(start = 42.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        prompts.forEach { prompt ->
            Surface(
                color = SurfaceContainerLow,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.clickable { onPromptClick(prompt) }
            ) {
                Text(
                    text = prompt,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = OnSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AssistantTypingBubble() {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = PrimaryBlue,
            shape = CircleShape,
            modifier = Modifier.size(34.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            color = SurfaceContainerLowest,
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBlock(
                    modifier = Modifier
                        .width(42.dp)
                        .height(10.dp)
                )
                Text(
                    text = stringResource(R.string.ui_f160dc891e),
                    modifier = Modifier.padding(start = 9.dp),
                    color = OnSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    isSending: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val density = LocalDensity.current
    val imeHeight = WindowInsets.ime.getBottom(density)
    val navBarHeight = WindowInsets.navigationBars.getBottom(density)
    val keyboardPadding = if (imeHeight > 0) {
        with(density) { (imeHeight - navBarHeight).coerceAtLeast(0).toDp() }
    } else {
        0.dp
    }

    Surface(
        color = SurfaceContainerLowest,
        shadowElevation = 8.dp,
        modifier = Modifier.padding(bottom = keyboardPadding)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            SimpleFormTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = stringResource(R.string.ui_56f46885da),
                singleLine = false,
                minLines = 1,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() })
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = if (value.isBlank() || isSending) {
                    SurfaceContainerLow
                } else {
                    PrimaryBlue
                },
                shape = CircleShape,
                modifier = Modifier
                    .size(48.dp)
                    .clickable(
                        enabled = value.isNotBlank() && !isSending,
                        onClick = onSend
                    )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.ui_bbc4e7f57f),
                        tint = if (value.isBlank() || isSending) OnSurfaceVariant else Color.White,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
        }
    }
}
