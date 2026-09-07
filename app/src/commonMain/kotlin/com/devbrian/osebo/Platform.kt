package com.devbrian.osebo

/**
 * First expect/actual pair for the multiplatform migration — proves the
 * commonMain/androidMain/iosMain wiring is real. Business logic moves into
 * commonMain incrementally behind expect/actual boundaries like this one.
 */
expect class Platform() {
    val name: String
}

expect fun getPlatform(): Platform
