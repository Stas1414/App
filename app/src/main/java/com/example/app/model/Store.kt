package com.example.app.model // Или ваш пакет

import com.google.gson.annotations.SerializedName
data class Store(

    @SerializedName("id")
    val id: Int?,

    @SerializedName("active")
    val active: String?,

    @SerializedName("address")
    val address: String?,

    @SerializedName("code")
    val code: String?,

    @SerializedName("dateCreate")
    val dateCreate: String?,

    @SerializedName("dateModify")
    val dateModify: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("gpsN")
    val gpsN: Double?,

    @SerializedName("gpsS")
    val gpsS: Double?,

    @SerializedName("issuingCenter")
    val issuingCenter: String?,

    @SerializedName("modifiedBy")
    val modifiedBy: Int?,

    @SerializedName("phone")
    val phone: String?,

    @SerializedName("schedule")
    val schedule: String?,

    @SerializedName("sort")
    val sort: Int?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("userId")
    val userId: Int?,

    @SerializedName("xmlId")
    val xmlId: String?

)