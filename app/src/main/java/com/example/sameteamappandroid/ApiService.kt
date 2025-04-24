package com.example.sameteamappandroid

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val role: String?,
    val team: String?,
    val teamPassword: String?
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: User
)

data class User(
    val userId: Int,
    val username: String,
    val email: String,
    val role: String,
    val points: Int,
    val teamId: Int?
)

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("Auth/register")
    fun register(@Body request: RegisterRequest): Call<Void>

    @POST("Auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}
