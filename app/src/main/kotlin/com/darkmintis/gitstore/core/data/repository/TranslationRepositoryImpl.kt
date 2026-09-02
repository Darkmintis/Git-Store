package com.darkmintis.gitstore.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.darkmintis.gitstore.core.domain.model.TranslationLanguage
import com.darkmintis.gitstore.core.domain.repository.TranslationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TranslationRepositoryImpl(
    private val preferences: DataStore<Preferences>
) : TranslationRepository {
    private val TARGET_LANG_KEY = stringPreferencesKey("translation_target_language")
    private val AUTO_TRANSLATE_KEY = booleanPreferencesKey("auto_translate_enabled")

    override fun getTargetLanguage(): Flow<TranslationLanguage> {
        return preferences.data.map { prefs ->
            val code = prefs[TARGET_LANG_KEY]
            TranslationLanguage.fromCode(code)
        }
    }

    override suspend fun setTargetLanguage(language: TranslationLanguage) {
        preferences.edit { prefs ->
            prefs[TARGET_LANG_KEY] = language.code
        }
    }

    override fun isAutoTranslateEnabled(): Flow<Boolean> {
        return preferences.data.map { prefs ->
            prefs[AUTO_TRANSLATE_KEY] ?: false
        }
    }

    override suspend fun setAutoTranslateEnabled(enabled: Boolean) {
        preferences.edit { prefs ->
            prefs[AUTO_TRANSLATE_KEY] = enabled
        }
    }
}
