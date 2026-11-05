package com.adioquino.nothingcalendar.widget.render

import android.appwidget.AppWidgetManager
import android.os.Build
import android.os.Bundle
import android.util.SizeF
import androidx.core.os.BundleCompat

object WidgetSizeResolver {
    fun resolve(
        options: Bundle,
        defaultSize: WidgetSize,
    ): List<WidgetSize> {
        val providedSizes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            BundleCompat.getParcelableArrayList(
                options,
                AppWidgetManager.OPTION_APPWIDGET_SIZES,
                SizeF::class.java,
            ).orEmpty().map { WidgetSize(it.width.toInt(), it.height.toInt()) }
        } else {
            emptyList()
        }
        return resolveBounds(
            minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, defaultSize.widthDp),
            minHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, defaultSize.heightDp),
            maxWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, defaultSize.widthDp),
            maxHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, defaultSize.heightDp),
            providedSizes = providedSizes,
            defaultSize = defaultSize,
        )
    }

    fun resolveBounds(
        minWidthDp: Int,
        minHeightDp: Int,
        maxWidthDp: Int,
        maxHeightDp: Int,
        providedSizes: List<WidgetSize>,
        defaultSize: WidgetSize,
    ): List<WidgetSize> {
        val validProvided = providedSizes.filter(WidgetSize::isValid).distinct()
        if (validProvided.isNotEmpty()) return validProvided

        val minWidth = minWidthDp.takeIf { it > 0 } ?: defaultSize.widthDp
        val minHeight = minHeightDp.takeIf { it > 0 } ?: defaultSize.heightDp
        val maxWidth = maxWidthDp.takeIf { it >= minWidth } ?: minWidth
        val maxHeight = maxHeightDp.takeIf { it >= minHeight } ?: minHeight
        // Pre-API 31 launchers do not expose the current orientation. The minimum pair is the
        // safest single fallback because it cannot reserve more content than the live widget fits.
        return listOf(
            WidgetSize(minWidth, minHeight),
            WidgetSize(maxWidth, maxHeight),
        ).filter(WidgetSize::isValid).distinct().ifEmpty { listOf(defaultSize) }
    }
}

data class WidgetSize(val widthDp: Int, val heightDp: Int) {
    fun isValid(): Boolean = widthDp > 0 && heightDp > 0
}
