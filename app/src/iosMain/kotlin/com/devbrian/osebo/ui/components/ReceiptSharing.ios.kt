package com.devbrian.osebo.ui.components

import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIPrintInfo
import platform.UIKit.UIPrintInfoOutputGeneral
import platform.UIKit.UIPrintInteractionController
import platform.UIKit.UISimpleTextPrintFormatter
import platform.UIKit.UIViewController

/**
 * Unverified against a real build, same caveat as BarcodeScannerView.ios.kt:
 * written against documented UIKit interop, no Xcode here to compile it.
 */
@OptIn(ExperimentalForeignApi::class)
private fun topViewController(): UIViewController? {
    var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}

@OptIn(ExperimentalForeignApi::class)
actual fun shareReceiptText(text: String, title: String) {
    val activityController = UIActivityViewController(
        activityItems = listOf(text),
        applicationActivities = null,
    )
    topViewController()?.presentViewController(activityController, animated = true, completion = null)
}

@OptIn(ExperimentalForeignApi::class)
actual fun printReceiptText(text: String, title: String) {
    val printController = UIPrintInteractionController.sharedPrintController ?: return
    val printInfo = UIPrintInfo.printInfo()
    printInfo.outputType = UIPrintInfoOutputGeneral
    printInfo.jobName = title
    printController.printInfo = printInfo
    printController.printFormatter = UISimpleTextPrintFormatter(text = text)
    printController.presentAnimated(true, completionHandler = null)
}
