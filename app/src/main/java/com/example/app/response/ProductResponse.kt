package com.example.app.response // Или ваш пакет

import com.google.gson.annotations.SerializedName

// Основной класс ответа для catalog.product.list
data class ProductResponse(

    @SerializedName("result")
    val result: ProductListResult?,


) {


    data class ProductListResult(
        @SerializedName("products")
        val products: List<ProductInfo>?
    )


    data class ProductInfo(
        @SerializedName("iblockId")
        val iblockId: Int?,
        @SerializedName("id")
        val id: Int?,
        @SerializedName("name")
        val name: String?
    )
}