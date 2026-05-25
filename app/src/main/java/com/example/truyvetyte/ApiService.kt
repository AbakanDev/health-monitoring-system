package com.example.truyvetyte.network

import com.example.truyvetyte.CheckInRequest
import com.example.truyvetyte.LoginRequest
import com.example.truyvetyte.LoginResponse
import com.example.truyvetyte.model.AiAskRequest
import com.example.truyvetyte.model.AiAskResponse
import com.example.truyvetyte.model.CachLyResponse
import com.example.truyvetyte.model.CheckInResponse
import com.example.truyvetyte.model.CheckinHistoryResponse
import com.example.truyvetyte.model.CheckinStatsResponse
import com.example.truyvetyte.model.ContactHistoryResponse
import com.example.truyvetyte.model.ContactStatsResponse
import com.example.truyvetyte.model.CuaKhauListResponse
import com.example.truyvetyte.model.DashboardOverviewResponse
import com.example.truyvetyte.model.DashboardSummaryResponse
import com.example.truyvetyte.model.HealthResponse
import com.example.truyvetyte.model.ImmigrationSubmitResponse
import com.example.truyvetyte.model.KhaiBaoRequest
import com.example.truyvetyte.model.KhaiBaoSubmitResponse
import com.example.truyvetyte.model.RegisterModels
import com.example.truyvetyte.model.RegisterRequest
import com.example.truyvetyte.model.TiemChungResponse // Import model mới
import com.example.truyvetyte.model.TrendResponse
import com.example.truyvetyte.model.VaccineRatesResponse
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

    @GET("api/health/vaccine-rates")
    suspend fun getVaccineRates(): Response<VaccineRatesResponse>

    @GET("api/health/dashboard-summary")
    suspend fun getDashboardSummary(): Response<DashboardSummaryResponse>

    @GET("api/health/contact-stats/{cccd}")
    suspend fun getContactStats(
        @Header("Authorization") token: String,
        @Path("cccd") cccd: String
    ): Response<ContactStatsResponse>

    @GET("api/health/contact-history/{cccd}")
    suspend fun getContactHistory(
        @Header("Authorization") token: String,
        @Path("cccd") cccd: String
    ): Response<ContactHistoryResponse>

    @GET("api/health/checkin-stats/{cccd}")
    suspend fun getCheckinStats(
        @Header("Authorization") token: String,
        @Path("cccd") cccd: String
    ): Response<CheckinStatsResponse>

    @GET("api/health/checkin-history/{cccd}")
    suspend fun getCheckinHistory(
        @Header("Authorization") token: String,
        @Path("cccd") cccd: String
    ): Response<CheckinHistoryResponse>

    @GET("api/health/health-declaration-history/{cccd}")
    suspend fun getKhaiBaoHistory(
        @Header("Authorization") token: String,
        @Path("cccd") cccd: String
    ): Response<com.example.truyvetyte.model.KhaiBaoHistoryResponse>

    @POST("api/health/health-declaration")
    suspend fun submitKhaiBao(
        @Header("Authorization") token: String,
        @Body request: KhaiBaoRequest
    ): Response<KhaiBaoSubmitResponse>

    @GET("api/health/immigration-history/{cccd}")
    suspend fun getImmigrationHistory(
        @Header("Authorization") token: String,
        @Path("cccd") cccd: String
    ): Response<com.example.truyvetyte.model.ImmigrationHistoryResponse>

    @GET("api/health/cua-khau")
    suspend fun getCuaKhauList(
        @Header("Authorization") token: String
    ): Response<CuaKhauListResponse>

    @POST("api/health/immigration-declaration")
    suspend fun submitImmigrationDeclaration(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<ImmigrationSubmitResponse>

    @POST("api/health/checkin")
    suspend fun sendCheckIn(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<CheckInResponse>

    @POST("api/health/ai/ask")
    suspend fun askHealthAI(@Body body: AiAskRequest): Response<AiAskResponse>

    @GET("api/health/dashboard-overview")
    suspend fun getDashboardOverview(
        @Header("Authorization") token: String
    ): Response<DashboardOverviewResponse>
}