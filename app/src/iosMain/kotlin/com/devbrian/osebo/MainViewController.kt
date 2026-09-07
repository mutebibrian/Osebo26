package com.devbrian.osebo

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * Entry point for the iOS app shell (Xcode project, not yet part of this
 * repo) to obtain the shared Compose UI as a UIViewController.
 */
fun MainViewController(): UIViewController = ComposeUIViewController { App() }
