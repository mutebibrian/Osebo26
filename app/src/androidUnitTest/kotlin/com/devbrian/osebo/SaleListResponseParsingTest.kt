package com.devbrian.osebo

import com.devbrian.osebo.data.remote.dto.response.SaleListApiResponse
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaleListResponseParsingTest {
    @Test
    fun saleListResponse_acceptsLiveSnakeCaseFields() {
        val response = Gson().fromJson(
            """
                {
                  "success": true,
                  "data": [
                    {
                      "id": "sale-id",
                      "invoice_number": "INV-100",
                      "created_at": "2026-09-24T10:30:00.000Z",
                      "total_amount": 45000,
                      "paid_amount": "45000",
                      "payment_status": "completed",
                      "sale_stock_items": [
                        {
                          "id": "line-id",
                          "quantity": 2,
                          "price": 22500,
                          "discount": 0,
                          "amount": 45000
                        }
                      ]
                    }
                  ]
                }
            """.trimIndent(),
            SaleListApiResponse::class.java,
        )

        assertTrue(response.success)
        val sale = response.data.orEmpty().single()
        assertEquals("INV-100", sale.invoiceNumber)
        assertEquals(45000.0, sale.totalPrice ?: 0.0, 0.0)
        assertEquals("completed", sale.paymentStatus)
        assertEquals(1, sale.saleStockItems.orEmpty().size)
    }
}
