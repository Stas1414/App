package com.example.app.response

import com.example.app.model.Deal
import com.google.gson.annotations.SerializedName

data class DealByBarcodeResponse(@SerializedName("result")
                                 val result: List<Deal>
)