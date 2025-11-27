package me.crossevol.mobilemonitor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.crossevol.mobilemonitor.model.DayOfWeek
import me.crossevol.mobilemonitor.model.DayPattern
import me.crossevol.mobilemonitor.repository.AppRestrictionRepository
import me.crossevol.mobilemonitor.utils.RulePatternExpander
import java.time.LocalTime

/**
 * UI state for the add rule form
 *
 * @param dayPattern Selected day pattern (WORKDAY, WEEKEND, or CUSTOM)
 * @param selectedDays Set of selected days (used for CUSTOM pattern)
 * @param timeRangeStart Start time of restriction period
 * @param timeRangeEnd End time of restriction period
 * @param totalTime Maximum allowed usage time in minutes
 * @param totalCount Maximum allowed access count
 * @param isValid Whether the form is valid and can be saved
 * @param errorMessage Error message if validation fails
 * @param isSaving Whether the form is currently being saved
 * @param saveSuccess Whether the save operation completed successfully
 */
data class RuleFormState(
    val dayPattern: DayPattern = DayPattern.WORKDAY,
    val selectedDays: Set<DayOfWeek> = emptySet(),
    val timeRangeStart: LocalTime = LocalTime.of(
        9,
        0
    ),
    val timeRangeEnd: LocalTime = LocalTime.of(
        17,
        0
    ),
    val totalTime: Int = 0,
    val totalCount: Int = 0,
    val isValid: Boolean = true,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

/**
 * ViewModel for the add rule screen
 * Manages form state and handles rule creation with pattern expansion
 *
 * @param appId The ID of the app to create rules for
 * @param repository Repository for app restriction operations
 */
class AddRuleViewModel(
    private val appId: Long,
    private val repository: AppRestrictionRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(RuleFormState())
    val formState: StateFlow<RuleFormState> = _formState.asStateFlow()

    /**
     * Update the selected day pattern
     *
     * @param pattern New day pattern
     */
    fun updateDayPattern(pattern: DayPattern) {
        _formState.value = _formState.value.copy(
            dayPattern = pattern,
            // Clear selected days when switching away from custom
            selectedDays = if (pattern == DayPattern.CUSTOM) _formState.value.selectedDays else emptySet()
        )
        validateForm()
    }

    /**
     * Toggle a day in the custom day selection
     *
     * @param day Day to toggle
     */
    fun toggleDay(day: DayOfWeek) {
        val currentDays = _formState.value.selectedDays
        val newDays = if (currentDays.contains(day)) {
            currentDays - day
        } else {
            currentDays + day
        }

        _formState.value = _formState.value.copy(selectedDays = newDays)
        validateForm()
    }

    /**
     * Update the start time of the restriction period
     *
     * @param time New start time
     */
    fun updateTimeRangeStart(time: LocalTime) {
        _formState.value = _formState.value.copy(timeRangeStart = time)
        validateForm()
    }

    /**
     * Update the end time of the restriction period
     *
     * @param time New end time
     */
    fun updateTimeRangeEnd(time: LocalTime) {
        _formState.value = _formState.value.copy(timeRangeEnd = time)
        validateForm()
    }

    /**
     * Update the total time restriction
     *
     * @param time Total time in minutes
     */
    fun updateTotalTime(time: Int) {
        _formState.value = _formState.value.copy(totalTime = time.coerceAtLeast(0))
        validateForm()
    }

    /**
     * Update the total count restriction
     *
     * @param count Total access count
     */
    fun updateTotalCount(count: Int) {
        _formState.value = _formState.value.copy(totalCount = count.coerceAtLeast(0))
        validateForm()
    }

    /**
     * Validate the form and update validation state
     */
    private fun validateForm() {
        val state = _formState.value

        // Validate day pattern and selected days
        val isDayPatternValid = RulePatternExpander.validateRuleInput(
            pattern = state.dayPattern,
            selectedDays = state.selectedDays
        )

        // Validate time range (end time should be after start time)
        val isTimeRangeValid = state.timeRangeEnd.isAfter(state.timeRangeStart)

        // Validate that values are non-negative
        val areValuesValid = state.totalTime >= 0 && state.totalCount >= 0

        val isValid = isDayPatternValid && isTimeRangeValid && areValuesValid
        val errorMessage = when {
            !isDayPatternValid -> "Please select at least one day for custom pattern"
            !isTimeRangeValid  -> "End time must be after start time"
            !areValuesValid    -> "Time and count must be non-negative"
            else               -> null
        }

        _formState.value = state.copy(
            isValid = isValid,
            errorMessage = errorMessage
        )
    }

    /**
     * Save the rule with pattern expansion
     * Expands the selected pattern into individual rules and saves them to the database
     */
    fun saveRule() {
        if (!_formState.value.isValid) {
            _formState.value = _formState.value.copy(
                errorMessage = "Please fix validation errors before saving"
            )
            return
        }

        viewModelScope.launch {
            try {
                _formState.value = _formState.value.copy(
                    isSaving = true,
                    errorMessage = null
                )

                val state = _formState.value

                // Expand pattern into individual rules
                val rules = RulePatternExpander.expandPattern(
                    pattern = state.dayPattern,
                    selectedDays = state.selectedDays,
                    timeRangeStart = state.timeRangeStart,
                    timeRangeEnd = state.timeRangeEnd,
                    totalTime = state.totalTime,
                    totalCount = state.totalCount,
                    appInfoId = appId
                )

                if (rules.isEmpty()) {
                    throw IllegalStateException("No rules were generated from the pattern")
                }

                // Save all rules to database
                repository.saveRules(rules)

                // Notify monitoring service to reload rules
                // Note: Service notification will be handled by the repository implementation

                _formState.value = _formState.value.copy(
                    isSaving = false,
                    saveSuccess = true
                )
            } catch (e: IllegalStateException) {
                _formState.value = _formState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Invalid rule configuration"
                )
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    isSaving = false,
                    errorMessage = "Failed to save rule: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
}
