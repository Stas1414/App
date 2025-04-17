package com.example.app.api

import com.example.app.request.DealByBarcodeRequest
import com.example.app.request.DocumentElementRequest
import com.example.app.request.DocumentIdRequest
import com.example.app.request.DocumentRequest
import com.example.app.request.PasswordRequest
import com.example.app.request.ProductIdRequest
import com.example.app.request.StageRequest
import com.example.app.request.UpdateDealRequest
import com.example.app.response.DealByBarcodeResponse
import com.example.app.response.DocumentElementResponse
import com.example.app.response.DocumentIdResponse
import com.example.app.response.DocumentResponse
import com.example.app.response.PasswordResponse
import com.example.app.response.ProductBarcodeResponse
import com.example.app.response.ProductResponse
import com.example.app.response.StageResponse
import com.example.app.response.StoreResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiBitrix {

    @POST("crm.deal.list")
    suspend fun getDealByBarcode(
        @Body request: DealByBarcodeRequest
    ): Response<DealByBarcodeResponse>


    @POST("lists.element.get")
    fun getPasswords(@Body request: PasswordRequest): Call<PasswordResponse>

    @POST("crm.deal.update")
    suspend fun updateDeal(
        @Body request: UpdateDealRequest
    ): Response<Boolean>

    @POST("crm.status.list")
    suspend fun getStage(
        @Body request: StageRequest
    ): Response<StageResponse>

    @GET("barcode.getproductid.json")
    suspend fun getProductIdByBarcode(
        @Query("barcode") barcode: String
    ): Response<ProductBarcodeResponse>

    @POST("catalog.product.list")
    suspend fun getProductsById(
        @Body request: ProductIdRequest
    ): Response<ProductResponse>

    @POST("catalog.document.element.list")
    suspend fun getDocID(
        @Body request: DocumentIdRequest
    ): Response<DocumentIdResponse>

    @POST("catalog.document.list")
    suspend fun getDocuments(
        @Body request: DocumentRequest
    ): Response<DocumentResponse>

    @POST("catalog.document.element.list")
    suspend fun getDocumentElements(
        @Body request: DocumentElementRequest
    ): Response<DocumentElementResponse>

    @GET("catalog.store.list")
    suspend fun getStoreList(): Response<StoreResponse>
}