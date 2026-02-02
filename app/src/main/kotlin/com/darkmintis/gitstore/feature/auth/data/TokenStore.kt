package com.darkmintis.gitstore.feature.auth.data

import com.darkmintis.gitstore.core.domain.model.DeviceTokenSuccess

interface TokenStore {
    suspend fun save(token: DeviceTokenSuccess)
    suspend fun load(): DeviceTokenSuccess?
    suspend fun clear()
}
