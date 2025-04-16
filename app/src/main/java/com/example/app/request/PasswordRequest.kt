package com.example.app.request

import com.google.gson.annotations.SerializedName

data class PasswordRequest(
    @field:SerializedName("IBLOCK_TYPE_ID")
    val iblockTypeId: String,

    @field:SerializedName("IBLOCK_ID")
    val iblockId: Int
) {
}