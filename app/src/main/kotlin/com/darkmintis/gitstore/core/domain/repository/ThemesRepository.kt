package com.darkmintis.gitstore.core.domain.repository

import kotlinx.coroutines.flow.Flow
import com.darkmintis.gitstore.core.presentation.model.AppTheme
import com.darkmintis.gitstore.core.presentation.model.FontTheme

interface ThemesRepository {
    fun getThemeColor(): Flow<AppTheme>
    suspend fun setThemeColor(theme: AppTheme)
    fun getIsDarkTheme(): Flow<Boolean?>
    suspend fun setDarkTheme(isDarkTheme: Boolean?)
    fun getAmoledTheme(): Flow<Boolean>
    suspend fun setAmoledTheme(enabled: Boolean)
    fun getFontTheme(): Flow<FontTheme>
    suspend fun setFontTheme(fontTheme: FontTheme)
}

