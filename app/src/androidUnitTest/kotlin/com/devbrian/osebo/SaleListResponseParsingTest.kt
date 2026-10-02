package com.devbrian.osebo

import com.devbrian.osebo.data.remote.dto.response.SaleApiData
import com.devbrian.osebo.data.remote.dto.response.SaleApiDataSerializer
import com.devbrian.osebo.data.remote.dto.response.SaleListApiResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaleListResponseParsingTest {
    // SaleApiData lives in commonMain (shared with the Ktor/iOS client) and only
    // carries kotlinx.serialization's @SerialName, so a bare Gson() can't match
    // its alternate field names (e.g. "total_amount" for totalPrice). Mirror the
    // same bridge NetworkModule registers in production instead of a plain Gson.
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(
            SaleApiData::class.java,
            JsonDeserializer { json, _, _ ->
                Json { ignoreUnknownKeys = true }.decodeFromString(SaleApiDataSerializer, json.toString())
            }
        )
        .create()

    @Test
    fun saleListResponse_acceptsLiveSnakeCaseFields() {
        val response = gson.fromJson(
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
