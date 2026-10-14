package com.devbrian.osebo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeCode128Code
import platform.AVFoundation.AVMetadataObjectTypeCode39Code
import platform.AVFoundation.AVMetadataObjectTypeEAN13Code
import platform.AVFoundation.AVMetadataObjectTypeEAN8Code
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AVFoundation.AVMetadataObjectTypeUPCECode
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
                    onCancel()
                    return@dispatch_async
                }
                val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
                val input = device?.let { AVCaptureDeviceInput.deviceInputWithDevice(it, null) }
                if (device == null || input == null) {
                    onCancel()
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
            text = "Point the camera at a barcode",
            color = Color.White,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp),
        )
    }
}
