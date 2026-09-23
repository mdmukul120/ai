package com.example.data.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val appName: String,
    val packageName: String,
    val icon: Drawable? = null,
    val description: String = "",
    val category: String = "ইউটিলিটি",
    val isSystemApp: Boolean = false,
    val canLaunch: Boolean = true
)
