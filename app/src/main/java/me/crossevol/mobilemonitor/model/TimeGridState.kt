package me.crossevol.mobilemonitor.model

/**
 * Data class representing the state of the time grid visualization
 * Contains a 7x24 matrix indicating rule coverage for each day and hour
 * 
 * @param coverage 2D array where coverage[day][hour] indicates if that time slot has rule coverage
 *                 - day: 0-6 representing Monday through Sunday
 *                 - hour: 0-23 representing 00:00 through 23:00
 */
data class TimeGridState(
    val coverage: Array<BooleanArray> = Array(7) { BooleanArray(24) { false } }
) {
    
    /**
     * Check if a specific day and hour has rule coverage
     * 
     * @param dayIndex Day index (0-6 for Monday-Sunday)
     * @param hour Hour (0-23)
     * @return true if the time slot has rule coverage, false otherwise
     */
    fun hasRuleCoverage(dayIndex: Int, hour: Int): Boolean {
        return if (dayIndex in 0..6 && hour in 0..23) {
            coverage[dayIndex][hour]
        } else {
            false
        }
    }
    
    /**
     * Set rule coverage for a specific day and hour
     * 
     * @param dayIndex Day index (0-6 for Monday-Sunday)
     * @param hour Hour (0-23)
     * @param hasCoverage Whether this time slot has rule coverage
     */
    fun setRuleCoverage(dayIndex: Int, hour: Int, hasCoverage: Boolean) {
        if (dayIndex in 0..6 && hour in 0..23) {
            coverage[dayIndex][hour] = hasCoverage
        }
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as TimeGridState
        
        return coverage.contentDeepEquals(other.coverage)
    }
    
    override fun hashCode(): Int {
        return coverage.contentDeepHashCode()
    }
}