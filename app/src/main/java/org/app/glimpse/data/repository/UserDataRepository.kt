package org.app.glimpse.data.repository

import android.content.Context
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import org.app.glimpse.FriendUser
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
                friendsFriend.add(
                    FriendUser.newBuilder()
                        .setId(friendFriend.id)
                        .setName(friendFriend.name)
                        .setBio(friendFriend.bio)
                        .setAvatar(friendFriend.avatar)
                        .setLatitude(friendFriend.latitude)
                        .setLongitude(friendFriend.longitude)
                        .setCreatedAt(friendFriend.createdAt)
                        .setUpdatedAt(friendFriend.updatedAt)
                        .build()
                )
            }
            friends.add(
                FriendUser.newBuilder()
                    .setId(friend.id)
                    .setName(friend.name)
                    .setBio(friend.bio)
                    .setAvatar(friend.avatar)
                    .setLatitude(friend.latitude)
                    .setLongitude(friend.longitude)
                    .addAllFriends(friendsFriend)
                    .setCreatedAt(friend.createdAt)
                    .setUpdatedAt(friend.updatedAt)
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
                .setCreatedAt(data.createdAt)
                .setUpdatedAt(data.updatedAt)
                .build()
        }
    }

    override suspend fun setUserData(data: UserData) { dataStore.updateData { data } }

}