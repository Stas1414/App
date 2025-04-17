package com.example.app.service

import android.util.Log
import com.example.app.api.ApiBitrix
import com.example.app.model.Deal
import com.example.app.model.Stage
import com.example.app.request.DealByBarcodeRequest
import com.example.app.request.StageRequest
import com.example.app.request.UpdateDealRequest
import com.example.app.response.StageResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
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

    suspend fun updateDealToStageByName(dealId: Double): Boolean {

        val targetStageName = "Успешно завершен"

        Log.i("DealService", "Attempting to find stage '$targetStageName' to update deal $dealId...")
        val targetStage: Stage? = fetchStageByName(targetStageName)


        if (targetStage == null) {

            Log.e("DealService", "Cannot update deal $dealId because stage '$targetStageName' was not found.")
            return false
        }


        val stageIdToAssign = targetStage.statusId



        if (stageIdToAssign.isBlank()) {
            Log.e("DealService", "Found stage '$targetStageName', but its relevant ID (statusId/id) is blank. Cannot update deal $dealId.")
            return false
        }


        Log.i("DealService", "Found stage '$targetStageName' with ID to assign: '$stageIdToAssign'. Attempting to update deal $dealId...")


        return updateDealStageInternal(dealId, stageIdToAssign)
    }




    private suspend fun fetchStageByName(stageName: String): Stage? {
        val request = StageRequest(
            filter = mutableMapOf(
                "ENTITY_ID" to  "STATUS",
                "NAME" to  stageName
            )
        )

        return withContext(Dispatchers.IO) {
            try {
                val response: Response<StageResponse> = apiBitrix.getStage(request)

                if (response.isSuccessful) {
                    val stageResponse = response.body()
                    if (stageResponse != null) {
                        val foundStage = stageResponse.result.firstOrNull()
                        if (foundStage != null) {
                            Log.i("DealService", "[Internal] Successfully fetched stage '${foundStage.name}' (ID: ${foundStage.id}, STATUS_ID: ${foundStage.statusId})")
                            foundStage
                        } else {
                            Log.w("DealService", "[Internal] API call successful, but no stage found with name: $stageName in response: ${response.body()?.toString()}")
                            null
                        }
                    } else {
                        Log.e("DealService", "[Internal] API call successful for stage '$stageName', but response body was null.")
                        null
                    }
                } else {
                    Log.e("DealService", "[Internal] Failed to fetch stage '$stageName'. Code: ${response.code()}, Error: ${response.errorBody()?.string()}")
                    null
                }
            } catch (e: Exception) {
                Log.e("DealService", "[Internal] Error fetching stage '$stageName'", e)
                null
            }
        }
    }


    private suspend fun updateDealStageInternal(dealId: Double, stageId: String): Boolean {


        val request = UpdateDealRequest(
            id = dealId,
            fields = mutableMapOf(

                "STAGE_ID" to stageId
            )
        )

        return withContext(Dispatchers.IO) {
            try {

                val response: Response<Boolean> = apiBitrix.updateDeal(request)

                if (response.isSuccessful) {
                    Log.i(
                        "DealService",
                        "[Internal] Successfully updated deal ID: $dealId to stage ID: $stageId. Status: ${response.code()}"
                    )
                    true
                } else {
                    Log.e(
                        "DealService",
                        "[Internal] Failed to update deal ID: $dealId to stage $stageId. Status: ${response.code()} - Body: ${
                            response.errorBody()?.string()
                        }"
                    )
                    false
                }
            } catch (e: Exception) {
                Log.e(
                    "DealService",
                    "[Internal] Error updating deal ID: $dealId to stage $stageId",
                    e
                )
                false
            }
        }


    }
}