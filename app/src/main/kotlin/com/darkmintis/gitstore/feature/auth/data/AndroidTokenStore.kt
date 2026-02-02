package com.darkmintis.gitstore.feature.auth.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.darkmintis.gitstore.core.domain.model.DeviceTokenSuccess
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AndroidTokenStore(
    private val dataStore: DataStore<Preferences>
) : TokenStore {
    
    private companion object {
        val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        val TOKEN_TYPE_KEY = stringPreferencesKey("token_type")
        val EXPIRES_IN_KEY = stringPreferencesKey("expires_in")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        val REFRESH_EXPIRES_IN_KEY = stringPreferencesKey("refresh_token_expires_in")
        val SCOPE_KEY = stringPreferencesKey("scope")
    }
    
    override suspend fun save(token: DeviceTokenSuccess) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = token.accessToken
            preferences[TOKEN_TYPE_KEY] = token.tokenType
            preferences[EXPIRES_IN_KEY] = token.expiresIn.toString()
            preferences[REFRESH_TOKEN_KEY] = token.refreshToken ?: ""
            preferences[REFRESH_EXPIRES_IN_KEY] = token.refreshTokenExpiresIn?.toString() ?: ""
            preferences[SCOPE_KEY] = token.scope ?: "repo,user"
        }
    }
    
    override suspend fun load(): DeviceTokenSuccess? {
        return dataStore.data.map { preferences ->
            val accessToken = preferences[ACCESS_TOKEN_KEY]
            val tokenType = preferences[TOKEN_TYPE_KEY]
            if (accessToken.isNullOrEmpty() || tokenType.isNullOrEmpty()) {
                null
            } else {
                DeviceTokenSuccess(
                    accessToken = accessToken,
                    tokenType = tokenType,
                    expiresIn = preferences[EXPIRES_IN_KEY]?.toLongOrNull() ?: 28800L,
                    refreshToken = preferences[REFRESH_TOKEN_KEY]?.takeIf { it.isNotEmpty() },
                    refreshTokenExpiresIn = preferences[REFRESH_EXPIRES_IN_KEY]?.toLongOrNull(),
                    scope = preferences[SCOPE_KEY] ?: "repo,user"
                )
            }
        }.first()
    }
    
    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(TOKEN_TYPE_KEY)
            preferences.remove(EXPIRES_IN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(REFRESH_EXPIRES_IN_KEY)
            preferences.remove(SCOPE_KEY)
        }
    }
}
