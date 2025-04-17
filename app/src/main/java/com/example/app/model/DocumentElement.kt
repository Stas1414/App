package com.example.app.model

import com.google.gson.annotations.SerializedName
data class DocumentElement(

    @SerializedName("amount")
    val amount: Double?,

    @SerializedName("docId")
    val docId: Int?,

    @SerializedName("elementId")
    val elementId: Int?,

    @SerializedName("id")
    val id: Int?,

    @SerializedName("purchasingPrice")
    val purchasingPrice: Double?,

    @SerializedName("storeFrom")
    val storeFrom: Int?,

    @SerializedName("storeTo")
    val storeTo: Int?,

    val name: String?,

    var storeFromName: String?
)