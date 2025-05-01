package com.example.sameteamappandroid

data class Chore(
    val choreId: Int,
    val choreText: String,
    val points: Int,
    val assignedTo: Int,
    val dateAssigned: String,
    val completed: Boolean
)
