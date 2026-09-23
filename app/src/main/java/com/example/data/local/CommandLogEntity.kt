package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "command_logs")
data class CommandLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val sender: String, // "boss" or "charu"
    val messageText: String,
    val actionType: String = "NONE",
    val actionDetails: String = "",
    val isSuccess: Boolean = true
)
