package com.aniruddha.dreamsave.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aniruddha.dreamsave.data.AppData
import com.aniruddha.dreamsave.data.Goal
import com.aniruddha.dreamsave.logic.Calc
import com.aniruddha.dreamsave.logic.formatMoney
import com.aniruddha.dreamsave.logic.formatPercent
import com.aniruddha.dreamsave.ui.components.AnimatedMoneyText
import com.aniruddha.dreamsave.ui.components.AnimatedProgressBar
import com.aniruddha.dreamsave.ui.components.BrandBrush
import com.aniruddha.dreamsave.ui.components.GlassCard
import com.aniruddha.dreamsave.ui.components.GradientButton
import com.aniruddha.dreamsave.ui.components.SectionTitle
import com.aniruddha.dreamsave.ui.theme.Cyan
import com.aniruddha.dreamsave.ui.theme.Gold
import java.time.LocalTime

@Composable
fun HomeScreen(
    data: AppData,
    onOpenGoal: (String) -> Unit,
    onCreateGoal: (String, Double, Double, Long?, String) -> Unit
) {
    var showForm by remember { mutableStateOf(false) }

    val savedByGoal = remember(data.goals, data.deposits) {
        data.goals.associate { it.id to Calc.savedOf(it, data.deposits) }
    }
    val totalSaved = savedByGoal.values.sum()
    val totalTarget = data.goals.sumOf { it.target }
    val overall = if (totalTarget > 0.0) (totalSaved / totalTarget).coerceIn(0.0, 1.0).toFloat() else 0f
    val streaks = remember(data.deposits) { Calc.streaks(data.deposits) }
    val completed = data.goals.count { it.completedAt != null }
    val sortedGoals = remember(data.goals) { data.goals.sortedBy { it.completedAt != null } }

    val hour = LocalTime.now().hour
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(key = "header") {
                Column {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "DreamSave",
                        style = MaterialTheme.typography.headlineLarge.copy(brush = BrandBrush)
                    )
                }
            }

            item(key = "summary") {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Total saved",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AnimatedMoneyText(value = totalSaved, style = MaterialTheme.typography.headlineLarge)
                    Text(
                        text = if (data.goals.isEmpty()) "Create a goal to get started"
                        else "of ${formatMoney(totalTarget)} across ${data.goals.size} goal" + if (data.goals.size == 1) "" else "s",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))
                    AnimatedProgressBar(progress = overall, modifier = Modifier.fillMaxWidth(), height = 12.dp)
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = Gold, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "${streaks.current}-day streak",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = Cyan, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "$completed completed",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            item(key = "goalsTitle") {
                SectionTitle("Your goals")
            }

            if (sortedGoals.isEmpty()) {
                item(key = "empty") {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Rounded.Savings,
                                contentDescription = null,
                                tint = Cyan,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "No goals yet",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Tap \"New goal\" and start saving for something that matters to you.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(sortedGoals, key = { it.id }) { goal ->
                    GoalCard(
                        goal = goal,
                        saved = savedByGoal[goal.id] ?: 0.0,
                        onClick = { onOpenGoal(goal.id) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }

        GradientButton(
            text = "New goal",
            onClick = { showForm = true },
            icon = Icons.Rounded.Add,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        )
    }

    if (showForm) {
        GoalFormDialog(
            initial = null,
            onDismiss = { showForm = false },
            onSave = { name, target, starting, deadline, description ->
                showForm = false
                onCreateGoal(name, target, starting, deadline, description)
            }
        )
    }
}

@Composable
private fun GoalCard(goal: Goal, saved: Double, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val progress = Calc.progressOf(goal, saved)
    val done = goal.completedAt != null
    GlassCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${formatMoney(saved)} of ${formatMoney(goal.target)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(12.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (done) Color(0x333DDC97) else Color(0x332E8BFF))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (done) {
                    Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    text = formatPercent(progress),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        AnimatedProgressBar(progress = progress, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = if (done) "Goal reached" else "${formatMoney(Calc.remainingOf(goal, saved))} to go",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (done) "Completed" else Calc.deadlineLabel(goal),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
