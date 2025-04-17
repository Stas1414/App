package com.example.app.request

data class DocumentIdRequest(
    var select: MutableList<String>? = null,
    var filter: MutableMap<String, Any?>? = null
    ) {
}