package me.crossevol.mobilemonitor.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import me.crossevol.mobilemonitor.model.AppUsageInfo

/**
 * RecyclerView adapter for displaying a list of app usage information.
 * Handles efficient list updates using DiffUtil for optimal performance.
 */
class AppUsageAdapter : RecyclerView.Adapter<AppUsageViewHolder>() {
    
    private var apps: List<AppUsageInfo> = emptyList()
    
    /**
     * Creates a new ViewHolder for app usage items.
     * 
     * @param parent The parent ViewGroup
     * @param viewType The view type (not used in this implementation)
     * @return A new AppUsageViewHolder instance
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppUsageViewHolder {
        return AppUsageViewHolder(parent.context)
    }
    
    /**
     * Binds app usage data to the ViewHolder at the specified position.
     * 
     * @param holder The ViewHolder to bind data to
     * @param position The position in the list
     */
    override fun onBindViewHolder(holder: AppUsageViewHolder, position: Int) {
        holder.bind(apps[position])
    }
    
    /**
     * Returns the total number of items in the list.
     * 
     * @return The size of the apps list
     */
    override fun getItemCount(): Int = apps.size
    
    /**
     * Updates the list of apps with efficient diff calculation.
     * Uses DiffUtil to calculate the minimal set of changes needed to update the list.
     * 
     * @param newApps The new list of app usage information
     */
    fun updateApps(newApps: List<AppUsageInfo>) {
        val diffCallback = AppUsageDiffCallback(apps, newApps)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        apps = newApps
        diffResult.dispatchUpdatesTo(this)
    }
    
    /**
     * DiffUtil callback for efficiently calculating differences between old and new app lists.
     * Compares items by package name and content to determine what changes are needed.
     */
    private class AppUsageDiffCallback(
        private val oldList: List<AppUsageInfo>,
        private val newList: List<AppUsageInfo>
    ) : DiffUtil.Callback() {
        
        override fun getOldListSize(): Int = oldList.size
        
        override fun getNewListSize(): Int = newList.size
        
        /**
         * Checks if two items represent the same app by comparing package names.
         */
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].packageName == newList[newItemPosition].packageName
        }
        
        /**
         * Checks if the content of two items is the same.
         * Compares all relevant fields to determine if the item needs to be updated.
         */
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            
            return oldItem.appName == newItem.appName &&
                    oldItem.lastTimeUsed == newItem.lastTimeUsed &&
                    oldItem.totalTimeInForeground == newItem.totalTimeInForeground &&
                    oldItem.icon == newItem.icon
        }
    }
}