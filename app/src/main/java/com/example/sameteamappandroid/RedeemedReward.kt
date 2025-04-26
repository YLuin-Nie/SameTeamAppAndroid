package com.example.sameteamappandroid

data class RedeemedReward(
    val redemptionId: Int,
    val userId: Int,
    val rewardId: Int,
    val rewardName: String,
    val pointsSpent: Int,
    val dateRedeemed: String
)
