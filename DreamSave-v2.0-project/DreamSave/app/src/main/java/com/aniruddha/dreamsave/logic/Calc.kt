package com.aniruddha.dreamsave.logic

import com.aniruddha.dreamsave.data.AppData
import com.aniruddha.dreamsave.data.Deposit
import com.aniruddha.dreamsave.data.Goal
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

data class Streaks(val current: Int, val best: Int)

data class Recommendation(val amount: Double?, val caption: String)

val QUICK_AMOUNTS: List<Int> = listOf(10, 20, 50, 100, 200, 500, 1000)

const val MAX_AMOUNT: Double = 1_000_000_000.0

object Calc {

    fun today(): LocalDate = LocalDate.now()

    fun dayOf(timestamp: Long): LocalDate =
        Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()

    fun savedOf(goal: Goal, deposits: List<Deposit>): Double {
        var total = goal.startingAmount
        for (d in deposits) {
            if (d.goalId == goal.id) total += d.amount
        }
        return total
    }

    fun progressOf(goal: Goal, saved: Double): Float {
        if (goal.target <= 0.0) return 0f
        return (saved / goal.target).coerceIn(0.0, 1.0).toFloat()
    }

    fun remainingOf(goal: Goal, saved: Double): Double = max(0.0, goal.target - saved)

    fun daysLeft(goal: Goal): Long? {
        val deadline = goal.deadlineEpochDay ?: return null
        return deadline - today().toEpochDay()
    }

    fun deadlineLabel(goal: Goal): String {
        val left = daysLeft(goal) ?: return "No deadline"
        return when {
            left < 0L -> "Overdue"
            left == 0L -> "Due today"
            left == 1L -> "1 day left"
            else -> "$left days left"
        }
    }

    fun recommendation(goal: Goal, saved: Double): Recommendation {
        val remaining = remainingOf(goal, saved)
        if (remaining <= 0.0) return Recommendation(null, "Goal reached!")
        val left = daysLeft(goal)
        if (left == null) {
            return Recommendation(ceil(remaining / 30.0), "per day to finish in 30 days")
        }
        if (left < 0L) {
            return Recommendation(ceil(remaining), "to finish today (deadline passed)")
        }
        val days = left + 1L
        val label = if (days == 1L) "today" else "per day for $days days"
        return Recommendation(ceil(remaining / days.toDouble()), label)
    }

    fun streaks(deposits: List<Deposit>): Streaks {
        if (deposits.isEmpty()) return Streaks(0, 0)
        val days = deposits.map { dayOf(it.timestamp).toEpochDay() }.toSortedSet()

        var best = 0
        var run = 0
        var previous = Long.MIN_VALUE
        for (d in days) {
            run = if (previous != Long.MIN_VALUE && d == previous + 1L) run + 1 else 1
            if (run > best) best = run
            previous = d
        }

        val todayEpoch = today().toEpochDay()
        var cursor = if (days.contains(todayEpoch)) todayEpoch else todayEpoch - 1L
        var current = 0
        while (days.contains(cursor)) {
            current++
            cursor--
        }
        return Streaks(current, best)
    }

    /** Marks goals as completed / not completed depending on their saved amount. */
    fun normalize(data: AppData): AppData {
        val sums = HashMap<String, Double>()
        for (d in data.deposits) {
            sums[d.goalId] = (sums[d.goalId] ?: 0.0) + d.amount
        }
        val now = System.currentTimeMillis()
        val goals = data.goals.map { g ->
            val saved = g.startingAmount + (sums[g.id] ?: 0.0)
            val done = saved >= g.target - 0.000001
            if (done && g.completedAt == null) {
                g.copy(completedAt = now)
            } else if (!done && g.completedAt != null) {
                g.copy(completedAt = null)
            } else {
                g
            }
        }
        return data.copy(goals = goals)
    }
}

fun parseAmount(text: String): Double? {
    val value = text.trim().replace(",", "").toDoubleOrNull() ?: return null
    return if (value.isNaN() || value.isInfinite()) null else value
}

fun plainAmount(value: Double): String =
    if (value == floor(value) && abs(value) < 1e12) value.toLong().toString() else value.toString()

fun formatMoney(value: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale("en", "IN"))
    nf.maximumFractionDigits = 2
    nf.minimumFractionDigits = 0
    return "\u20B9" + nf.format(value)
}

fun shortAmount(value: Double): String {
    return when {
        value >= 100000.0 -> String.format(Locale.US, "%.1fL", value / 100000.0).replace(".0L", "L")
        value >= 1000.0 -> String.format(Locale.US, "%.1fk", value / 1000.0).replace(".0k", "k")
        else -> value.toLong().toString()
    }
}

fun formatPercent(progress: Float): String = "${floor(progress * 100f).toInt()}%"

fun formatDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))

fun formatEpochDay(epochDay: Long): String = formatDate(LocalDate.ofEpochDay(epochDay))

fun formatDateTime(timestamp: Long): String =
    Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", Locale.getDefault()))
