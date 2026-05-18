package com.example.truyvetyte.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // THAY BẰNG LINK RENDER CỦA BẠN (nhớ có dấu / ở cuối)
    private const val BASE_URL = "https://health-app-backend-pscl.onrender.com"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}