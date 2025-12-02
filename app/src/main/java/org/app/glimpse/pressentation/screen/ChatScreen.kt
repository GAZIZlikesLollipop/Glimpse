@file:OptIn(ExperimentalTime::class)

package org.app.glimpse.pressentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.navigation.NavController
import coil3.ImageLoader
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.launch
import org.app.glimpse.R
import org.app.glimpse.data.network.ApiState
import org.app.glimpse.data.network.ApiViewModel
import org.app.glimpse.data.network.Message
import org.app.glimpse.data.network.User
import org.app.glimpse.pressentation.components.createUnsafeOkHttpClient
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime

@Composable
fun ChatScreen(
    friendId: Long,
    paddingValues: PaddingValues,
    navController: NavController,
    apiViewModel: ApiViewModel
) {
    BackHandler {
        navController.popBackStack()
        apiViewModel.isChats = true
    }
    val apiState by apiViewModel.userData.collectAsState()
    val context = LocalContext.current
    val chatState by apiViewModel.chatMessages.collectAsState()
    if(apiState is ApiState.Success) {
        val focusManager = LocalFocusManager.current
        val scope = rememberCoroutineScope()
        val userData = (apiState as ApiState.Success).data as User
        val windowInfo = LocalWindowInfo.current
        val data = userData.friends.find { it.id == friendId }!!
        val time = Instant.ofEpochMilli(data.lastOnline).atZone(ZoneId.systemDefault()).toLocalDateTime()
        val timeDiff = Duration.between(time, LocalDateTime.now(ZoneId.systemDefault()))
        val cnt = stringArrayResource(R.array.chat_cnt)
        val lastOnline =
            if (timeDiff.toSeconds() < 4) {
                cnt[0]
            } else {
                when {
                    timeDiff.toSeconds() < 60 -> "${timeDiff.toSeconds()} ${cnt[1]}"
                    timeDiff.toMinutes() < 60 -> "${timeDiff.toMinutes()} ${cnt[2]}"
                    timeDiff.toHours() < 24 -> "${timeDiff.toHours()} ${cnt[3]}"
                    timeDiff.toDays() < 30 -> "${timeDiff.toDays()} ${cnt[4]}"
                    timeDiff.toDays() < 365 -> "${timeDiff.toDays() / 30.44} ${cnt[5]}"
                    else -> "${timeDiff.toDays() / 365} ${cnt[6]}"
                }
            }
        var chatMessage by remember { mutableStateOf(TextFieldValue("")) }
        val messages = remember { mutableStateMapOf<String, List<Message>>() }
        var showPopUp by rememberSaveable { mutableStateOf(false) }

        var pressOffset by remember { mutableStateOf(Offset.Zero) }

        var currInd by rememberSaveable { mutableLongStateOf(0) }

        var isUpdate by rememberSaveable { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }

        val listState = rememberLazyListState()
        LaunchedEffect(Unit) {
            if(apiViewModel.webSocketCnn == null){
                apiViewModel.startWebSocket()
            }
            if(messages.isNotEmpty()) {
                scope.launch {
                    listState.animateScrollToItem(
                        messages.toList().reversed().last().second.lastIndex
                    )
                }
            }
        }

        LaunchedEffect(chatState) {
            if(chatState is ApiState.Success){
                val rawMessages = (chatState as ApiState.Success).data as List<Message>
                val msgs = rawMessages.sortedBy { it.createdAt }
                messages.clear()
                messages.putAll(groupingMessages(msgs))
                if(messages.isNotEmpty()) {
                    scope.launch {
                        listState.animateScrollToItem(
                            messages.toList().reversed().last().second.lastIndex
                        )
                    }
                }
            }
        }

        Box(Modifier.fillMaxSize()){
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(top = paddingValues.calculateTopPadding()),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SubcomposeAsyncImage(
                        model = data.avatar,
                        imageLoader = ImageLoader.Builder(context)
                            .components { add(OkHttpNetworkFetcherFactory(createUnsafeOkHttpClient())) }
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .size((windowInfo.containerSize.width / 22).dp)
                            .clip(RoundedCornerShape(20.dp)),
                        loading = {
                            Box(Modifier.fillMaxSize().shimmer().background(MaterialTheme.colorScheme.onBackground))
                        },
                        error = {
                            Box(Modifier.fillMaxSize().shimmer().background(MaterialTheme.colorScheme.onBackground))
                        },
                        success = {
                            SubcomposeAsyncImageContent()
                        }
                    )
                    Column {
                        Text(
                            data.name,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = lastOnline,
                            color = if (timeDiff.toSeconds() < 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(
                                0.5f
                            )
                        )
                    }
                    Button(
                        onClick = { navController.popBackStack() },
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            Color.Transparent,
                            MaterialTheme.colorScheme.onBackground
                        ),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(
                            2.dp,
                            MaterialTheme.colorScheme.onBackground.copy(0.8f)
                        ),
                        modifier = Modifier.size((windowInfo.containerSize.width / 30).dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Back"
                        )
                    }
                }

                HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.onBackground.copy(0.35f))

                if(messages.isNotEmpty()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        items(messages.toList().reversed()) { (date, mgs) ->
                            Text(
                                date,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground.copy(0.75f)
                            )
                            Spacer(Modifier.height(24.dp))
                            mgs.forEach { msg ->
                                val createdAt = LocalDateTime.ofInstant(
                                    Instant.ofEpochMilli(msg.createdAt),
                                    ZoneId.systemDefault()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onLongPress = {
                                                    pressOffset = it
                                                    currInd = msg.id
                                                    showPopUp = true
                                                },
                                            )
                                        }
                                ) {
                                    if (msg.senderId == userData.id) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Card(
                                                shape = RoundedCornerShape(
                                                    topStart = 24.dp,
                                                    topEnd = 0.dp,
                                                    bottomEnd = 24.dp,
                                                    bottomStart = 24.dp
                                                ),
                                                modifier = Modifier.padding(8.dp),
                                                colors = CardDefaults.cardColors(
                                                    MaterialTheme.colorScheme.primaryContainer,
                                                    MaterialTheme.colorScheme.onBackground
                                                )
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(12.dp),
                                                    horizontalAlignment = Alignment.End
                                                ) {
                                                    Text(msg.content)
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            8.dp
                                                        )
                                                    ) {
                                                        Text(
                                                            "${createdAt.hour}:${if (createdAt.minute < 10) "0${createdAt.minute}" else createdAt.minute}",
                                                            color = MaterialTheme.colorScheme.onBackground.copy(
                                                                0.5f
                                                            ),
                                                            style = MaterialTheme.typography.labelLarge
                                                        )
                                                        Icon(
                                                            imageVector = if (msg.id != mgs.last().id || userData.friends.find { it.id == friendId }?.lastOnline!! >= msg.createdAt) ImageVector.vectorResource(
                                                                R.drawable.done_all
                                                            ) else Icons.Rounded.Check,
                                                            contentDescription = "isChecked",
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Start
                                        ) {
                                            Card(
                                                shape = RoundedCornerShape(
                                                    topStart = 0.dp,
                                                    topEnd = 24.dp,
                                                    bottomEnd = 24.dp,
                                                    bottomStart = 24.dp
                                                ),
                                                modifier = Modifier.padding(8.dp),
                                                colors = CardDefaults.cardColors(
                                                    MaterialTheme.colorScheme.surfaceContainer,
                                                    MaterialTheme.colorScheme.onBackground
                                                )
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(12.dp),
                                                    horizontalAlignment = Alignment.End
                                                ) {
                                                    Text(msg.content)
                                                    Text(
                                                        "${createdAt.hour}:${if (createdAt.minute < 10) "0${createdAt.minute}" else createdAt.minute}",
                                                        color = MaterialTheme.colorScheme.onBackground.copy(
                                                            0.5f
                                                        ),
                                                        style = MaterialTheme.typography.labelLarge
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.padding(30.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.onBackground.copy(0.05f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ChatBubbleOutline,
                                    contentDescription = cnt[10],
                                    tint = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(50.dp)
                                )
                                Text(
                                    cnt[10],
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = paddingValues.calculateBottomPadding())
                        .padding(16.dp)
                        .background(MaterialTheme.colorScheme.background),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = chatMessage,
                        onValueChange = { chatMessage = it },
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        placeholder = { Text(cnt[7]) },
                        modifier = Modifier.weight(1f).focusRequester(focusRequester)
                    )
                    AnimatedVisibility(
                        visible = chatMessage.text.trim().isNotBlank(),
                    ) {
                        Button(
                            onClick = {
                                if (isUpdate) {
                                    scope.launch {
                                        apiViewModel.updateMessage(currInd, chatMessage.text,friendId)
                                        isUpdate = false
                                        chatMessage = TextFieldValue("")
                                    }
                                } else {
                                    scope.launch {
                                        apiViewModel.sendMessage(
                                            Message(content = chatMessage.text),
                                            friendId
                                        )
                                        chatMessage = TextFieldValue("")
                                    }
                                }
                                focusManager.clearFocus()
                            },
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier.size((windowInfo.containerSize.width / 24).dp)
                        ) {
                            Icon(
                                imageVector = if(isUpdate) Icons.Default.Edit else Icons.AutoMirrored.Rounded.Send,
                                contentDescription = if(isUpdate) "update" else "send"
                            )
                        }
                    }
                }
            }
            if(showPopUp){
                Popup(
                    alignment = Alignment.TopStart,
                    onDismissRequest = { showPopUp = false },
                    offset = IntOffset(
                        pressOffset.x.roundToInt(),
                        pressOffset.y.roundToInt()
                    )
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer),
                        modifier = Modifier.width(135.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            if(chatState is ApiState.Success) {
                                val rawMessages = (chatState as ApiState.Success).data as List<Message>
                                if (rawMessages.find { it.id == currInd }?.senderId != friendId) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            isUpdate = true
                                            focusRequester.requestFocus()
                                            chatMessage =
                                                chatMessage.copy(
                                                    text = rawMessages.find { it.id == currInd }!!.content,
                                                    TextRange(rawMessages.find { it.id == currInd }!!.content.length)
                                                )
                                            showPopUp = false
                                        },
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Edit,
                                            contentDescription = cnt[8],
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = cnt[8],
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.W500,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    }
                                    HorizontalDivider()
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    apiViewModel.deleteMessage(currInd,friendId)
                                    showPopUp = false
                                },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = cnt[9],
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = cnt[9],
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.W500,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun groupingMessages(data: List<Message>): Map<String,List<Message>> {
    val resultMap: MutableMap<String,List<Message>> = mutableMapOf()
    data.forEach { f ->
        val it = LocalDateTime.ofInstant(Instant.ofEpochMilli(f.createdAt), ZoneId.systemDefault())
        when {
            it.dayOfMonth == OffsetDateTime.now().dayOfMonth -> {
                val list = resultMap["Today"]?.toMutableList()
                list?.add(f)
                resultMap["Today"] = list?.toList() ?: listOf(f)
            }
            it.dayOfMonth == OffsetDateTime.now().minusDays(1).dayOfMonth -> {
                val list = resultMap["Yesterday"]?.toMutableList()
                list?.add(f)
                resultMap["Yesterday"] = list?.toList() ?: listOf(f)
            }
            it.dayOfWeek.ordinal > 1 && it.dayOfMonth < OffsetDateTime.now().minusDays(1).dayOfMonth -> {
                val list = resultMap[it.dayOfWeek.name.lowercase()]?.toMutableList()
                list?.add(f)
                resultMap[it.dayOfWeek.name.lowercase()] = list?.toList() ?: listOf(f)
            }
            it.year == OffsetDateTime.now().year -> {
                val list = resultMap["${it.dayOfMonth} ${it.month.name.lowercase()}"]?.toMutableList()
                list?.add(f)
                resultMap["${it.dayOfMonth} ${it.month.name.lowercase()}"] = list?.toList() ?: listOf(f)
            }
            else -> {
                val list = resultMap["${it.year} ${it.dayOfMonth} ${it.month.name.lowercase()}"]?.toMutableList()
                list?.add(f)
                resultMap["${it.year} ${it.dayOfMonth} ${it.month.name.lowercase()}"] = list?.toList() ?: listOf(f)
            }
        }
    }
    return resultMap.toMap()
}
