package com.devesh.spendwise.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.devesh.spendwise.data.local.ExpenseEntity
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class MonthlyReportData(
    val monthName: String,
    val year: Int,
    val totalSpending: Double,
    val monthlyBudget: Double,
    val remainingBudget: Double,
    val savings: Double,
    val budgetPercentage: Double,
    val topCategory: String,
    val topCategoryAmount: Double,
    val topMerchant: String,
    val topMerchantAmount: Double,
    val highestExpense: ExpenseEntity?,
    val dailyAvgSpending: Double,
    val avgTransactionValue: Double,
    val highestSpendingDay: String,
    val totalTransactions: Int,
    val categoryBreakdown: List<Pair<String, Double>>,
    val expenses: List<ExpenseEntity>
)

object ReportExporter {

    @RequiresApi(Build.VERSION_CODES.O)
    fun exportPdf(context: Context, data: MonthlyReportData): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val titlePaint = Paint().apply {
            color = Color.parseColor("#4143D5")
            textSize = 24f
            isFakeBoldText = true
        }
        val headerPaint = Paint().apply {
            color = Color.parseColor("#1B1B23")
            textSize = 14f
            isFakeBoldText = true
        }
        val bodyPaint = Paint().apply {
            color = Color.parseColor("#464555")
            textSize = 11f
        }

        var y = 40f

        // Draw Header
        canvas.drawText("SpendWise Financial Report", 40f, y, titlePaint)
        y += 24f
        canvas.drawText("Period: ${data.monthName} ${data.year}", 40f, y, bodyPaint)
        y += 30f

        // Draw Divider
        paint.color = Color.parseColor("#C6C5D7")
        paint.strokeWidth = 1f
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 30f

        // Financial Summary Box
        canvas.drawText("Financial Summary", 40f, y, headerPaint)
        y += 20f
        canvas.drawText("Total Spent: Rs. ${String.format(Locale.US, "%.2f", data.totalSpending)}", 50f, y, bodyPaint)
        canvas.drawText("Total Budget: Rs. ${String.format(Locale.US, "%.2f", data.monthlyBudget)}", 300f, y, bodyPaint)
        y += 18f
        canvas.drawText("Remaining Budget: Rs. ${String.format(Locale.US, "%.2f", data.remainingBudget)}", 50f, y, bodyPaint)
        canvas.drawText("Est. Savings: Rs. ${String.format(Locale.US, "%.2f", data.savings)}", 300f, y, bodyPaint)
        y += 30f

        // Category Breakdown
        canvas.drawText("Category Spending Breakdown", 40f, y, headerPaint)
        y += 20f
        data.categoryBreakdown.forEach { (cat, amount) ->
            canvas.drawText("- $cat: Rs. ${String.format(Locale.US, "%.2f", amount)}", 50f, y, bodyPaint)
            y += 16f
        }
        y += 20f

        // Key Metrics
        canvas.drawText("Key Statistics", 40f, y, headerPaint)
        y += 20f
        canvas.drawText("Top Category: ${data.topCategory}", 50f, y, bodyPaint)
        canvas.drawText("Top Merchant: ${data.topMerchant}", 300f, y, bodyPaint)
        y += 18f
        canvas.drawText("Daily Average: Rs. ${String.format(Locale.US, "%.2f", data.dailyAvgSpending)}", 50f, y, bodyPaint)
        canvas.drawText("Avg Txn Value: Rs. ${String.format(Locale.US, "%.2f", data.avgTransactionValue)}", 300f, y, bodyPaint)
        y += 18f
        canvas.drawText("Total Transactions: ${data.totalTransactions}", 50f, y, bodyPaint)
        canvas.drawText("Peak Day: ${data.highestSpendingDay}", 300f, y, bodyPaint)

        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "SpendWise_Report_${data.monthName}_${data.year}.pdf")
        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            Toast.makeText(context, "PDF Report generated: ${file.name}", Toast.LENGTH_SHORT).show()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun exportCsv(context: Context, data: MonthlyReportData): File? {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.US)
        val sb = StringBuilder()
        sb.append("ID,Date,Category,Amount,Payment Mode,Note,Reference\n")

        data.expenses.forEach { expense ->
            val dateStr = Instant.ofEpochMilli(expense.date)
                .atZone(ZoneId.systemDefault())
                .format(formatter)
            val noteEscaped = "\"${expense.note.replace("\"", "\"\"")}\""
            val refEscaped = "\"${(expense.reference ?: "").replace("\"", "\"\"")}\""

            sb.append("${expense.id},$dateStr,${expense.category},${expense.amount},${expense.paymentMode},$noteEscaped,$refEscaped\n")
        }

        val file = File(context.cacheDir, "SpendWise_Expenses_${data.monthName}_${data.year}.csv")
        return try {
            file.writeText(sb.toString())
            Toast.makeText(context, "CSV File generated: ${file.name}", Toast.LENGTH_SHORT).show()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareFile(context: Context, file: File, mimeType: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share SpendWise Report"))
    }

    fun shareSummaryText(context: Context, data: MonthlyReportData) {
        val summary = "SpendWise Monthly Report (${data.monthName} ${data.year}):\n" +
                "• Total Spent: ₹${String.format(Locale.US, "%.2f", data.totalSpending)}\n" +
                "• Budget: ₹${String.format(Locale.US, "%.2f", data.monthlyBudget)}\n" +
                "• Remaining: ₹${String.format(Locale.US, "%.2f", data.remainingBudget)}\n" +
                "• Top Category: ${data.topCategory}\n" +
                "• Top Merchant: ${data.topMerchant}\n" +
                "• Transactions: ${data.totalTransactions}"

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Summary"))
    }
}
