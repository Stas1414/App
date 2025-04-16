package com.example.app.service

import com.example.app.api.ApiBitrix
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DealService {

    private var apiBitrix: ApiBitrix
    private var baseUrl = ""


    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiBitrix = retrofit.create(ApiBitrix::class.java)

    }
}