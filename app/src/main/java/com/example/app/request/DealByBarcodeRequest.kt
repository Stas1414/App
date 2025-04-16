package com.example.app.request

data class DealByBarcodeRequest(
    var filter: MutableMap<String, Any?>? = null
) {
}