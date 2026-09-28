package com.aniruddha.dreamsave.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aniruddha.dreamsave.data.Goal
import com.aniruddha.dreamsave.logic.MAX_AMOUNT
import com.aniruddha.dreamsave.logic.formatEpochDay
import com.aniruddha.dreamsave.logic.parseAmount
import com.aniruddha.dreamsave.logic.plainAmount
import com.aniruddha.dreamsave.ui.components.GradientButton
import java.time.LocalDate

private fun isAmountInput(text: String): Boolean =
    text.length <= 12 && text.all { it.isDigit() || it == '.' }

@Composable
fun GoalFormDialog(
    initial: Goal?,
    onDismiss: () -> Unit,
    onSave: (String, Double, Double, Long?, String) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var target by remember { mutableStateOf(if (initial != null) plainAmount(initial.target) else "") }
    var starting by remember { mutableStateOf(if (initial != null) plainAmount(initial.startingAmount) else "0") }
    var deadline by remember { mutableStateOf(initial?.deadlineEpochDay) }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var showPicker by remember { mutableStateOf(false) }
    var attempted by remember { mutableStateOf(false) }

    val targetValue = parseAmount(target)
    val startingValue = if (starting.isBlank()) 0.0 else parseAmount(starting)

    val nameError = name.isBlank()
    val targetError = targetValue == null || targetValue <= 0.0 || targetValue > MAX_AMOUNT
    val startingError = startingValue == null || startingValue < 0.0 ||
        (targetValue != null && startingValue >= targetValue)

    val submit: () -> Unit = {
        attempted = true
        if (!nameError && !targetError && !startingError) {
            onSave(name.trim(), targetValue ?: 0.0, startingValue ?: 0.0, deadline, description.trim())
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .heightIn(max = 660.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (initial == null) "New goal" else "Edit goal",
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 60) name = it },
                    label = { Text("Goal name") },
                    singleLine = true,
                    isError = attempted && nameError,
                    supportingText = if (attempted && nameError) {
                        { Text("Please enter a name") }
                    } else null,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = target,
                    onValueChange = { if (isAmountInput(it)) target = it },
                    label = { Text("Target amount") },
                    prefix = { Text("\u20B9") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = attempted && targetError,
                    supportingText = if (attempted && targetError) {
                        { Text("Enter an amount greater than 0") }
                    } else null,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = starting,
                    onValueChange = { if (isAmountInput(it)) starting = it },
                    label = { Text("Starting amount") },
                    prefix = { Text("\u20B9") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = attempted && startingError,
                    supportingText = if (attempted && startingError) {
                        { Text("Must be below the target amount") }
                    } else null,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { showPicker = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Rounded.Flag, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (deadline == null) "Deadline (optional)" else formatEpochDay(deadline ?: 0L)
                        )
                    }
                    if (deadline != null) {
                        IconButton(onClick = { deadline = null }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear deadline")
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= 200) description = it },
                    label = { Text("Description (optional)") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    GradientButton(
                        text = if (initial == null) "Create goal" else "Save changes",
                        onClick = submit,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    if (showPicker) {
        DeadlinePicker(
            initialEpochDay = deadline,
            onPicked = { deadline = it; showPicker = false },
            onDismiss = { showPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeadlinePicker(initialEpochDay: Long?, onPicked: (Long) -> Unit, onDismiss: () -> Unit) {
    val millisPerDay = 86_400_000L
    val todayEpoch = LocalDate.now().toEpochDay()
    val state = rememberDatePickerState(
        initialSelectedDateMillis = (initialEpochDay ?: (todayEpoch + 30L)) * millisPerDay,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis / millisPerDay >= todayEpoch
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selected = state.selectedDateMillis
                    if (selected != null) onPicked(selected / millisPerDay) else onDismiss()
                }
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = state)
    }
}
