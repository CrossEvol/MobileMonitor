package me.crossevol.mobilemonitor.ui

import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import me.crossevol.mobilemonitor.R
import me.crossevol.mobilemonitor.model.AppUsageInfo
import me.crossevol.mobilemonitor.utils.TimeFormatter
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewHolder for displaying app usage information in a RecyclerView.
 * Handles binding of app data to UI components including app icon, name, usage time, and last used time.
 * Creates UI programmatically without XML layouts.
 */
class AppUsageViewHolder(context: Context) : RecyclerView.ViewHolder(createItemView(context)) {
    
    private val ivAppIcon: ImageView
    private val tvAppName: TextView
    private val tvLastUsed: TextView
    private val tvUsageTime: TextView
    
    companion object {
        /**
         * Creates the item view programmatically using Kotlin code instead of XML layout.
         */
        private fun createItemView(context: Context): LinearLayout {
            return LinearLayout(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                val padding = dpToPx(context, 16)
                setPadding(padding, padding, padding, padding)
            }
        }
        
        private fun dpToPx(context: Context, dp: Int): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                context.resources.displayMetrics
            ).toInt()
        }
    }
    
    init {
        val context = itemView.context
        val rootLayout = itemView as LinearLayout
        
        // Create app icon ImageView
        ivAppIcon = ImageView(context).apply {
            val iconSize = dpToPx(context, 48)
            layoutParams = LinearLayout.LayoutParams(iconSize, iconSize).apply {
                marginEnd = dpToPx(context, 16)
            }
            contentDescription = context.getString(R.string.app_icon_description)
        }
        rootLayout.addView(ivAppIcon)
        
        // Create text container LinearLayout
        val textContainer = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            orientation = LinearLayout.VERTICAL
        }
        
        // Create app name TextView
        tvAppName = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
        }
        textContainer.addView(tvAppName)
        
        // Create last used TextView
        tvLastUsed = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(context, 4)
            }
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(getThemeColor(context, android.R.attr.textColorSecondary))
        }
        textContainer.addView(tvLastUsed)
        
        rootLayout.addView(textContainer)
        
        // Create usage time TextView
        tvUsageTime = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
        }
        rootLayout.addView(tvUsageTime)
    }
    
    private fun getThemeColor(context: Context, attr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return ContextCompat.getColor(context, typedValue.resourceId)
    }
    
    /**
     * Binds app usage information to the view components.
     * 
     * @param appUsageInfo The app usage data to display
     */
    fun bind(appUsageInfo: AppUsageInfo) {
        // Set app name
        tvAppName.text = appUsageInfo.appName
        
        // Set app icon, use default if not available
        if (appUsageInfo.icon != null) {
            ivAppIcon.setImageDrawable(appUsageInfo.icon)
        } else {
            ivAppIcon.setImageResource(R.mipmap.ic_launcher)
        }
        
        // Format and set usage time
        tvUsageTime.text = TimeFormatter.formatUsageTime(appUsageInfo.totalTimeInForeground)
        
        // Format and set last used time
        tvLastUsed.text = formatLastUsedTime(appUsageInfo.lastTimeUsed)
    }
    
    /**
     * Formats the last used timestamp into a human-readable string.
     * 
     * @param lastTimeUsed Timestamp in milliseconds when the app was last used
     * @return Formatted string showing when the app was last used
     */
    private fun formatLastUsedTime(lastTimeUsed: Long): String {
        if (lastTimeUsed <= 0) {
            return itemView.context.getString(R.string.never_used)
        }
        
        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - lastTimeUsed
        
        return when {
            // Less than 1 minute ago
            timeDifference < 60_000 -> "Just now"
            
            // Less than 1 hour ago
            timeDifference < 3_600_000 -> {
                val minutes = timeDifference / 60_000
                "$minutes minute${if (minutes != 1L) "s" else ""} ago"
            }
            
            // Less than 24 hours ago
            timeDifference < 86_400_000 -> {
                val hours = timeDifference / 3_600_000
                "$hours hour${if (hours != 1L) "s" else ""} ago"
            }
            
            // Less than 7 days ago
            timeDifference < 604_800_000 -> {
                val days = timeDifference / 86_400_000
                "$days day${if (days != 1L) "s" else ""} ago"
            }
            
            // More than 7 days ago - show actual date
            else -> {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                itemView.context.getString(R.string.last_used_format, dateFormat.format(Date(lastTimeUsed)))
            }
        }
    }
}