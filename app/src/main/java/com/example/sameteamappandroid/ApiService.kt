package com.example.sameteamappandroid

import retrofit2.Call
import retrofit2.http.*


// ============================== //
//          DATA CLASSES         //
// ============================== //
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

data class CreateTeamRequest(
    val userId: Int,
    val teamName: String,
    val teamPassword: String
)

data class JoinTeamRequest(
    val userId: Int,
    val teamName: String,
    val teamPassword: String
)

data class AddUserToTeamRequest(
    val email: String,
    val teamId: Int
)

// ============================== //
//          API SERVICE          //
// ============================== //
interface ApiService {

    // ---------- AUTH ---------- //
    @Headers("Content-Type: application/json")
    @POST("Auth/register")
    fun register(@Body request: RegisterRequest): Call<Void>

    @POST("Auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    // ---------- USERS ---------- //
    @GET("Users")
    fun fetchUsers(): Call<List<User>>

    @GET("Users/team/{teamId}")
    fun fetchTeam(@Path("teamId") teamId: Int): Call<Team>

    @POST("Users/addChild")
    fun addChild(@Query("email") email: String, @Query("parentId") parentId: Int): Call<User>

    @POST("Users/createTeam")
    fun createTeam(@Body request: CreateTeamRequest): Call<Team>

    @POST("Users/joinTeam")
    fun joinTeam(@Body request: JoinTeamRequest): Call<Team>

    @POST("Users/addUserToTeam")
    fun addUserToTeam(@Body request: AddUserToTeamRequest): Call<User>

    @POST("Users/removeFromTeam/{userId}")
    fun removeUserFromTeam(@Path("userId") userId: Int): Call<Void>

    @PUT("Users/{userId}/points")
    fun updateUserPoints(
        @Path("userId") userId: Int,
        @Body request: Map<String, Int>
    ): Call<Void>


    // ---------- CHORES ---------- //
    @POST("Chores")
    fun postChore(@Body chore: Chore): Call<Chore>

    @GET("Chores")
    fun fetchChores(): Call<List<Chore>>

    @PUT("Chores/{id}")
    fun completeChore(@Path("id") id: Int, @Body updatedChore: Chore): Call<Chore>

    @GET("Chores/completed")
    fun fetchCompletedChores(): Call<List<Chore>>

    @POST("Chores/complete/{choreId}")
    fun moveChoreToCompleted(@Path("choreId") choreId: Int): Call<Void>

    @POST("Chores/undoComplete/{completedChoreId}")
    fun undoCompletedChore(@Path("completedChoreId") completedChoreId: Int): Call<Void>

    @DELETE("Chores/{choreId}")
    fun deleteChore(@Path("choreId") choreId: Int): Call<Void>

    // ---------- REWARDS ---------- //
    @GET("Rewards")
    fun fetchRewards(): Call<List<Reward>>

    @POST("Rewards")
    fun postReward(@Body reward: Reward): Call<Reward>

    @PUT("Rewards/{id}")
    fun updateReward(@Path("id") id: Int, @Body reward: Reward): Call<Reward>

    @DELETE("Rewards/{id}")
    fun deleteReward(@Path("id") id: Int): Call<Void>

    @POST("Chores") // used to reward as chore
    fun rewardAsChore(@Body chore: Chore): Call<Chore>

    // ---------- REDEEMED REWARDS ---------- //
    @GET("RedeemedRewards/{userId}")
    fun fetchRedeemedRewards(@Path("userId") userId: Int): Call<List<RedeemedReward>>

    @POST("RedeemedRewards")
    fun postRedeemedReward(@Body redeemedReward: RedeemedReward): Call<RedeemedReward>
}
