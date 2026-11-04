package com.adioquino.nothingcalendar.widget.agenda

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object AgendaIndicatorColorResolver {
    private const val MIN_CONTRAST = 1.5
    private const val OPAQUE_ALPHA = 0xFF000000.toInt()

    fun resolve(
        calendarColor: Int?,
        backgroundColor: Int,
        fallbackColor: Int,
    ): Int {
        val fallback = fallbackColor or OPAQUE_ALPHA
        val candidate = calendarColor?.or(OPAQUE_ALPHA) ?: fallback
        return if (contrast(candidate, backgroundColor or OPAQUE_ALPHA) >= MIN_CONTRAST) {
            candidate
        } else {
            fallback
        }
    }

    private fun contrast(first: Int, second: Int): Double {
        val firstLuminance = luminance(first)
        val secondLuminance = luminance(second)
        return (max(firstLuminance, secondLuminance) + 0.05) /
            (min(firstLuminance, secondLuminance) + 0.05)
    }

    private fun luminance(color: Int): Double {
        val red = linearChannel((color shr 16) and 0xFF)
        val green = linearChannel((color shr 8) and 0xFF)
        val blue = linearChannel(color and 0xFF)
        return 0.2126 * red + 0.7152 * green + 0.0722 * blue
    }

    private fun linearChannel(channel: Int): Double {
        val value = channel / 255.0
        return if (value <= 0.04045) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
    }
}
