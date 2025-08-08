@file:OptIn(ExperimentalTime::class)

package org.app.glimpse.pressentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.app.glimpse.R
import org.app.glimpse.data.network.ApiState
import org.app.glimpse.data.network.ApiViewModel
import org.app.glimpse.data.network.Message
import org.app.glimpse.data.network.User
import java.time.Duration
import java.time.LocalDateTime
import java.time.OffsetDateTime
import kotlin.collections.forEach
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun ChatScreen(
    friendId: Long,
    paddingValues: PaddingValues,
    navController: NavController,
    apiViewModel: ApiViewModel
) {
    val apiState by apiViewModel.userData.collectAsState()
    if(apiState is ApiState.Success) {
        val userData = (apiState as ApiState.Success).data as User
        val rawMessages = (userData.sentMessages.filter { it.receivedId == friendId } + userData.receivedMessages.filter { it.senderId == friendId }).sortedBy { it.createdAt }
        val windowInfo = LocalWindowInfo.current
        val data = userData.friends.find { it.id == friendId }
        val time = data?.lastOnline?.toLocalDateTime(TimeZone.currentSystemDefault())?.toJavaLocalDateTime()
        val timeDiff = Duration.between(time, LocalDateTime.now())
        val cnt = stringArrayResource(R.array.chat_cnt)
        val lastOnline =
            if (timeDiff.toSeconds() < 3) {
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
        var chatMessage by rememberSaveable { mutableStateOf("") }
        val messages = remember { mutableStateMapOf<String, List<Message>>() }
        LaunchedEffect(Unit) {
            messages.putAll(groupingMessages(rawMessages))
        }
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
                AsyncImage(
                    model = data?.avatar,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .size((windowInfo.containerSize.width / 22).dp)
                        .clip(RoundedCornerShape(20.dp))
                )
                Column {
                    Text(
                        data?.name ?: "",
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
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.onBackground.copy(0.8f)),
                    modifier = Modifier.size((windowInfo.containerSize.width / 30).dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Back"
                    )
                }
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(messages.toList()) { (date, messages) ->
                    Text(
                        date,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.75f)
                    )
                    messages.forEach {
                        if (it.senderId == userData.id) {
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
                                        Text(it.content)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                "${it.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).hour}:${it.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).minute}",
                                                color = MaterialTheme.colorScheme.onBackground.copy(
                                                    0.5f
                                                ),
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                            Icon(
                                                imageVector =
                                                    if (it.isChecked) ImageVector.vectorResource(R.drawable.done_all) else Icons.Rounded.Check,
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
                                        Text(it.content)
                                        Text(
                                            "${it.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).hour}:${it.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).minute}",
                                            color = MaterialTheme.colorScheme.onBackground.copy(0.5f),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
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
                    placeholder = { Text(cnt[7]) }
                )
                AnimatedVisibility(
                    visible = chatMessage.trim().isNotBlank(),
                ) {
                    Button(
                        onClick = { chatMessage = "" },
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier.size((windowInfo.containerSize.width / 24).dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "send"
                        )
                    }
                }
            }
        }
    }
}

fun groupingMessages(data: List<Message>): Map<String,List<Message>> {
    val resultMap: MutableMap<String,List<Message>> = mutableMapOf()
    data.forEach { f ->
        val it = f.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
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
                val list = resultMap[it.dayOfWeek.name]?.toMutableList()
                list?.add(f)
                resultMap[it.dayOfWeek.name] = list?.toList() ?: listOf(f)
            }
            it.year == OffsetDateTime.now().year -> {
                val list = resultMap["${it.dayOfMonth} ${it.month.name}"]?.toMutableList()
                list?.add(f)
                resultMap["${it.dayOfMonth} ${it.month.name}"] = list?.toList() ?: listOf(f)
            }
            else -> {
                val list = resultMap["${it.year} ${it.dayOfMonth} ${it.month.name}"]?.toMutableList()
                list?.add(f)
                resultMap["${it.year} ${it.dayOfMonth} ${it.month.name}"] = list?.toList() ?: listOf(f)
            }
        }
    }
    return resultMap.toMap()
}