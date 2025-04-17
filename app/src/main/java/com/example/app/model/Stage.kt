package com.example.app.model

import com.google.gson.annotations.SerializedName



data class Stage(

    @SerializedName("ID")
    val id: String,

    @SerializedName("ENTITY_ID")
    val entityId: String,

    @SerializedName("STATUS_ID")
    val statusId: String,

    @SerializedName("NAME")
    val name: String,

    @SerializedName("NAME_INIT")
    val nameInit: String,

    @SerializedName("SORT")
    val sort: Int,

    @SerializedName("SYSTEM")
    val system: Boolean,

    @SerializedName("COLOR")
    val color: String,

    @SerializedName("SEMANTICS")
    val semantics: String?,

    @SerializedName("CATEGORY_ID")
    val categoryId: String


)
