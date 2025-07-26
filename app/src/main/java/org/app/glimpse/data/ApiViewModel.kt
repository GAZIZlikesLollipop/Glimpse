package org.app.glimpse.data

import androidx.lifecycle.ViewModel
import java.time.OffsetDateTime

class ApiViewModel: ViewModel() {
    val userData: User =
        User(
            id = 0,
            name = "Grinya",
            password = "12345678",
            bio = "Im very funny",
            avatar = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fmlpnk72yciwc.i.optimole.com%2FcqhiHLc.IIZS~2ef73%2Fw%3Aauto%2Fh%3Aauto%2Fq%3A75%2Fhttps%3A%2F%2Fbleedingcool.com%2Fwp-content%2Fuploads%2F2022%2F11%2FAVATAR_THE_WAY_OF_WATER_1SHT_DIGITAL_LOAK_sRGB_V1.jpg&f=1&nofb=1&ipt=ff723019f41bbd61e208e1e29de3472068d269714c642ff051893f954d0cf29e",
            latitude = 41.2167289259734,
            longitude = 69.33520401007092,
            lastOnline = OffsetDateTime.now().minusDays(1),
            friends = listOf(
                FriendUser(
                    id = 0,
                    userName = "Furiya",
                    avatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSmGjt5BrPeTNQMuCNHnIAPjzPzi-SJRKqqxA&s",
                    bio = "Im cuties and darkest dragon on the world",
                    latitude = 41.216728,
                    longitude = 69.335105,
                    lastOnline = OffsetDateTime.now(),
                    friends = emptyList(),
                    createdAt = OffsetDateTime.now().minusWeeks(1),
                    updatedAt = OffsetDateTime.now().minusDays(2)
                ),
                FriendUser(
                    id = 1,
                    userName = "D. Tramp",
                    avatar = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse1.mm.bing.net%2Fth%2Fid%2FOIP.-O8kGYxjDkwOldLaV9HL1AHaFW%3Fr%3D0%26pid%3DApi&f=1&ipt=a0dc84ded74f9869574f695c92874028aa9553d5d787c2d4865ba8fc72bbb04b&ipo=images",
                    bio = "Im very big boy for more than friendship)",
                    latitude = 41.216195,
                    longitude = 69.335341,
                    lastOnline = OffsetDateTime.now().minusHours(6),
                    friends = emptyList(),
                    createdAt = OffsetDateTime.now().minusYears(1),
                    updatedAt = OffsetDateTime.now().minusMonths(1)
                ),
                FriendUser(
                    id = 2,
                    userName = "Vanya2077",
                    bio = "Im very Funny boy",
                    avatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTP0KtXQ5ov5a9PoDEWxGpv1AL83o8As6y5cw&s",
                    latitude = 41.232697,
                    longitude = 69.335182,
                    lastOnline = OffsetDateTime.now(),
                    friends = emptyList(),
                    createdAt = OffsetDateTime.now().minusDays(3),
                    updatedAt = OffsetDateTime.now().minusDays(1)
                )
            ),
            sentMessages = listOf(
                Message(
                    id = 0,
                    content = "hello, how are you?",
                    senderId = 0,
                    isChecked = true,
                    createdAt = OffsetDateTime.now().minusMinutes(5),
                    updatedAt = OffsetDateTime.now()
                ),
                Message(
                    id = 2,
                    content = "im good!",
                    senderId = 0,
                    isChecked = true,
                    createdAt = OffsetDateTime.now().minusMinutes(3).minusSeconds(40),
                    updatedAt = OffsetDateTime.now()
                ),
                Message(
                    id = 1,
                    content = "bye!",
                    senderId = 0,
                    createdAt = OffsetDateTime.now().minusMinutes(1).minusSeconds(30),
                    updatedAt = OffsetDateTime.now()
                ),
            ),
            receivedMessages = listOf(
                Message(
                    id = 1,
                    content = "im fine, and you?",
                    senderId = 1,
                    createdAt = OffsetDateTime.now().minusMinutes(4),
                    updatedAt = OffsetDateTime.now()
                ),
                Message(
                    id = 1,
                    content = "ok, bye,bye",
                    senderId = 1,
                    createdAt = OffsetDateTime.now().minusMinutes(2).minusSeconds(50),
                    updatedAt = OffsetDateTime.now()
                ),
            ),
            createdAt = OffsetDateTime.now().minusMonths(1),
            updatedAt = OffsetDateTime.now().minusWeeks(2)
        )
}