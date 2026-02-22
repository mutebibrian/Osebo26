package com.devbrian.osebo.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.devbrian.osebo.models.CartItem
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PrintUtils {

    /**
     * Generate a PDF receipt
     */
    suspend fun generateReceiptPdf(
        context: Context,
        receiptNumber: String,
        date: String,
        customerName: String,
        items: List<CartItem>,
        subtotal: String,
        discount: String,
        tax: String,
        total: String,
        paid: String,
        change: String,
        paymentMethod: String
    ): File {
        // Create a new PDF document
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Create paints
        val titlePaint = Paint().apply {
            textSize = 20f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        val headerPaint = Paint().apply {
            textSize = 14f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        val normalPaint = Paint().apply {
            textSize = 12f
            textAlign = Paint.Align.LEFT
        }

        val boldPaint = Paint().apply {
            textSize = 12f
            isFakeBoldText = true
            textAlign = Paint.Align.LEFT
        }

        val rightPaint = Paint().apply {
            textSize = 12f
            textAlign = Paint.Align.RIGHT
        }

        val smallPaint = Paint().apply {
            textSize = 10f
            textAlign = Paint.Align.CENTER
        }

        var yPosition = 40f

        // Store Name
        canvas.drawText("OSEBO STORE", 150f, yPosition, titlePaint)
        yPosition += 25f

        // Store Address
        canvas.drawText("Kampala, Uganda", 150f, yPosition, headerPaint)
        yPosition += 20f
        canvas.drawText("Tel: +256 700 123456", 150f, yPosition, smallPaint)
        yPosition += 30f

        // Receipt Info
        canvas.drawText("Receipt No: $receiptNumber", 30f, yPosition, normalPaint)
        yPosition += 18f
        canvas.drawText("Date: $date", 30f, yPosition, normalPaint)
        yPosition += 18f
        canvas.drawText("Customer: $customerName", 30f, yPosition, normalPaint)
        yPosition += 25f

        // Divider
        canvas.drawLine(30f, yPosition, 270f, yPosition, Paint().apply { strokeWidth = 2f })
        yPosition += 20f

        // Items Header
        canvas.drawText("Item", 30f, yPosition, boldPaint)
        canvas.drawText("Qty", 180f, yPosition, boldPaint)
        canvas.drawText("Price", 250f, yPosition, rightPaint)
        yPosition += 18f

        // Items
        if (items.isEmpty()) {
            canvas.drawText("No items", 150f, yPosition, normalPaint)
            yPosition += 20f
        } else {
            items.forEach { item ->
                val itemTotal = item.unitPrice * item.quantity * (1 - item.discount / 100)

                // Item name (truncate if too long)
                var itemName = item.product.name
                if (itemName.length > 15) {
                    itemName = itemName.substring(0, 12) + "..."
                }

                canvas.drawText(itemName, 30f, yPosition, normalPaint)
                canvas.drawText(item.quantity.toString(), 180f, yPosition, normalPaint)
                canvas.drawText(CurrencyFormatter.formatFull(itemTotal), 250f, yPosition, rightPaint)
                yPosition += 18f

                // Show discount if applicable
                if (item.discount > 0) {
                    canvas.drawText("  (${item.discount}% off)", 40f, yPosition, smallPaint)
                    yPosition += 15f
                }
            }
        }

        yPosition += 10f

        // Divider
        canvas.drawLine(30f, yPosition, 270f, yPosition, Paint().apply { strokeWidth = 2f })
        yPosition += 20f

        // Totals
        canvas.drawText("Subtotal:", 30f, yPosition, normalPaint)
        canvas.drawText(subtotal, 250f, yPosition, rightPaint)
        yPosition += 18f

        canvas.drawText("Discount:", 30f, yPosition, normalPaint)
        canvas.drawText(discount, 250f, yPosition, rightPaint)
        yPosition += 18f

        canvas.drawText("Tax:", 30f, yPosition, normalPaint)
        canvas.drawText(tax, 250f, yPosition, rightPaint)
        yPosition += 18f

        // Total (bold)
        canvas.drawText("TOTAL:", 30f, yPosition, boldPaint)
        canvas.drawText(total, 250f, yPosition, boldPaint)
        yPosition += 25f

        canvas.drawText("Paid:", 30f, yPosition, normalPaint)
        canvas.drawText(paid, 250f, yPosition, rightPaint)
        yPosition += 18f

        canvas.drawText("Change:", 30f, yPosition, normalPaint)
        canvas.drawText(change, 250f, yPosition, rightPaint)
        yPosition += 18f

        canvas.drawText("Payment: $paymentMethod", 30f, yPosition, normalPaint)
        yPosition += 25f

        // Footer
        canvas.drawText("Thank you for your purchase!", 150f, yPosition, smallPaint)
        yPosition += 15f
        canvas.drawText("Visit us again!", 150f, yPosition, smallPaint)

        pdfDocument.finishPage(page)

        // Save PDF to external storage
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "receipt_${receiptNumber}_$timestamp.pdf"

        val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(documentsDir, fileName)

        try {
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }

        return file
    }

    /**
     * Generate a simple text receipt for sharing
     */
    fun generateTextReceipt(
        receiptNumber: String,
        date: String,
        customerName: String,
        items: List<CartItem>,
        subtotal: String,
        discount: String,
        tax: String,
        total: String,
        paid: String,
        change: String,
        paymentMethod: String
    ): String {
        val sb = StringBuilder()
        sb.appendLine("=".repeat(40))
        sb.appendLine("           OSEBO STORE")
        sb.appendLine("=".repeat(40))
        sb.appendLine()
        sb.appendLine("Receipt No: $receiptNumber")
        sb.appendLine("Date: $date")
        sb.appendLine("Customer: $customerName")
        sb.appendLine()
        sb.appendLine("-".repeat(40))
        sb.appendLine(String.format("%-20s %3s %10s", "Item", "Qty", "Price"))
        sb.appendLine("-".repeat(40))

        items.forEach { item ->
            val itemTotal = item.unitPrice * item.quantity * (1 - item.discount / 100)
            var itemName = item.product.name
            if (itemName.length > 15) {
                itemName = itemName.substring(0, 12) + "..."
            }
            sb.appendLine(String.format("%-20s %3d %10s",
                itemName,
                item.quantity,
                CurrencyFormatter.formatFull(itemTotal)
            ))
            if (item.discount > 0) {
                sb.appendLine(String.format("  (%d%% off)", item.discount.toInt()))
            }
        }

        sb.appendLine("-".repeat(40))
        sb.appendLine(String.format("%-30s %10s", "Subtotal:", subtotal))
        sb.appendLine(String.format("%-30s %10s", "Discount:", discount))
        sb.appendLine(String.format("%-30s %10s", "Tax:", tax))
        sb.appendLine("=".repeat(40))
        sb.appendLine(String.format("%-30s %10s", "TOTAL:", total))
        sb.appendLine("-".repeat(40))
        sb.appendLine(String.format("%-30s %10s", "Paid:", paid))
        sb.appendLine(String.format("%-30s %10s", "Change:", change))
        sb.appendLine(String.format("%-30s %10s", "Payment:", paymentMethod))
        sb.appendLine()
        sb.appendLine("=".repeat(40))
        sb.appendLine("     Thank you for your purchase!")
        sb.appendLine("         Visit us again!")
        sb.appendLine("=".repeat(40))

        return sb.toString()
    }

    /**
     * Share receipt via intent
     */
    fun shareReceipt(context: Context, file: File): Boolean {
        return try {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(android.content.Intent.EXTRA_SUBJECT, "Receipt")
                putExtra(android.content.Intent.EXTRA_TEXT, "Please find attached receipt")
            }

            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Receipt"))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}