package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_arrow_left
import com.devbrian.osebo.resources.iconsax_export
import com.devbrian.osebo.resources.iconsax_new_sale
import com.devbrian.osebo.resources.iconsax_print
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.painterResource

data class ReceiptItemUi(
    val name: String,
    val quantity: String,
    val unitPrice: String,
    val lineTotal: String,
    val discountLabel: String? = null,
)

data class ReceiptUiState(
    val invoiceNumber: String = "",
    val receiptDate: String = "",
    val customerName: String = "Walk-in Customer",
    val customerPhone: String = "N/A",
    val servedBy: String = "",
    val shopName: String = "",
    val shopAddress: String = "",
    val shopContact: String = "",
    val businessType: String = "",
    val items: List<ReceiptItemUi> = emptyList(),
    val subtotal: String = "UGX 0",
    val discount: String = "UGX 0",
    val tax: String = "UGX 0",
    val total: String = "UGX 0",
    val paid: String = "UGX 0",
    val change: String = "UGX 0",
    val paymentMethod: String = "",
)

/**
 * Mirrors ReceiptFragment/fragment_receipt.xml's card layout. Print and
 * Share are left as documented no-ops: Android's versions depend on a
 * Bluetooth thermal-printer SDK (PrinterConnectionManager) and native PDF
 * generation/share-sheet integration (PrintUtils) that have no commonMain
 * equivalent yet — a real port needs its own iOS-side (expect/actual) work.
 */
@Composable
fun ReceiptScreen(
    state: ReceiptUiState,
    onBackClick: () -> Unit,
    onPrintClick: () -> Unit,
    onShareClick: () -> Unit,
    onNewSaleClick: () -> Unit,
) {
    Scaffold(containerColor = ReceiptCanvas) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { ReceiptHeader(onBackClick = onBackClick) }
            item { ReceiptSuccessBadge() }
            item {
                ReceiptCard(state)
            }
            item {
                ReceiptActionButtons(
                    onPrintClick = onPrintClick,
                    onShareClick = onShareClick,
                    onNewSaleClick = onNewSaleClick,
                )
            }
        }
    }
}

@Composable
private fun ReceiptHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick, modifier = Modifier.size(42.dp)) {
            Icon(
                painter = painterResource(Res.drawable.iconsax_arrow_left),
                contentDescription = "Back",
                tint = ReceiptInk,
                modifier = Modifier.size(23.dp),
            )
        }
        Spacer(Modifier.width(5.dp))
        Text(
            text = "Receipt",
            fontFamily = oseboFontFamily(),
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            color = ReceiptInk,
        )
    }
}

@Composable
private fun ReceiptSuccessBadge() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(44.dp).background(ReceiptGreen.copy(alpha = 0.14f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = ReceiptGreen,
                modifier = Modifier.size(26.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Payment successful",
            fontFamily = oseboFontFamily(),
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = ReceiptGreen,
        )
    }
}

@Composable
private fun ReceiptCard(state: ReceiptUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = state.shopName.ifBlank { "Your shop" },
                fontFamily = oseboFontFamily(),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = ReceiptBlue,
                textAlign = TextAlign.Center,
            )
            if (state.businessType.isNotBlank()) {
                Text(
                    text = state.businessType,
                    fontFamily = oseboFontFamily(),
                    fontSize = 9.sp,
                    color = ReceiptMuted,
                )
            }
            if (state.shopAddress.isNotBlank()) {
                Text(
                    text = state.shopAddress,
                    fontFamily = oseboFontFamily(),
                    fontSize = 9.sp,
                    color = ReceiptMuted,
                    textAlign = TextAlign.Center,
                )
            }
            if (state.shopContact.isNotBlank()) {
                Text(
                    text = "Tel: ${state.shopContact}",
                    fontFamily = oseboFontFamily(),
                    fontWeight = FontWeight.Medium,
                    fontSize = 9.sp,
                    color = ReceiptBlue,
                )
            }
        }

        HorizontalDivider(color = ReceiptDivider)

        Row(modifier = Modifier.fillMaxWidth()) {
            ReceiptLabelValue(
                modifier = Modifier.weight(1f),
                label = "Receipt",
                value = state.invoiceNumber,
            )
            ReceiptLabelValue(
                modifier = Modifier.weight(1f),
                label = "Date",
                value = state.receiptDate,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ReceiptSurface)
                .padding(10.dp),
        ) {
            Text(
                text = state.customerName,
                fontFamily = oseboFontFamily(),
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = ReceiptBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = state.customerPhone,
                fontFamily = oseboFontFamily(),
                fontSize = 9.sp,
                color = ReceiptMuted,
            )
        }

        if (state.servedBy.isNotBlank()) {
            Text(
                text = "Served by: ${state.servedBy}",
                fontFamily = oseboFontFamily(),
                fontSize = 10.sp,
                color = ReceiptMuted,
            )
        }

        HorizontalDivider(color = ReceiptDivider)

        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(ReceiptBlue).padding(vertical = 6.dp, horizontal = 4.dp)) {
            Text(
                "ITEM",
                modifier = Modifier.weight(3f),
                fontFamily = oseboFontFamily(),
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = Color.White,
            )
            Text(
                "QTY",
                modifier = Modifier.weight(1f),
                fontFamily = oseboFontFamily(),
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Text(
                "TOTAL",
                modifier = Modifier.weight(1f),
                fontFamily = oseboFontFamily(),
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = Color.White,
                textAlign = TextAlign.End,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            state.items.forEach { item -> ReceiptItemRow(item) }
        }

        HorizontalDivider(color = ReceiptDivider)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ReceiptSurface)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            ReceiptSummaryRow("Subtotal", state.subtotal)
            if (state.discount != "UGX 0") ReceiptSummaryRow("Discount", state.discount, valueColor = ReceiptGreen)
            ReceiptSummaryRow("Tax", state.tax)
            HorizontalDivider(color = ReceiptDivider)
            ReceiptSummaryRow("TOTAL", state.total, strong = true)
            ReceiptSummaryRow("Paid", state.paid)
            ReceiptSummaryRow("Change", state.change, valueColor = ReceiptGreen)
            ReceiptSummaryRow("Payment method", state.paymentMethod)
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Thank you!",
                fontFamily = oseboFontFamily(),
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = ReceiptBlue,
            )
            Text(
                "Powered by Osebo",
                fontFamily = oseboFontFamily(),
                fontSize = 8.sp,
                color = ReceiptMuted,
            )
        }
    }
}

