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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import org.app.glimpse.R
import org.app.glimpse.data.ApiViewModel
import java.time.Duration
import java.time.OffsetDateTime

@Composable
fun ChatScreen(
    friendId: Long,
    paddingValues: PaddingValues,
    navController: NavController,
    apiViewModel: ApiViewModel
) {
    val messages =
        (apiViewModel.userData.sentMessages + apiViewModel.userData.receivedMessages).sortedBy { it.createdAt }
    val windowInfo = LocalWindowInfo.current
    val data = apiViewModel.userData.friends.find { it.id == friendId }
    val timeDiff = Duration.between(data?.lastOnline, OffsetDateTime.now())
    val lastOnline =
        if (timeDiff.toSeconds() < 3) {
            "online"
        } else {
            when {
                timeDiff.toSeconds() < 60 -> "${timeDiff.toSeconds()} seconds ago"
                timeDiff.toMinutes() < 60 -> "${timeDiff.toMinutes()} minutes ago"
                timeDiff.toHours() < 24 -> "${timeDiff.toHours()} hours ago"
                timeDiff.toDays() < 30 -> "${timeDiff.toDays()} days ago"
                timeDiff.toDays() < 365 -> "${timeDiff.toDays() / 30.44} months ago"
                else -> "${timeDiff.toDays() / 365} years ago"
            }
        }
    var chatMessage by rememberSaveable { mutableStateOf("") }
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
                contentDescription = "",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .size((windowInfo.containerSize.width / 22).dp)
                    .clip(RoundedCornerShape(20.dp))
            )
            Column {
                Text(
                    data?.userName ?: "",
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
                    contentDescription = ""
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
//                    when {
//                        it.createdAt.dayOfMonth == OffsetDateTime.now().dayOfMonth -> "Today"
//                        it.createdAt == OffsetDateTime.now().minusDays(1) -> "Yesterday"
//                        else -> "${it.createdAt.dayOfMonth} july"
//                    },
                    "Today",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.75f)
                )
            }
            items(messages) {
                if (it.senderId == apiViewModel.userData.id) {
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
                                        "${it.createdAt.hour}:${it.createdAt.minute}",
                                        color = MaterialTheme.colorScheme.onBackground.copy(0.5f),
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
                                    "${it.createdAt.hour}:${it.createdAt.minute}",
                                    color = MaterialTheme.colorScheme.onBackground.copy(0.5f),
                                    style = MaterialTheme.typography.labelLarge
                                )
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
                placeholder = {
                    Text(
                        "Enter a message..."
                    )
                }
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

//fun groupingMessages(data: List<Message>): Map<String,List<Message>> {
//   return emptyMap()
//}