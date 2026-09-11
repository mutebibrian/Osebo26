package com.devbrian.osebo.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Corner radii matched to the reference mockup: soft rounded cards, fully
 * pill-shaped buttons/chips/search fields, and a large-radius floating
 * bottom bar.
 */
object OseboShapes {
    val Card = RoundedCornerShape(18.dp)
    val CardSmall = RoundedCornerShape(12.dp)
    val Pill = RoundedCornerShape(50)
    val Chip = RoundedCornerShape(20.dp)
    val BottomBar = RoundedCornerShape(28.dp)
    val Sheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
}

val OseboMaterialShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = OseboShapes.CardSmall,
    medium = OseboShapes.Card,
    large = RoundedCornerShape(24.dp),
    extraLarge = OseboShapes.BottomBar
)
