package me.crossevol.mobilemonitor.model

/**
 * Enum representing days of the week with numeric values
 * 
 * @param value Numeric representation (1-7 for Monday-Sunday)
 */
enum class DayOfWeek(val value: Int) {
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6),
    SUNDAY(7);
    
    companion object {
        fun fromValue(value: Int): DayOfWeek? {
            return values().find { it.value == value }
        }
    }
}
