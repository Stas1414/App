package com.example.app.api

import com.example.app.request.DealByBarcodeRequest
import com.example.app.response.DealByBarcodeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiBitrix {

    @POST("crm.deal.list")
    suspend fun getDealByBarcode(
        @Body request: DealByBarcodeRequest
    ) : Response<DealByBarcodeResponse>
}