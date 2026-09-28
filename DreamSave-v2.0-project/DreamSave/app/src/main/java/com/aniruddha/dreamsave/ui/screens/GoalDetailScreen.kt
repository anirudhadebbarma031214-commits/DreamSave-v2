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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aniruddha.dreamsave.data.Deposit
import com.aniruddha.dreamsave.data.Goal
import com.aniruddha.dreamsave.logic.Calc
import com.aniruddha.dreamsave.logic.MAX_AMOUNT
import com.aniruddha.dreamsave.logic.QUICK_AMOUNTS
import com.aniruddha.dreamsave.logic.formatDateTime
import com.aniruddha.dreamsave.logic.formatMoney
import com.aniruddha.dreamsave.logic.formatPercent
import com.aniruddha.dreamsave.logic.parseAmount
import com.aniruddha.dreamsave.ui.components.AnimatedMoneyText
import com.aniruddha.dreamsave.ui.components.BrandBrush
import com.aniruddha.dreamsave.ui.components.GlassCard
import com.aniruddha.dreamsave.ui.components.GradientButton
import com.aniruddha.dreamsave.ui.components.ProgressRing
import com.aniruddha.dreamsave.ui.components.SectionTitle
import com.aniruddha.dreamsave.ui.components.bouncyClick
import com.aniruddha.dreamsave.ui.theme.Cyan
import com.aniruddha.dreamsave.ui.theme.ElectricBlue
import com.aniruddha.dreamsave.ui.theme.Gold
import com.aniruddha.dreamsave.ui.theme.Violet

private enum class HistoryFilter(val label: String) {
    ALL("All"),
    WEEK("7 days"),
    MONTH("30 days")
}

