package com.devbrian.osebo.ui.components

/**
 * Unreachable in the real Android app (see ReceiptSharing.kt and
 * BarcodeScannerView.android.kt) — kept only so the shared ReceiptScreen
 * compiles for the Android target too. Android's real share/print lives in
 * ReceiptFragment (PDF generation + Bluetooth thermal printer).
 */
actual fun shareReceiptText(text: String, title: String) = Unit

actual fun printReceiptText(text: String, title: String) = Unit
