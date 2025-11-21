package me.crossevol.mobilemonitor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.crossevol.mobilemonitor.model.AppRule
import me.crossevol.mobilemonitor.repository.AppRestrictionRepository
import java.time.LocalTime

/**
 * UI state for the edit rule form
 * 
 * @param rule The rule being edited (null if loading)
 * @param timeRangeStart Start time of restriction period
 * @param timeRangeEnd End time of restriction period
 * @param totalTime Maximum allowed usage time in minutes
 * @param totalCount Maximum allowed access count
 * @param isValid Whether the form is valid and can be saved
 * @param errorMessage Error message if validation fails
 * @param isLoading Whether the rule is being loaded
 * @param isSaving Whether the form is currently being saved
 * @param saveSuccess Whether the save operation completed successfully
 */
data class EditRuleFormState(
    val rule: AppRule? = null,
    val timeRangeStart: LocalTime = LocalTime.of(9, 0),
    val timeRangeEnd: LocalTime = LocalTime.of(17, 0),
    val totalTime: Int = 0,
    val totalCount: Int = 0,
    val isValid: Boolean = true,
    val errorMessage: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

/**
 * ViewModel for the edit rule screen
 * Manages form state and handles rule updates
 * 
 * @param ruleId The ID of the rule to edit
 * @param repository Repository for app restriction operations
 */
class EditRuleViewModel(
    private val ruleId: Long,
    private val repository: AppRestrictionRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(EditRuleFormState())
    val formState: StateFlow<EditRuleFormState> = _formState.asStateFlow()

    init {
        loadRule()
    }

    /**
     * Load the existing rule from the database
     */
    private fun loadRule() {
        viewModelScope.launch {
            try {
                _formState.value = _formState.value.copy(isLoading = true, errorMessage = null)
                
                val rule = repository.getRuleById(ruleId)
                
                if (rule != null) {
                    _formState.value = EditRuleFormState(
                        rule = rule,
                        timeRangeStart = rule.timeRangeStart,
                        timeRangeEnd = rule.timeRangeEnd,
                        totalTime = rule.totalTime,
                        totalCount = rule.totalCount,
                        isLoading = false,
                        isValid = true
                    )
                } else {
                    _formState.value = _formState.value.copy(
                        isLoading = false,
                        errorMessage = "Rule not found"
                    )
                }
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load rule"
                )
            }
        }
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
        
        // Validate time range (end time should be after start time)
        val isTimeRangeValid = state.timeRangeEnd.isAfter(state.timeRangeStart)
        
        // Validate that values are non-negative
        val areValuesValid = state.totalTime >= 0 && state.totalCount >= 0
        
        val isValid = isTimeRangeValid && areValuesValid
        val errorMessage = when {
            !isTimeRangeValid -> "End time must be after start time"
            !areValuesValid -> "Time and count must be non-negative"
            else -> null
        }
        
        _formState.value = state.copy(
            isValid = isValid,
            errorMessage = errorMessage
        )
    }

    /**
     * Save the updated rule to the database
     */
    fun saveRule() {
        val currentRule = _formState.value.rule
        if (!_formState.value.isValid || currentRule == null) {
            return
        }
        
        viewModelScope.launch {
            try {
                _formState.value = _formState.value.copy(isSaving = true, errorMessage = null)
                
                val state = _formState.value
                
                // Create updated rule with new values
                val updatedRule = currentRule.copy(
                    timeRangeStart = state.timeRangeStart,
                    timeRangeEnd = state.timeRangeEnd,
                    totalTime = state.totalTime,
                    totalCount = state.totalCount
                )
                
                // Update rule in database
                repository.updateRule(updatedRule)
                
                // Notify monitoring service to reload rules
                // Note: Service notification will be handled by the repository implementation
                
                _formState.value = _formState.value.copy(
                    isSaving = false,
                    saveSuccess = true
                )
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Failed to save rule"
                )
            }
        }
    }
}