@Composable
fun GoalDetailScreen(
    goal: Goal?,
    allDeposits: List<Deposit>,
    onBack: () -> Unit,
    onAddDeposit: (Double, String) -> Boolean,
    onDeleteDeposit: (String) -> Unit,
    onUpdateGoal: (String, Double, Double, Long?, String) -> Unit,
    onDeleteGoal: () -> Unit
) {
    var lastGoal by remember { mutableStateOf(goal) }
    SideEffect { if (goal != null) lastGoal = goal }
    // Only leave the screen if the goal existed and was then deleted (avoids a race right after creation).
    var seenGoal by remember { mutableStateOf(false) }
    LaunchedEffect(goal == null) {
        if (goal != null) seenGoal = true else if (seenGoal) onBack()
    }

    val shown = goal ?: lastGoal ?: return

    val deposits = remember(allDeposits, shown.id) {
        allDeposits.filter { it.goalId == shown.id }.sortedByDescending { it.timestamp }
    }
    val saved = Calc.savedOf(shown, deposits)
    val progress = Calc.progressOf(shown, saved)
    val remaining = Calc.remainingOf(shown, saved)
    val done = shown.completedAt != null
    val recommendation = Calc.recommendation(shown, saved)
    val streaks = remember(deposits) { Calc.streaks(deposits) }
    val savedToday = deposits.any { Calc.dayOf(it.timestamp) == Calc.today() }

    var customAmount by remember { mutableStateOf("") }
    var customNote by remember { mutableStateOf("") }
    var customError by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(HistoryFilter.ALL) }
    var showEdit by remember { mutableStateOf(false) }
    var showDeleteGoal by remember { mutableStateOf(false) }
    var depositToDelete by remember { mutableStateOf<Deposit?>(null) }

    val haptic = LocalHapticFeedback.current
    val addDeposit: (Double, String) -> Boolean = { amount, note ->
        val ok = onAddDeposit(amount, note)
        if (ok) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        ok
    }

    val filtered = remember(deposits, query, filter) {
        val today = Calc.today()
        val q = query.trim().lowercase()
        deposits.filter { d ->
            val day = Calc.dayOf(d.timestamp)
            val inRange = when (filter) {
                HistoryFilter.ALL -> true
                HistoryFilter.WEEK -> !day.isBefore(today.minusDays(6))
                HistoryFilter.MONTH -> !day.isBefore(today.minusDays(29))
            }
            val matches = q.isEmpty() ||
                d.note.lowercase().contains(q) ||
                formatMoney(d.amount).lowercase().contains(q) ||
                d.amount.toString().contains(q) ||
                formatDateTime(d.timestamp).lowercase().contains(q)
            inRange && matches
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "top") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = shown.name,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showEdit = true }) {
                    Icon(Icons.Rounded.Edit, contentDescription = "Edit goal")
                }
                IconButton(onClick = { showDeleteGoal = true }) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Delete goal", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        item(key = "hero") {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProgressRing(progress = progress, diameter = 132.dp, strokeWidth = 12.dp) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formatPercent(progress),
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                text = if (done) "complete" else "saved",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.width(18.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Saved",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        AnimatedMoneyText(value = saved, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Target",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(text = formatMoney(shown.target), style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Remaining",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatMoney(remaining),
                            style = MaterialTheme.typography.titleMedium,
                            color = Cyan
                        )
                    }
                }
                if (shown.description.isNotBlank()) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = shown.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Started with ${formatMoney(shown.startingAmount)}  \u2022  " +
                        if (done) "Completed" else Calc.deadlineLabel(shown),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item(key = "plan") {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Daily saving recommendation",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = recommendation.amount?.let { formatMoney(it) } ?: "\uD83C\uDF89 Done",
                    style = MaterialTheme.typography.headlineMedium.copy(brush = BrandBrush)
                )
                Text(
                    text = recommendation.caption,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item(key = "streaks") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                GlassCard(modifier = Modifier.weight(1f), cornerRadius = 20.dp, contentPadding = 16.dp) {
                    Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = Gold, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${streaks.current} ${if (streaks.current == 1) "day" else "days"}",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Current streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = when {
                            streaks.current == 0 -> "Save today to start one"
                            savedToday -> "Streak is alive"
                            else -> "Save today to extend it"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Cyan
                    )
                }
                GlassCard(modifier = Modifier.weight(1f), cornerRadius = 20.dp, contentPadding = 16.dp) {
                    Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = Violet, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${streaks.best} ${if (streaks.best == 1) "day" else "days"}",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Best streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item(key = "addMoney") {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Add money", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                for (rowAmounts in QUICK_AMOUNTS.chunked(4)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (amount in rowAmounts) {
                            QuickAmountChip(
                                amount = amount,
                                onClick = { addDeposit(amount.toDouble(), "") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        for (i in rowAmounts.size until 4) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = customAmount,
                    onValueChange = {
                        if (it.length <= 12 && it.all { c -> c.isDigit() || c == '.' }) {
                            customAmount = it
                            customError = false
                        }
                    },
                    label = { Text("Custom amount") },
                    prefix = { Text("\u20B9") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = customError,
                    supportingText = if (customError) {
                        { Text("Enter a valid amount") }
                    } else null,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = customNote,
                    onValueChange = { if (it.length <= 80) customNote = it },
                    label = { Text("Note (optional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                GradientButton(
                    text = "Add to goal",
                    icon = Icons.Rounded.Add,
                    onClick = {
                        val value = parseAmount(customAmount)
                        if (value == null || value <= 0.0 || value > MAX_AMOUNT) {
                            customError = true
                        } else if (addDeposit(value, customNote)) {
                            customAmount = ""
                            customNote = ""
                            customError = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item(key = "historyHeader") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle("Deposit history (${deposits.size})")
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search amount, note or date") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = if (query.isNotEmpty()) {
                        {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (option in HistoryFilter.values()) {
                        FilterChip(
                            selected = filter == option,
                            onClick = { filter = option },
                            label = { Text(option.label) }
                        )
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item(key = "historyEmpty") {
                GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
                    Text(
                        text = if (deposits.isEmpty()) "No deposits yet. Tap a quick amount above to make your first one."
                        else "No deposits match your search.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            items(filtered, key = { it.id }) { deposit ->
                DepositRow(
                    deposit = deposit,
                    onDelete = { depositToDelete = deposit },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }

    if (showEdit) {
        GoalFormDialog(
            initial = shown,
            onDismiss = { showEdit = false },
            onSave = { name, target, starting, deadline, description ->
                showEdit = false
                onUpdateGoal(name, target, starting, deadline, description)
            }
        )
    }

    if (showDeleteGoal) {
        AlertDialog(
            onDismissRequest = { showDeleteGoal = false },
            title = { Text("Delete this goal?") },
            text = { Text("\"${shown.name}\" and all of its deposits will be permanently removed.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteGoal = false
                    onDeleteGoal()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteGoal = false }) { Text("Cancel") }
            }
        )
    }

    val pending = depositToDelete
    if (pending != null) {
        AlertDialog(
            onDismissRequest = { depositToDelete = null },
            title = { Text("Delete this deposit?") },
            text = { Text("${formatMoney(pending.amount)} from ${formatDateTime(pending.timestamp)} will be removed and your progress will update.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteDeposit(pending.id)
                    depositToDelete = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { depositToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun QuickAmountChip(amount: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .bouncyClick(onClick)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(ElectricBlue.copy(alpha = 0.28f), Violet.copy(alpha = 0.28f))
                )
            )
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "\u20B9$amount",
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1
        )
    }
}

@Composable
private fun DepositRow(deposit: Deposit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier.fillMaxWidth(), cornerRadius = 18.dp, contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(ElectricBlue.copy(alpha = 0.35f), Violet.copy(alpha = 0.35f)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = Cyan, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "+" + formatMoney(deposit.amount),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = formatDateTime(deposit.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (deposit.note.isNotBlank()) {
                    Text(
                        text = deposit.note,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Delete deposit",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
