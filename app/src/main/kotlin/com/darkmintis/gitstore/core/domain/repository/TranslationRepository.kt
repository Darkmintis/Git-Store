package com.darkmintis.gitstore.core.domain.repository

import com.darkmintis.gitstore.core.domain.model.TranslationLanguage
import kotlinx.coroutines.flow.Flow

interface TranslationRepository {
    fun getTargetLanguage(): Flow<TranslationLanguage>
    suspend fun setTargetLanguage(language: TranslationLanguage)
    fun isAutoTranslateEnabled(): Flow<Boolean>
    suspend fun setAutoTranslateEnabled(enabled: Boolean)
}
