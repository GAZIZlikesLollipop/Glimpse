package org.app.glimpse.pressentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.valentinilk.shimmer.shimmer
import org.app.glimpse.R

@Composable
fun ChatCard(
    friend: org.app.glimpse.FriendUser,
    onLocation: () -> Unit,
    onChat: () -> Unit,
    onProfile: () -> Unit
){
    val windowInfo = LocalWindowInfo.current
    val width = windowInfo.containerSize.width.dp
    val context = LocalContext.current
    Row(
        modifier = Modifier.width(width),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        SubcomposeAsyncImage(
            model = friend.avatar,
            contentDescription = "",
            imageLoader = ImageLoader.Builder(context)
                .components { add(OkHttpNetworkFetcherFactory( createUnsafeOkHttpClient())) }
                .build(),
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .size((windowInfo.containerSize.width / 18).dp)
                .clip(RoundedCornerShape(25.dp))
                .clickable { onProfile() },
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