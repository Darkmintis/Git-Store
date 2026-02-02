package com.darkmintis.gitstore.core.data.data_source

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.darkmintis.gitstore.core.domain.model.DeviceTokenSuccess
import com.darkmintis.gitstore.feature.auth.data.TokenStore

interface TokenDataSource {
    val tokenFlow: StateFlow<DeviceTokenSuccess?>
    suspend fun save(token: DeviceTokenSuccess)
    suspend fun reloadFromStore(): DeviceTokenSuccess?
    suspend fun clear()

    fun current(): DeviceTokenSuccess?
}

class DefaultTokenDataSource(
    private val tokenStore: TokenStore,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : TokenDataSource {
    private val _flow = MutableStateFlow<DeviceTokenSuccess?>(null)
    override val tokenFlow: StateFlow<DeviceTokenSuccess?> = _flow

    private val isInitialized = CompletableDeferred<Unit>()

    init {
        scope.launch {
            try {
                val token = tokenStore.load()
                _flow.value = token
            } finally {
                isInitialized.complete(Unit)
            }
        }
    }

    override suspend fun save(token: DeviceTokenSuccess) {
        tokenStore.save(token)
        _flow.value = token
    }

    override suspend fun reloadFromStore(): DeviceTokenSuccess? {
        isInitialized.await()
        return _flow.value
    }

    override suspend fun clear() {
        tokenStore.clear()
        _flow.value = null
    }

    override fun current(): DeviceTokenSuccess? = _flow.value
}


