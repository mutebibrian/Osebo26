package com.devbrian.osebo.ui.components

import androidx.compose.runtime.Composable

/**
 * Full-screen barcode scanner used by New Sale's scan button.
 *
 * Android's real app never calls this: its New Sale flow is a legacy
 * Fragment (NewSaleFragment) that launches its own MLKit-based
 * BarcodeScannerFragment directly, entirely outside the commonMain Compose
 * screens iOS uses — there is no shared abstraction to port. This is a
 * from-scratch iOS implementation (AVFoundation) so New Sale's scan button
 * does something real on iOS; the androidMain actual is an unreachable
 * placeholder kept only so the shared NewSaleScreen compiles for Android too.
 */
@Composable
expect fun BarcodeScannerView(onResult: (String) -> Unit, onCancel: () -> Unit)
