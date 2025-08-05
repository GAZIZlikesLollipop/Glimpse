package org.app.glimpse.data.network

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: Long,
    val content: String,
    val isChecked: Boolean = false,
    val senderId: Long? = null,
    val receivedId: Long? = null,
    @SerialName("created_at") val createdAt: LocalDateTime,
    @SerialName("updated_at") val updatedAt: LocalDateTime
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
    val lastOnline: LocalDateTime,
    val friends: List<FriendUser>,
    val sentMessages: List<Message>,
    val receivedMessages: List<Message>,
    @SerialName("created_at") val createdAt: LocalDateTime,
    @SerialName("updated_at") val updatedAt: LocalDateTime
)

@Serializable
data class ResponseUser(
    val id: Long,
    val name: String,
    val bio: String,
    val avatar: String,
    val latitude: Double,
    val longitude: Double,
    val friends: List<FriendData>,
    @SerialName("created_at") val createdAt: LocalDateTime,
    @SerialName("updated_at") val updatedAt: LocalDateTime
)

@Serializable
data class FriendData(
    val id: Long,
    val name: String,
    val avatar: String,
    val bio: String,
    val longitude: Double,
    val latitude: Double,
    val friends: List<FriendData>,
    @SerialName("created_at") val createdAt: LocalDateTime,
    @SerialName("updated_at") val updatedAt: LocalDateTime
)

@Serializable
data class FriendUser(
    val id: Long,
    val name: String,
    val avatar: String,
    val bio: String,
    val latitude: Double,
    val longitude: Double,
    val lastOnline: LocalDateTime,
    val friends: List<FriendData>,
    @SerialName("created_at") val createdAt: LocalDateTime,
    @SerialName("updated_at") val updatedAt: LocalDateTime
)

@Serializable
data class GeocoderResponse(
    val name: String,
    @SerialName("display_name")
    val displayName: String,
    val boundingbox: List<Double>
)