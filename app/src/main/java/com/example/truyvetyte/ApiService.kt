package com.example.truyvetyte.network

import com.example.truyvetyte.CheckInRequest
import com.example.truyvetyte.LoginRequest
import com.example.truyvetyte.LoginResponse
import com.example.truyvetyte.model.CachLy
import com.example.truyvetyte.model.CachLyResponse
import com.example.truyvetyte.model.HealthResponse
import com.example.truyvetyte.model.RegisterModels
import com.example.truyvetyte.model.RegisterRequest
import com.example.truyvetyte.model.TiemChungResponse // Import model mới
import com.example.truyvetyte.model.TrendResponse
import com.example.truyvetyte.model.XetNghiemResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("api/health")
    suspend fun checkHealth(): Response<HealthResponse>

    @POST("api/auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<RegisterModels>

    @POST("api/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/auth/tiemchung/{cccd}")
    suspend fun getThongTinTiemChung(
        @Header("Authorization") token: String,
        @Path("cccd") cccd: String
    ): Response<TiemChungResponse>
    @GET("api/health/quarantine-status/{cccd}")
    suspend fun getThongTinCachLy(
        @Header("Authorization") token: String,
        @Path("cccd") cccd: String
    ): Response<CachLyResponse>

    @GET("api/health/test-history/{cccd}")
    suspend fun getLichSuXetNghiem(
        @Header("Authorization") token: String,
        @Path("cccd") cccd: String
    ): Response<XetNghiemResponse>

    @GET("api/health/trend-f0")
    suspend fun getTrendAnalysis(): Response<TrendResponse>

    @POST("api/checkin")
    suspend fun sendCheckIn(
        @Body request: CheckInRequest
    ): Response<ResponseBody>
}