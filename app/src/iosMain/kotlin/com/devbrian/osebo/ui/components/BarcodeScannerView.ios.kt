package com.devbrian.osebo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
// Wildcarded rather than named: requestAccessForMediaType is a
// completion-handler-based ObjC class method, and Kotlin/Native generates
// these as package-level extension functions under a synthetic name that
// isn't reliably guessable without compiling against the real SDK headers.
import platform.AVFoundation.*
import platform.CoreGraphics.CGRectZero
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * Real iOS camera-based barcode scanner (AVFoundation), since there is no
 * shared abstraction to port from Android's MLKit-based scanner (see
 * BarcodeScannerView.kt). Requires NSCameraUsageDescription in Info.plist
 * (set via iosApp/project.yml) — without it the app crashes on first
 * camera access.
 *
 * Unverified against a real build: written against JetBrains' documented
 * UIKitView + AVFoundation interop patterns, but this sandbox has no Xcode
 * to compile it. If Archive fails here, send the exact compiler error.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun BarcodeScannerView(onResult: (String) -> Unit, onCancel: () -> Unit) {
    var hasScanned by remember { mutableStateOf(false) }
    // Was previously just onCancel() on denial/setup failure, which closed this dialog
    // instantly with zero feedback — indistinguishable from the scan button doing
    // nothing at all. Surfacing a real message instead so a denied permission is
    // visible and actionable rather than looking like a dead feature.
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val session = remember { AVCaptureSession() }
    val previewLayer = remember { AVCaptureVideoPreviewLayer(session = session) }

    val delegate = remember {
        object : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {
            override fun captureOutput(
                output: AVCaptureOutput,
                didOutputMetadataObjects: List<*>,
                fromConnection: AVCaptureConnection,
            ) {
                if (hasScanned) return
                val code = didOutputMetadataObjects
                    .filterIsInstance<AVMetadataMachineReadableCodeObject>()
                    .firstOrNull()
                    ?.stringValue
                    ?: return
                hasScanned = true
                session.stopRunning()
                onResult(code)
            }
        }
    }

    DisposableEffect(Unit) {
        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
            dispatch_async(dispatch_get_main_queue()) {
                if (!granted) {
                    errorMessage = "Camera access is off for Osebo. " +
                        "Enable it in Settings → Osebo → Camera to scan barcodes."
                    return@dispatch_async
                }
                val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
                val input = device?.let { AVCaptureDeviceInput.deviceInputWithDevice(it, null) }
                if (device == null || input == null) {
                    errorMessage = "No camera is available on this device."
                    return@dispatch_async
                }
                if (session.canAddInput(input)) session.addInput(input)

                val metadataOutput = AVCaptureMetadataOutput()
                if (session.canAddOutput(metadataOutput)) {
                    session.addOutput(metadataOutput)
                    metadataOutput.setMetadataObjectsDelegate(delegate, queue = dispatch_get_main_queue())
                    metadataOutput.metadataObjectTypes = listOf(
                        AVMetadataObjectTypeEAN13Code,
                        AVMetadataObjectTypeEAN8Code,
                        AVMetadataObjectTypeUPCECode,
                        AVMetadataObjectTypeCode128Code,
                        AVMetadataObjectTypeCode39Code,
                        AVMetadataObjectTypeQRCode,
                    )
                }
                session.startRunning()
            }
        }
        onDispose { session.stopRunning() }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        UIKitView(
            factory = {
                object : UIView(frame = CGRectZero.readValue()) {
                    override fun layoutSubviews() {
                        super.layoutSubviews()
                        CATransaction.begin()
                        CATransaction.setValue(true, kCATransactionDisableActions)
                        previewLayer.setFrame(bounds)
                        CATransaction.commit()
                    }
                }.apply {
                    layer.addSublayer(previewLayer)
                    previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
                }
            },
            modifier = Modifier.fillMaxSize(),
            onRelease = { session.stopRunning() },
        )

        IconButton(
            onClick = onCancel,
            modifier = Modifier.align(Alignment.TopEnd).padding(20.dp),
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }

        Text(
            text = errorMessage ?: "Point the camera at a barcode",
            color = Color.White,
            modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 32.dp, vertical = 48.dp),
        )
    }
}
