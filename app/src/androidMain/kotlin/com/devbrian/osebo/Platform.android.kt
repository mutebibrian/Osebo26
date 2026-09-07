package com.devbrian.osebo

import android.os.Build

actual class Platform actual constructor() {
    actual val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = Platform()
