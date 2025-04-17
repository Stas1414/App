package com.example.app.request

data class ProductIdRequest(
    val select: List<String> = listOf("id", "iblockId", "name"),
    val filter: MutableMap<String, Any?>? = null
)
