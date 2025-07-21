package org.app.glimpse.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class Message(
    val id: Long,
    val content: String,
    val owner: User,
    val createdDate: OffsetDateTime,
    val updateDate: OffsetDateTime
)

@Serializable
data class FriendData(
    val id: Long,
    val userName: String,
    val avatar: String,
    val city: String,
    val friends: List<FriendData>,
    @SerialName("created_at") val createdAt: OffsetDateTime
)

@Serializable
data class FriendUser(
    val userName: String,
    val avatar: String,
    val latitude: Double,
    val longitude: Double,
    val lastOnline: OffsetDateTime,
    val friends: List<FriendData>,
    @SerialName("created_at") val createdAt: OffsetDateTime,
    @SerialName("updated_at") val updatedAt: OffsetDateTime
)

@Serializable
data class Friend(
    val id: Long,
    @SerialName("user_id") val userId: Long,
    val messages: List<Message>,
    val data: FriendUser
)

@Serializable
data class User(
    val id: Long,
    val name: String,
    val password: String,
    val avatar: String,
    val latitude: Double,
    val longitude: Double,
    val lastOnline: OffsetDateTime,
    val friends: List<Friend>,
    @SerialName("created_at") val createdAt: OffsetDateTime,
    @SerialName("updated_at") val updatedAt: OffsetDateTime

)
