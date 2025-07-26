package org.app.glimpse.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class Message(
    val id: Long,
    val content: String,
    val isChecked: Boolean = false,
    val senderId: Long? = null,
    val receivedId: Long? = null,
    @SerialName("created_at") val createdAt: OffsetDateTime,
    @SerialName("updated_at") val updatedAt: OffsetDateTime
)

@Serializable
data class User(
    val id: Long,
    val name: String,
    val password: String,
    val bio: String,
    val avatar: String,
    val latitude: Double,
    val longitude: Double,
    val lastOnline: OffsetDateTime,
    val friends: List<FriendUser>,
    val sentMessages: List<Message>,
    val receivedMessages: List<Message>,
    @SerialName("created_at") val createdAt: OffsetDateTime,
    @SerialName("updated_at") val updatedAt: OffsetDateTime
)

@Serializable
data class FriendData(
    val id: Long,
    val userName: String,
    val avatar: String,
    val bio: String,
    val city: String,
    val friends: List<FriendData>,
    @SerialName("created_at") val createdAt: OffsetDateTime,
    @SerialName("updated_at") val updatedAt: OffsetDateTime
)

@Serializable
data class FriendUser(
    val id: Long,
    val userName: String,
    val avatar: String,
    val bio: String,
    val latitude: Double,
    val longitude: Double,
    val lastOnline: OffsetDateTime,
    val friends: List<FriendData>,
    @SerialName("created_at") val createdAt: OffsetDateTime,
    @SerialName("updated_at") val updatedAt: OffsetDateTime
)