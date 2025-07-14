package org.app.glimpse.data

import java.time.OffsetDateTime
import java.time.OffsetTime

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
data class User(
    val id: Long,
    val name: String,
    val avatar: String,
    val location: Double,
    val lastOnline: OffsetDateTime,
    val friends: List<User>,
    val chats: List<Chat>
)