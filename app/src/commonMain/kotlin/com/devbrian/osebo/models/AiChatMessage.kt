package com.devbrian.osebo.models

import kotlinx.datetime.Clock
import kotlin.random.Random

data class AiChatMessage(
    val id: String = "${Clock.System.now().toEpochMilliseconds()}-${Random.nextInt()}",
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
)
