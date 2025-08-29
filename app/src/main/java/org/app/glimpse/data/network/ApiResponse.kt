@file:OptIn(ExperimentalTime::class)

package org.app.glimpse.data.network

import android.graphics.Bitmap
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class Message(
    val id: Long,
    val content: String,
    val isChecked: Boolean = false,
    val senderId: Long? = null,
    val receivedId: Long? = null,
    @SerialName("created_at") @Serializable(InstantSerialize::class) val createdAt: Instant = Clock.System.now(),
    @SerialName("updated_at") @Serializable(InstantSerialize::class) val updatedAt: Instant = Clock.System.now()
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
    val friends: List<FriendUser>,
    val sentMessages: List<Message>,
    val receivedMessages: List<Message>,
    @SerialName("created_at") @Serializable(InstantSerialize::class) val createdAt: Instant = Clock.System.now(),
    @SerialName("updated_at") @Serializable(InstantSerialize::class) val updatedAt: Instant = Clock.System.now()
)

@Serializable
data class FriendData(
    val id: Long,
    val name: String,
    val avatar: String,
    val bio: String,
    val longitude: Double,
    val latitude: Double,
    val friends: List<FriendData>? = null,
    @SerialName("created_at") @Serializable(InstantSerialize::class) val createdAt: Instant = Clock.System.now(),
    @SerialName("updated_at") @Serializable(InstantSerialize::class) val updatedAt: Instant = Clock.System.now()
)

@Serializable
data class FriendUser(
    val id: Long,
    val name: String,
    val avatar: String,
    val bio: String,
    val latitude: Double,
    val longitude: Double,
    @Serializable(InstantSerialize::class) val lastOnline: Instant,
    val friends: List<FriendData>? = null,
    @SerialName("created_at") @Serializable(InstantSerialize::class) val createdAt: Instant = Clock.System.now(),
    @SerialName("updated_at") @Serializable(InstantSerialize::class) val updatedAt: Instant = Clock.System.now()
)

@Serializable
data class GeocoderResponse(
    val name: String,
    @SerialName("display_name")
    val displayName: String,
    val boundingbox: List<Double>
)

data class SignUpUser(
    val userName: String,
    val password: String,
    val bio: String,
    val avatar: Bitmap?,
    val avatarExt: String,
    val latitude: Double,
    val longitude: Double
)

data class UpdateUser(
    val name: String,
    val password: String? = null,
    val bio: String? = null,
    val avatar: Bitmap? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val friends: List<FriendUser>? = null,
    val sentMessages: List<Message>? = null,
    val receivedMessages: List<Message>? = null,
    val avatarExt: String? = null
)

@Serializable
data class AuthRequest(
    val user_name: String,
    val password: String
)

object InstantSerialize: KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("MyCustomType", PrimitiveKind.STRING)
    override fun serialize(
        encoder: Encoder,
        value: Instant
    ) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }
}