package com.example.app.response

import com.example.app.model.Store

import com.google.gson.annotations.SerializedName

data class StoreResponse(


    @SerializedName("result")
    val result: StoreListResult?,

) {
    data class StoreListResult(


        @SerializedName("stores")
        val stores: List<Store>?
    )
}

