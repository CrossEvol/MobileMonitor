package me.crossevol.mobilemonitor.model

/**
 * Enum representing the view mode for the app detail screen
 * Used to toggle between rule list view and time grid view
 */
enum class ViewMode {
    /**
     * Traditional list view showing rules in a vertical list format
     */
    LIST,
    
    /**
     * Visual grid view showing rules as a 7x24 heatmap
     */
    GRID
}