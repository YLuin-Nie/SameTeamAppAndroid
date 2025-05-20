package com.example.sameteamappandroid

data class User(
    val userId: Int,
    val username: String,
    val email: String,
    val role: String,
    val points: Int,
    val totalPoints: Int,
    val teamId: Int?
)
