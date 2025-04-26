package com.example.sameteamappandroid

import retrofit2.Call
import retrofit2.http.*

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

    @GET("Users")
    fun fetchUsers(): Call<List<User>>

    @POST("Chores")
    fun postChore(@Body chore: Chore): Call<Chore>

    @GET("Chores")
    fun fetchChores(): Call<List<Chore>>

    @PUT("Chores/{id}")
    fun completeChore(@Path("id") id: Int, @Body updatedChore: Chore): Call<Chore>

    @GET("Rewards")
    fun fetchRewards(): Call<List<Reward>>

    @POST("Rewards")
    fun postReward(@Body reward: Reward): Call<Reward>

    @PUT("Rewards/{id}")
    fun updateReward(@Path("id") id: Int, @Body reward: Reward): Call<Reward>

    @DELETE("Rewards/{id}")
    fun deleteReward(@Path("id") id: Int): Call<Void>

    @POST("Chores")
    fun rewardAsChore(@Body chore: Chore): Call<Chore>

    @GET("RedeemedRewards/{userId}")
    fun fetchRedeemedRewards(@Path("userId") userId: Int): Call<List<RedeemedReward>>

    @POST("RedeemedRewards")
    fun postRedeemedReward(@Body redeemedReward: RedeemedReward): Call<RedeemedReward>

    @GET("Users/team/{teamId}")
    fun fetchTeam(@Path("teamId") teamId: Int): Call<Team>

    @POST("Users/addChild")
    fun addChild(@Query("email") email: String, @Query("parentId") parentId: Int): Call<User>


}
