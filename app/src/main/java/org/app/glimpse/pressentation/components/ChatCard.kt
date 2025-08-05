package org.app.glimpse.pressentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.app.glimpse.R
import org.app.glimpse.data.network.FriendUser

@Composable
fun ChatCard(
    friend: FriendUser,
    onLocation: () -> Unit,
    onChat: () -> Unit,
    onProfile: () -> Unit,
){
    val windowInfo = LocalWindowInfo.current
    val width = windowInfo.containerSize.width.dp
    Row(
        modifier = Modifier.width(width),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AsyncImage(
            model = friend.avatar,
            contentDescription = "",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .size((windowInfo.containerSize.width / 18).dp)
                .clip(RoundedCornerShape(25.dp))
                .clickable { onProfile() }
        )
        Text(
            text = friend.name,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            modifier = Modifier.weight(1f),
            overflow = TextOverflow.Ellipsis
        )
        Button(
            onClick = onLocation,
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                MaterialTheme.colorScheme.onBackground.copy(0.15f),
                MaterialTheme.colorScheme.onBackground
            )
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.my_location),
                contentDescription = "navigate to friend location"
            )
        }
        Button(
            onClick = onChat,
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(24.dp),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.chat),
                contentDescription = "navigate to chat screen"
            )
        }
    }
}