@Composable
private fun ReceiptLabelValue(modifier: Modifier = Modifier, label: String, value: String) {
    Column(modifier = modifier) {
        Text(label, fontFamily = oseboFontFamily(), fontSize = 8.sp, color = ReceiptMuted)
        Text(
            value.ifBlank { "—" },
            fontFamily = oseboFontFamily(),
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            color = ReceiptBlue,
        )
    }
}

@Composable
private fun ReceiptItemRow(item: ReceiptItemUi) {
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                item.name,
                modifier = Modifier.weight(3f),
                fontFamily = oseboFontFamily(),
                fontSize = 10.sp,
                color = ReceiptInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                item.quantity,
                modifier = Modifier.weight(1f),
                fontFamily = oseboFontFamily(),
                fontSize = 10.sp,
                color = ReceiptInk,
                textAlign = TextAlign.Center,
            )
            Text(
                item.lineTotal,
                modifier = Modifier.weight(1f),
                fontFamily = oseboFontFamily(),
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                color = ReceiptInk,
                textAlign = TextAlign.End,
            )
        }
        item.discountLabel?.let { label ->
            Text(
                "  $label",
                fontFamily = oseboFontFamily(),
                fontSize = 8.sp,
                color = ReceiptGreen,
            )
        }
    }
}

@Composable
private fun ReceiptSummaryRow(label: String, value: String, strong: Boolean = false, valueColor: Color = ReceiptInk) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            fontFamily = oseboFontFamily(),
            fontWeight = if (strong) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = if (strong) 12.sp else 10.sp,
            color = if (strong) ReceiptInk else ReceiptMuted,
        )
        Text(
            value.ifBlank { "—" },
            fontFamily = oseboFontFamily(),
            fontWeight = FontWeight.SemiBold,
            fontSize = if (strong) 13.sp else 10.sp,
            color = valueColor,
        )
    }
}

@Composable
private fun ReceiptActionButtons(
    onPrintClick: () -> Unit,
    onShareClick: () -> Unit,
    onNewSaleClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onPrintClick,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(50),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.iconsax_print),
                    contentDescription = null,
                    tint = ReceiptBlue,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text("Print", fontFamily = oseboFontFamily(), fontSize = 11.sp, color = ReceiptBlue)
            }
            OutlinedButton(
                onClick = onShareClick,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(50),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.iconsax_export),
                    contentDescription = null,
                    tint = ReceiptBlue,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text("Share", fontFamily = oseboFontFamily(), fontSize = 11.sp, color = ReceiptBlue)
            }
        }
        Button(
            onClick = onNewSaleClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = ReceiptBlue, contentColor = Color.White),
        ) {
            Icon(
                painter = painterResource(Res.drawable.iconsax_new_sale),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text("New sale", fontFamily = oseboFontFamily(), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
    }
}

private val ReceiptCanvas = Color(0xFFF5F6F8)
private val ReceiptSurface = Color(0xFFF1F3F4)
private val ReceiptDivider = Color(0xFFE4E7EB)
private val ReceiptInk = Color(0xFF171B1F)
private val ReceiptMuted = Color(0xFF7A838D)
private val ReceiptBlue = Color(0xFF0788CF)
private val ReceiptGreen = Color(0xFF1AA260)
