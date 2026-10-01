package com.devbrian.osebo

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * Entry point for the iOS app shell (Xcode project, not yet part of this
 * repo) to obtain the shared Compose UI as a UIViewController.
 *
 * enforceStrictPlistSanityCheck disabled: Compose Multiplatform wants
 * CADisableMinimumFrameDurationOnPhone=true in Info.plist for ProMotion
 * performance, but the Xcode-side custom property didn't take effect
 * reliably. Revisit once the Xcode project is stable — for now this
 * only disables a dev-time warning, not actual rendering.
 */
fun MainViewController(): UIViewController = ComposeUIViewController(
    configure = { enforceStrictPlistSanityCheck = false }
) { App() }
