package com.aniruddha.dreamsave.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aniruddha.dreamsave.data.AppData
import com.aniruddha.dreamsave.logic.Calc
import com.aniruddha.dreamsave.logic.formatMoney
import com.aniruddha.dreamsave.logic.formatPercent
import com.aniruddha.dreamsave.logic.shortAmount
import com.aniruddha.dreamsave.ui.components.AnimatedMoneyText
import com.aniruddha.dreamsave.ui.components.AnimatedProgressBar
import com.aniruddha.dreamsave.ui.components.BrandBrush
import com.aniruddha.dreamsave.ui.components.GlassCard
import com.aniruddha.dreamsave.ui.components.SectionTitle
import com.aniruddha.dreamsave.ui.components.StatTile
import com.aniruddha.dreamsave.ui.theme.Cyan
import com.aniruddha.dreamsave.ui.theme.ElectricBlue
import com.aniruddha.dreamsave.ui.theme.Gold
import com.aniruddha.dreamsave.ui.theme.Mint
import com.aniruddha.dreamsave.ui.theme.Violet
import java.time.LocalDate
import java.util.Locale

@Composable
fun StatsScreen(data: AppData) {
    val today = Calc.today()
    val deposits = data.deposits

    val totalSaved = data.goals.sumOf { Calc.savedOf(it, deposits) }
    val totalTarget = data.goals.sumOf { it.target }
    val completed = data.goals.count { it.completedAt != null }
    val streaks = remember(deposits) { Calc.streaks(deposits) }
    val depositCount = deposits.size
    val average = if (depositCount == 0) 0.0 else deposits.sumOf { it.amount } / depositCount
    val biggest = deposits.maxOfOrNull { it.amount } ?: 0.0
    val activeDays = deposits.map { Calc.dayOf(it.timestamp) }.toSet().size
    val monthTotal = deposits
        .filter {
            val d = Calc.dayOf(it.timestamp)
            d.year == today.year && d.month == today.month
        }
        .sumOf { it.amount }

    val week: List<Pair<LocalDate, Double>> = (6 downTo 0).map { back ->
        val day = today.minusDays(back.toLong())
        day to deposits.filter { Calc.dayOf(it.timestamp) == day }.sumOf { it.amount }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "title") {
            Column {
                Text(
                    text = "Insights",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Statistics",
                    style = MaterialTheme.typography.headlineLarge.copy(brush = BrandBrush)
                )
            }
        }

        item(key = "overview") {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Total saved",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AnimatedMoneyText(value = totalSaved, style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(12.dp))
                val overall = if (totalTarget > 0.0) (totalSaved / totalTarget).coerceIn(0.0, 1.0).toFloat() else 0f
                AnimatedProgressBar(progress = overall, modifier = Modifier.fillMaxWidth(), height = 12.dp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${formatPercent(overall)} of ${formatMoney(totalTarget)} total target",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item(key = "tiles1") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatTile(
                    label = "Goals completed",
                    value = "$completed / ${data.goals.size}",
                    icon = Icons.Rounded.EmojiEvents,
                    accent = Gold,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "Total deposits",
                    value = depositCount.toString(),
                    icon = Icons.Rounded.Savings,
                    accent = Cyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item(key = "tiles2") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatTile(
                    label = "Current streak",
                    value = "${streaks.current} d",
                    icon = Icons.Rounded.LocalFireDepartment,
                    accent = Gold,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "Best streak",
                    value = "${streaks.best} d",
                    icon = Icons.Rounded.LocalFireDepartment,
                    accent = Violet,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item(key = "chart") {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Last 7 days", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                val maxValue = week.maxOf { it.second }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    for ((day, value) in week) {
                        val fraction = if (maxValue > 0.0) (value / maxValue).toFloat() else 0f
                        DayBar(
                            label = day.dayOfWeek
                                .getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())
                                .take(3),
                            amountLabel = if (value > 0.0) shortAmount(value) else "",
                            fraction = fraction,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item(key = "averages") {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Saving habits", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))
                StatLine("Average deposit", formatMoney(average))
                StatLine("Biggest deposit", formatMoney(biggest))
                StatLine("This month", formatMoney(monthTotal))
                StatLine("Days with savings", activeDays.toString())
            }
        }

        item(key = "goalsTitle") {
            SectionTitle("Goal breakdown")
        }

        if (data.goals.isEmpty()) {
            item(key = "noGoals") {
                GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
                    Text(
                        text = "Create a goal to see its progress here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            for (goal in data.goals) {
                item(key = "goal-" + goal.id) {
                    val saved = Calc.savedOf(goal, deposits)
                    val progress = Calc.progressOf(goal, saved)
                    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp, contentPadding = 16.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = goal.name,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = formatPercent(progress),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (goal.completedAt != null) Mint else ElectricBlue
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        AnimatedProgressBar(progress = progress, modifier = Modifier.fillMaxWidth(), height = 8.dp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "${formatMoney(saved)} of ${formatMoney(goal.target)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun DayBar(label: String, amountLabel: String, fraction: Float, modifier: Modifier = Modifier) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val maxBar: Dp = 110.dp
    val barHeight by animateDpAsState(
        targetValue = if (started) maxOf(4.dp, maxBar * fraction) else 4.dp,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "dayBar"
    )
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = amountLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(20.dp)
                .height(barHeight)
                .clip(RoundedCornerShape(8.dp))
                .background(BrandBrush)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
