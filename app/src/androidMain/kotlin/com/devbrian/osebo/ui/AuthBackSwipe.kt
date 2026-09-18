package com.devbrian.osebo.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.unit.dp
import kotlin.math.abs

internal fun Modifier.authBackSwipe(onBack: () -> Unit): Modifier = composed {
    val currentOnBack by rememberUpdatedState(onBack)
    pointerInput(Unit) {
        val edgeWidth = 32.dp.toPx()
        val swipeDistance = 72.dp.toPx()
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            if (down.position.x > edgeWidth) return@awaitEachGesture
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val pointer = event.changes.firstOrNull { it.id == down.id } ?: break
                if (!pointer.pressed) {
                    val horizontal = pointer.position.x - down.position.x
                    val vertical = pointer.position.y - down.position.y
                    if (horizontal > swipeDistance && horizontal > abs(vertical) * 1.3f) {
                        currentOnBack()
                    }
                    break
                }
            }
        }
    }
}
