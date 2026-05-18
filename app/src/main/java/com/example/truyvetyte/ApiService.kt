package com.example.truyvetyte.network

import com.example.truyvetyte.LoginRequest
import com.example.truyvetyte.model.RegisterRequest
import com.example.truyvetyte.model.RegisterResponse
import com.example.truyvetyte.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import com.example.truyvetyte.model.HealthResponse
import retrofit2.http.GET

interface ApiService {
    @GET("api/health")
    suspend fun checkHealth(): Response<HealthResponse>
    // Đường dẫn API bạn đã viết bên Express
    @POST("api/auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("api/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>
}