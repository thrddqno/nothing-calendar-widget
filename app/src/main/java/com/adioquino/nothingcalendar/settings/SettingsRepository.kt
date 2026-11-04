package com.adioquino.nothingcalendar.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val useDynamicColors: Flow<Boolean>

    suspend fun setUseDynamicColors(enabled: Boolean)
}
