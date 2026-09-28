package com.aniruddha.dreamsave.data

data class Goal(
    val id: String,
    val name: String,
    val target: Double,
    val startingAmount: Double,
    val deadlineEpochDay: Long?,
    val description: String,
    val createdAt: Long,
    val completedAt: Long?
)

data class Deposit(
    val id: String,
    val goalId: String,
    val amount: Double,
    val timestamp: Long,
    val note: String
)

enum class ThemeMode { DARK, LIGHT, SYSTEM }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val celebrations: Boolean = true
)

data class AppData(
    val goals: List<Goal> = emptyList(),
    val deposits: List<Deposit> = emptyList(),
    val settings: AppSettings = AppSettings()
)
