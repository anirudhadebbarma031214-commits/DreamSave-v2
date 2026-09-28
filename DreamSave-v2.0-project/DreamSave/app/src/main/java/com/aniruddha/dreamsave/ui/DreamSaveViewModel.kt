package com.aniruddha.dreamsave.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aniruddha.dreamsave.data.AppData
import com.aniruddha.dreamsave.data.DataRepository
import com.aniruddha.dreamsave.data.Deposit
import com.aniruddha.dreamsave.data.Goal
import com.aniruddha.dreamsave.data.ThemeMode
import com.aniruddha.dreamsave.logic.Calc
import com.aniruddha.dreamsave.logic.MAX_AMOUNT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class DreamSaveViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DataRepository(application)
    private val saveMutex = Mutex()

    private val _data = MutableStateFlow(Calc.normalize(repository.load()))
    val data: StateFlow<AppData> = _data.asStateFlow()

    private val _celebration = MutableStateFlow<String?>(null)
    val celebration: StateFlow<String?> = _celebration.asStateFlow()

    private fun commit(newData: AppData) {
        _data.value = newData
        viewModelScope.launch(Dispatchers.IO) {
            saveMutex.withLock {
                repository.save(_data.value)
            }
        }
    }

    fun addGoal(
        name: String,
        target: Double,
        starting: Double,
        deadlineEpochDay: Long?,
        description: String
    ): String {
        val goal = Goal(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            target = target,
            startingAmount = starting,
            deadlineEpochDay = deadlineEpochDay,
            description = description.trim(),
            createdAt = System.currentTimeMillis(),
            completedAt = null
        )
        val current = _data.value
        commit(Calc.normalize(current.copy(goals = current.goals + goal)))
        return goal.id
    }

    fun updateGoal(
        id: String,
        name: String,
        target: Double,
        starting: Double,
        deadlineEpochDay: Long?,
        description: String
    ) {
        val current = _data.value
        val goals = current.goals.map { g ->
            if (g.id == id) {
                g.copy(
                    name = name.trim(),
                    target = target,
                    startingAmount = starting,
                    deadlineEpochDay = deadlineEpochDay,
                    description = description.trim()
                )
            } else {
                g
            }
        }
        commit(Calc.normalize(current.copy(goals = goals)))
    }

    fun deleteGoal(id: String) {
        val current = _data.value
        commit(
            current.copy(
                goals = current.goals.filter { it.id != id },
                deposits = current.deposits.filter { it.goalId != id }
            )
        )
    }

    /** Returns true if the deposit was stored. */
    fun addDeposit(goalId: String, amount: Double, note: String): Boolean {
        if (amount.isNaN() || amount.isInfinite() || amount <= 0.0 || amount > MAX_AMOUNT) return false
        val current = _data.value
        val before = current.goals.firstOrNull { it.id == goalId } ?: return false
        val deposit = Deposit(
            id = UUID.randomUUID().toString(),
            goalId = goalId,
            amount = amount,
            timestamp = System.currentTimeMillis(),
            note = note.trim()
        )
        val next = Calc.normalize(current.copy(deposits = current.deposits + deposit))
        val after = next.goals.firstOrNull { it.id == goalId }
        commit(next)
        if (before.completedAt == null && after?.completedAt != null && next.settings.celebrations) {
            _celebration.value = goalId
        }
        return true
    }

    fun deleteDeposit(id: String) {
        val current = _data.value
        commit(Calc.normalize(current.copy(deposits = current.deposits.filter { it.id != id })))
    }

    fun dismissCelebration() {
        _celebration.value = null
    }

    fun setThemeMode(mode: ThemeMode) {
        val current = _data.value
        commit(current.copy(settings = current.settings.copy(themeMode = mode)))
    }

    fun setCelebrations(enabled: Boolean) {
        val current = _data.value
        commit(current.copy(settings = current.settings.copy(celebrations = enabled)))
    }

    fun resetAllData() {
        val current = _data.value
        _celebration.value = null
        commit(AppData(settings = current.settings))
    }
}
