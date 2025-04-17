package com.example.app.api

import com.example.app.request.DealByBarcodeRequest
import com.example.app.request.NewDocumentRequest
import com.example.app.request.PasswordRequest
import com.example.app.request.StageRequest
import com.example.app.request.UpdateDealRequest
import com.example.app.response.DealByBarcodeResponse
import com.example.app.response.PasswordResponse
import com.example.app.response.StageResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiBitrix {

    @POST("crm.deal.list")
    suspend fun getDealByBarcode(
        @Body request: DealByBarcodeRequest
    ) : Response<DealByBarcodeResponse>


    @POST("lists.element.get")
    fun getPasswords(@Body request: PasswordRequest): Call<PasswordResponse>

    @POST("crm.deal.update")
    suspend fun updateDeal(
        @Body request: UpdateDealRequest
    ) : Response<Boolean>

    @POST("catalog.document.add")
    suspend fun addNewDocument(
        @Body request: NewDocumentRequest
    ) : Response<HashMap<String, Any?>>

    @POST("crm.status.list")
    suspend fun getStage(
        @Body request: StageRequest
    ) : Response<StageResponse>
}