package com.example.app.response

import com.example.app.model.Stage
import com.google.gson.annotations.SerializedName

data class StageResponse(
    @SerializedName("result")
    val result: List<Stage>


)