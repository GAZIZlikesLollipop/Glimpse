@file:OptIn(ExperimentalTime::class)

package org.app.glimpse.data.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.io.ByteArrayOutputStream
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
data class Message(
    val id: Long = 0,
    val content: String,
    val isChecked: Boolean = false,
    val senderId: Long? = null,
    val receivedId: Long? = null,
    @SerialName("created_at")val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    @SerialName("updated_at")val updatedAt: Long = Clock.System.now().toEpochMilliseconds()
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
    @SerialName("created_at")val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    @SerialName("updated_at")val updatedAt: Long = Clock.System.now().toEpochMilliseconds()
)

@Serializable
data class FriendUser(
    val id: Long,
    val name: String,
    val avatar: String,
    val bio: String,
    val latitude: Double,
    val longitude: Double,
    val lastOnline: Long = Clock.System.now().toEpochMilliseconds(),
    val friends: List<FriendUser>? = null,
    @SerialName("created_at") val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    @SerialName("updated_at") val updatedAt: Long = Clock.System.now().toEpochMilliseconds()
)

@Serializable
data class Users(
    val id: Long,
    val name: String,
    val avatar: String,
    val latitude: Double,
    val longitude: Double
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

@Serializable
data class UpdateUser(
    val name: String? = null,
    val password: String? = null,
    val bio: String? = null,
    @Serializable(BitmapSerialize::class) val avatar: Bitmap? = null,
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

object BitmapSerialize: KSerializer<Bitmap> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Bitmap", PrimitiveKind.STRING)
    override fun serialize(
        encoder: Encoder,
        value: Bitmap
    ) {
        val baos = ByteArrayOutputStream()
        value.compress(Bitmap.CompressFormat.PNG,100,baos)
        val byteArray = baos.toByteArray()
        encoder.encodeString(Base64.encodeToString(byteArray, Base64.DEFAULT))
    }

    override fun deserialize(decoder: Decoder): Bitmap {
        val decodedBytes = Base64.decode(decoder.decodeString(), Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes,0,decodedBytes.size)
    }
}