package com.example.app.response

import com.example.app.model.DocumentElement
import com.google.gson.annotations.SerializedName

data class DocumentElementResponse(
    @SerializedName("result")
    val result: ResultData?
) {

    data class ResultData(
        @SerializedName("documentElements")
        val documentElements: List<DocumentElement>?
    )



}