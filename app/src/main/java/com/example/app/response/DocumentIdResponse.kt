package com.example.app.response

import com.google.gson.annotations.SerializedName

data class DocumentIdResponse(
    @SerializedName("result")
    val result: ResultData?
) {

    data class ResultData(
        @SerializedName("documentElements")
        val documentElements: List<DocumentElement>?
    )

    data class DocumentElement(
        @SerializedName("docId")
        val docId: Int?
    )
}