package com.darkmintis.gitstore.feature.settings.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import com.darkmintis.gitstore.core.data.data_source.TokenDataSource
import com.darkmintis.gitstore.feature.settings.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val tokenDataSource: TokenDataSource,
) : SettingsRepository {
    override val isUserLoggedIn: Flow<Boolean>
        get() = tokenDataSource
            .tokenFlow
            .map {
                it != null
            }
            .flowOn(Dispatchers.IO)

    override suspend fun logout() {
        tokenDataSource.clear()
    }
}

