package com.devbrian.osebo.ui.components

/**
 * Platform hook for Receipt's Share/Print buttons, previously wired to
 * literal no-op lambdas in App.kt. Plain text rather than a generated PDF:
 * Android's real ReceiptFragment generates a PDF (PrintUtils) and prints it
 * over Bluetooth to a thermal receipt printer (PrinterConnectionManager),
 * neither of which has a commonMain equivalent. Handing this text straight
 * to iOS's standard share sheet / AirPrint is the closest native iOS
 * equivalent to "share/print this receipt" without building PDF generation
 * or thermal-printer SDK integration from scratch.
 */
expect fun shareReceiptText(text: String, title: String)
expect fun printReceiptText(text: String, title: String)
