package org.app.glimpse.pressentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import java.time.Duration
import java.time.OffsetDateTime

@Composable
fun ChatScreen(
    friendId: Long,
    paddingValues: PaddingValues,
    navController: NavController
){
    val windowInfo = LocalWindowInfo.current
    val data = testFriends.find {it.id == friendId}
    val timeDiff = Duration.between(data?.data?.lastOnline, OffsetDateTime.now())
    val lastOnline = if(timeDiff.toSeconds() < 3){
        "online"
    }
        else {
        when {
            timeDiff.toSeconds() < 60 -> "${timeDiff.toSeconds()} seconds ago"
            timeDiff.toMinutes() < 60 -> "${timeDiff.toMinutes()} minutes ago"
            timeDiff.toHours() < 24 -> "${timeDiff.toHours()} hours ago"
            timeDiff.toDays() < 30 -> "${timeDiff.toDays()} days ago"
            timeDiff.toDays() < 365 -> "${timeDiff.toDays()/30.44} months ago"
            else -> "${timeDiff.toDays()/365} years ago"
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ){
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp).padding(paddingValues),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = data?.data?.avatar,
                contentDescription = "",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .size((windowInfo.containerSize.width / 20).dp)
                    .clip(RoundedCornerShape(20.dp))
            )
            Column {
                Text(
                    data?.data?.userName ?: "",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = lastOnline,
                    color = if(timeDiff.toSeconds() < 3)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(0.5f)
                )
            }
            Button(
                onClick = {navController.popBackStack()},
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(Color.Transparent, MaterialTheme.colorScheme.onBackground),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.onBackground.copy(0.8f)),
                modifier = Modifier.size((windowInfo.containerSize.width/30).dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = ""
                )
            }
        }

        LazyColumn {

        }

        Row {

        }
    }
}