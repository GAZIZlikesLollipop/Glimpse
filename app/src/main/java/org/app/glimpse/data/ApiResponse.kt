package org.app.glimpse.data

import java.time.OffsetDateTime

data class Message(
    val id: Long,
    val content: String,
    val owner: User,
    val createdDate: OffsetDateTime
)

data class Chat(
    val id: Long,
    val messages: List<Message>
)

data class UserFriend(
    val data: User,
    val chat: Chat,
)

data class User(
    val id: Long,
    val name: String,
    val avatar: String,
    val latitude: Double,
    val longitude: Double,
    val lastOnline: OffsetDateTime,
    val friends: List<UserFriend>,
)