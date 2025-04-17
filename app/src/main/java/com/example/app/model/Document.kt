package com.example.app.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.time.OffsetDateTime

data class Document(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("commentary")
    val commentary: String?,

    @SerializedName("createdBy")
    val createdBy: Int?,

    @SerializedName("currency")
    val currency: String?,

    @SerializedName("dateCreate")
    val dateCreate: String?,

    @SerializedName("dateDocument")
    val dateDocument: String?,

    @SerializedName("dateModify")
    val dateModify: String?,

    @SerializedName("dateStatus")
    val dateStatus: String?,

    @SerializedName("docNumber")
    val docNumber: String?,

    @SerializedName("docType")
    val docType: String?,

    @SerializedName("modifiedBy")
    val modifiedBy: Int?,

    @SerializedName("responsibleId")
    val responsibleId: Int?,

    @SerializedName("siteId")
    val siteId: String?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("statusBy")
    val statusBy: Int?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("total")
    val total: Double?
) : Serializable
