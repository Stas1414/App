package com.example.app.request

data class UpdateDealRequest(
    var id: Double,
    var fields: MutableMap<String, Any?>? = null
) {
}