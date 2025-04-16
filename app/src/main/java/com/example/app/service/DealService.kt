package com.example.app.service

import android.util.Log
import com.example.app.api.ApiBitrix
import com.example.app.model.Deal
import com.example.app.request.DealByBarcodeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DealService {

    private var apiBitrix: ApiBitrix
    private var baseUrl = "https://bitrix.izocom.by/rest/1/o2deu7wx7zfl3ib4/"


    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiBitrix = retrofit.create(ApiBitrix::class.java)

    }

    suspend fun getDealByBarcode(barcode: String): Deal? {

        val request = DealByBarcodeRequest(
            filter = mutableMapOf(
                "UF_CRM_1744639195227" to barcode
            )
        )

        return withContext(Dispatchers.IO) {
            try {
                val response = apiBitrix.getDealByBarcode(request)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    responseBody?.result?.firstOrNull()
                } else {
                    Log.e("DealService", "API Error ${response.code()}: ${response.message()}")
                    null
                }
            } catch (e: Exception) {
                Log.e("DealService", "Error fetching deal by barcode: $barcode", e)
                null
            }
        }
    }
}