package com.devbrian.osebo.utils

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.devbrian.osebo.models.FinancialStatement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FinancialStatementExportUtils {

    /**
     * Shows a PDF/Excel(CSV) format picker, generates the chosen file, and hands it to the
     * share sheet. One call site covers the dialog -> generate -> share flow used by every
     * screen that offers a "Export Financial Statement" action.
     */
    fun showExportDialog(
        fragment: Fragment,
        shopName: String,
        periodLabel: String,
        dateRangeLabel: String,
        statement: FinancialStatement
    ) {
        val context = fragment.requireContext()
        val formats = arrayOf("PDF", "Excel (CSV)")

        AlertDialog.Builder(context)
            .setTitle("Export Financial Statement")
            .setItems(formats) { _, which ->
                val asPdf = which == 0
                Toast.makeText(context, "Preparing export…", Toast.LENGTH_SHORT).show()

                fragment.lifecycleScope.launch {
                    try {
                        val file = if (asPdf) {
                            generatePdf(context, shopName, periodLabel, dateRangeLabel, statement)
                        } else {
                            generateCsv(context, shopName, periodLabel, dateRangeLabel, statement)
                        }
                        val mimeType = if (asPdf) "application/pdf" else "text/csv"
                        shareFile(context, file, mimeType)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    suspend fun generatePdf(
        context: Context,
        shopName: String,
        periodLabel: String,
        dateRangeLabel: String,
        statement: FinancialStatement
    ): File = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()
        // A4 at 72dpi
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            textSize = 20f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val subtitlePaint = Paint().apply {
            textSize = 13f
            textAlign = Paint.Align.CENTER
        }
        val smallPaint = Paint().apply {
            textSize = 10f
            textAlign = Paint.Align.CENTER
            color = android.graphics.Color.DKGRAY
        }
        val labelPaint = Paint().apply {
            textSize = 13f
            textAlign = Paint.Align.LEFT
        }
        val valuePaint = Paint().apply {
            textSize = 13f
            textAlign = Paint.Align.RIGHT
        }
        val boldLabelPaint = Paint().apply {
            textSize = 14f
            isFakeBoldText = true
            textAlign = Paint.Align.LEFT
        }
        val boldValuePaint = Paint().apply {
            textSize = 14f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }
        val linePaint = Paint().apply { strokeWidth = 1f }

        val centerX = 297.5f
        val leftX = 60f
        val rightX = 535f
        var y = 60f

        canvas.drawText("FINANCIAL STATEMENT", centerX, y, titlePaint)
        y += 26f
        canvas.drawText(shopName, centerX, y, subtitlePaint)
        y += 20f
        canvas.drawText("$periodLabel${if (dateRangeLabel.isNotBlank()) " • $dateRangeLabel" else ""}", centerX, y, subtitlePaint)
        y += 18f

        val generatedOn = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Generated on $generatedOn", centerX, y, smallPaint)
        y += 30f

        canvas.drawLine(leftX, y, rightX, y, linePaint)
        y += 30f

        fun row(label: String, value: Double, bold: Boolean = false) {
            val lp = if (bold) boldLabelPaint else labelPaint
            val vp = if (bold) boldValuePaint else valuePaint
            canvas.drawText(label, leftX, y, lp)
            canvas.drawText(CurrencyFormatter.formatFull(value), rightX, y, vp)
            y += if (bold) 26f else 22f
        }

        row("Total Sales", statement.sales)
        row("Total Purchases", statement.purchases)
        row("Total Expenses", statement.expenses)

        y += 6f
        canvas.drawLine(leftX, y, rightX, y, linePaint)
        y += 24f

        row("Gross Margin", statement.grossMargin, bold = true)
        row("Net Profit", statement.netProfit, bold = true)

        val profitMargin = if (statement.sales > 0) (statement.netProfit / statement.sales * 100) else 0.0
        canvas.drawText("Profit Margin", leftX, y, labelPaint)
        canvas.drawText(String.format(Locale.US, "%.1f%%", profitMargin), rightX, y, valuePaint)
        y += 22f

        row("Inventory Value", statement.inventory)

        y += 20f
        canvas.drawLine(leftX, y, rightX, y, linePaint)
        y += 24f
        canvas.drawText("Generated by Osebo", centerX, 800f, smallPaint)

        pdfDocument.finishPage(page)

        val file = createOutputFile(context, "financial_statement", "pdf")
        FileOutputStream(file).use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }
        pdfDocument.close()

        file
    }

    suspend fun generateCsv(
        context: Context,
        shopName: String,
        periodLabel: String,
        dateRangeLabel: String,
        statement: FinancialStatement
    ): File = withContext(Dispatchers.IO) {
        val generatedOn = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
        val profitMargin = if (statement.sales > 0) (statement.netProfit / statement.sales * 100) else 0.0

        val sb = StringBuilder()
        sb.appendLine("Financial Statement")
        sb.appendLine("Shop,\"$shopName\"")
        sb.appendLine("Period,\"$periodLabel\"")
        sb.appendLine("Date Range,\"$dateRangeLabel\"")
        sb.appendLine("Generated,\"$generatedOn\"")
        sb.appendLine()
        sb.appendLine("Metric,Amount (UGX)")
        sb.appendLine("Total Sales,${statement.sales}")
        sb.appendLine("Total Purchases,${statement.purchases}")
        sb.appendLine("Total Expenses,${statement.expenses}")
        sb.appendLine("Gross Margin,${statement.grossMargin}")
        sb.appendLine("Net Profit,${statement.netProfit}")
        sb.appendLine("Profit Margin (%),${String.format(Locale.US, "%.1f", profitMargin)}")
        sb.appendLine("Inventory Value,${statement.inventory}")

        val file = createOutputFile(context, "financial_statement", "csv")
        FileOutputStream(file).use { outputStream ->
            outputStream.write(sb.toString().toByteArray())
        }

        file
    }

    private fun createOutputFile(context: Context, baseName: String, extension: String): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        return File(documentsDir, "${baseName}_$timestamp.$extension")
    }

    fun shareFile(context: Context, file: File, mimeType: String): Boolean {
        return try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_SUBJECT, "Financial Statement")
                putExtra(Intent.EXTRA_TEXT, "Please find attached the financial statement")
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Financial Statement"))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
