package org.app.glimpse.pressentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.app.glimpse.R
import org.app.glimpse.data.network.ApiViewModel
import org.app.glimpse.data.network.Users

@Composable
fun FriendAdd(
    apiViewModel: ApiViewModel,
    onBack: () -> Unit
){
    var searchText by rememberSaveable { mutableStateOf("") }
    val searched = remember { mutableStateListOf<Users>() }
    val cnt = stringArrayResource(R.array.main_cnt)
    val windowInfo = LocalWindowInfo.current
    LaunchedEffect(Unit) {
        apiViewModel.getUserNames()
    }
    LaunchedEffect(searchText) {
        apiViewModel.userNames.forEach {
            if(searchText.lowercase().trim().contains(it.name.lowercase().trim())){
                searched.add(it)
            }
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically
        ){
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { onBack() },
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                    MaterialTheme.colorScheme.onBackground
                ),
//                modifier = Modifier.size(24.dp)
                modifier = Modifier.size((windowInfo.containerSize.width / 26).dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Spacer(Modifier.width(8.dp))
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "search",
                        modifier = Modifier.size(24.dp)
                    )
                },
                placeholder = {
                    Text(
                        text = cnt[2]
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    errorBorderColor = Color.Transparent
                ),
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .offset(x = (-4).dp)
                    .border(
                    2.dp,
                    MaterialTheme.colorScheme.onBackground,
                    RoundedCornerShape(36.dp)
                )
            )
        }
        if(searched.isNotEmpty()) {
            LazyColumn {
                items(searched) {

                }
            }
        } else {
            Text(
                text = cnt[3],
                fontSize = 36.sp,
                fontWeight = FontWeight.W600,
                modifier = Modifier.fillMaxSize().offset(y = (windowInfo.containerSize.height/12).dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(0.5f)
            )
        }
    }
}