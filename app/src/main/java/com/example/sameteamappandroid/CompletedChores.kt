package com.example.sameteamappandroid

data class CompletedChores(
    val completedId: Int,
    val choreId: Int,
    val choreText: String,
    val points: Int,
    val assignedTo: Int?,
    val dateAssigned: String,
    val completionDate: String
)
