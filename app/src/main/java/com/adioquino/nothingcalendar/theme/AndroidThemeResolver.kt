package com.adioquino.nothingcalendar.theme

import android.content.Context
import androidx.core.graphics.ColorUtils
import com.adioquino.nothingcalendar.R
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.MaterialColors

class AndroidThemeResolver(context: Context) : ThemeResolver {
    private val appContext = context.applicationContext

    override fun resolve(): WidgetPalette {
        val themedContext = DynamicColors.wrapContextIfAvailable(appContext)
        val fallbackBackground = appContext.getColor(R.color.widget_background)
        val background = MaterialColors.getColor(
            themedContext,
            com.google.android.material.R.attr.colorSurface,
            fallbackBackground,
        )

        return WidgetPalette(
            background = ColorUtils.setAlphaComponent(background, WIDGET_BACKGROUND_ALPHA),
            primaryText = MaterialColors.getColor(
                themedContext,
                com.google.android.material.R.attr.colorOnSurface,
                appContext.getColor(R.color.widget_text_primary),
            ),
            secondaryText = MaterialColors.getColor(
                themedContext,
                com.google.android.material.R.attr.colorOnSurfaceVariant,
                appContext.getColor(R.color.widget_text_secondary),
            ),
            accent = MaterialColors.getColor(
                themedContext,
                androidx.appcompat.R.attr.colorPrimary,
                appContext.getColor(R.color.widget_accent),
            ),
        )
    }

    private companion object {
        const val WIDGET_BACKGROUND_ALPHA = 235
    }
}
