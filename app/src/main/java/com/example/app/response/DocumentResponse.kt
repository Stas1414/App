package com.example.app.response

import com.example.app.model.Document
import com.google.gson.annotations.SerializedName


data class DocumentResponse(
    @SerializedName("result")
    val result: DocumentListResult?,
) {


    data class DocumentListResult(

        @SerializedName("documents")
        val documents: MutableList<Document>?
    )
}