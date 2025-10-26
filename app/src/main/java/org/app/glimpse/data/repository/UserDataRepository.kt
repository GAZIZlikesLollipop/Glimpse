package org.app.glimpse.data.repository

import android.content.Context
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import org.app.glimpse.FriendUser
import org.app.glimpse.Message
import org.app.glimpse.UserData
import org.app.glimpse.data.UserDataSerializer
import org.app.glimpse.data.network.User
import kotlin.time.ExperimentalTime

val Context.userData by dataStore(
    "userData.proto",
    UserDataSerializer
)

interface UserDataRepo {
    val userData: Flow<UserData>
    suspend fun setUserDataNet(data: User)
    suspend fun setUserData(data: UserData)
}

class UserDataRepository(context: Context): UserDataRepo {
    val dataStore = context.userData

    override val userData: Flow<UserData> = dataStore.data

    @OptIn(ExperimentalTime::class)
    override suspend fun setUserDataNet(data: User) {
        val friends = mutableListOf<FriendUser>()
        for(friend in data.friends) {
            val friendsFriend = mutableListOf<FriendUser>()
            for(friendFriend in friend.friends!!){
                val data = friendFriend
                friendsFriend.add(
                    FriendUser.newBuilder()
                        .setId(data.id)
                        .setName(data.name)
                        .setBio(data.bio)
                        .setAvatar(data.avatar)
                        .setLatitude(data.latitude)
                        .setLongitude(data.longitude)
                        .setCreatedAt(data.createdAt)
                        .setUpdatedAt(data.updatedAt)
                        .build()
                )
            }
            val data = friend
            friends.add(
                FriendUser.newBuilder()
                    .setId(data.id)
                    .setName(data.name)
                    .setBio(data.bio)
                    .setAvatar(data.avatar)
                    .setLatitude(data.latitude)
                    .setLongitude(data.longitude)
                    .addAllFriends(friendsFriend)
                    .setCreatedAt(data.createdAt)
                    .setUpdatedAt(data.updatedAt)
                    .build()
            )
        }
        val receivedMessages = mutableListOf<Message>()
        for(msg in data.receivedMessages){
            receivedMessages.add(
                Message.newBuilder()
                    .setId(msg.id)
                    .setContent(msg.content)
                    .setSenderId(msg.senderId ?: 0)
                    .setReceivedId(msg.receiverId ?: 0)
                    .setCreatedAt(msg.createdAt)
                    .setUpdatedAt(msg.updatedAt)
                    .build()
            )
        }
        val sentMessages = mutableListOf<Message>()
        for(msg in data.sentMessages){
            sentMessages.add(
                Message.newBuilder()
                    .setId(msg.id)
                    .setContent(msg.content)
                    .setSenderId(msg.senderId ?: 0)
                    .setReceivedId(msg.receiverId ?: 0)
                    .setCreatedAt(msg.createdAt)
                    .setUpdatedAt(msg.updatedAt)
                    .build()
            )
        }
        dataStore.updateData {
            UserData.newBuilder()
                .setId(data.id)
                .setName(data.name)
                .setPassword(data.password)
                .setBio(data.bio)
                .setAvatar(data.avatar)
                .setLatitude(data.latitude)
                .setLongitude(data.longitude)
                .addAllFriends(friends)
                .addAllSentMessages(sentMessages)
                .addAllReceivedMessages(receivedMessages)
                .setCreatedAt(data.createdAt)
                .setUpdatedAt(data.updatedAt)
                .build()
        }
    }

    override suspend fun setUserData(data: UserData) { dataStore.updateData { data } }

}