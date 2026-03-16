package com.darkmintis.gitstore.feature.settings.presentation

import com.darkmintis.gitstore.BuildConfig
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.darkmintis.gitstore.core.domain.repository.ThemesRepository
import com.darkmintis.gitstore.core.presentation.utils.BrowserHelper
import com.darkmintis.gitstore.feature.settings.domain.repository.SettingsRepository
import com.darkmintis.gitstore.feature.settings.presentation.SettingsEvent.*
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

private const val LATEST_RELEASE_API_URL = "https://api.github.com/repos/Darkmintis/Git-Store/releases/latest"

class SettingsViewModel(
    private val browserHelper: BrowserHelper,
    private val themesRepository: ThemesRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(SettingsState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                loadCurrentTheme()
                collectIsUserLoggedIn()
                checkForGitStoreUpdate()

                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = SettingsState()
        )

    private val _events = Channel<SettingsEvent>()
    val events = _events.receiveAsFlow()

    private fun collectIsUserLoggedIn() {
        viewModelScope.launch {
            settingsRepository.isUserLoggedIn
                .collect { isLoggedIn ->
                    _state.update { it.copy(isUserLoggedIn = isLoggedIn) }
                }
        }
    }

    private fun loadCurrentTheme() {
        viewModelScope.launch {
            themesRepository.getThemeColor().collect { theme ->
                _state.update {
                    it.copy(selectedThemeColor = theme)
                }
            }
        }

        viewModelScope.launch {
            themesRepository.getAmoledTheme().collect { isAmoled ->
                _state.update {
                    it.copy(isAmoledThemeEnabled = isAmoled)
                }
            }
        }

        viewModelScope.launch {
            themesRepository.getIsDarkTheme().collect { isDarkTheme ->
                _state.update {
                    it.copy(isDarkTheme = isDarkTheme)
                }
            }
        }

        viewModelScope.launch {
            themesRepository.getFontTheme().collect { fontTheme ->
                _state.update {
                    it.copy(selectedFontTheme = fontTheme)
                }
            }
        }
    }

    private fun checkForGitStoreUpdate() {
        viewModelScope.launch {
            if (_state.value.isCheckingGitStoreUpdate) return@launch
            _state.update {
                it.copy(
                    isCheckingGitStoreUpdate = true,
                    gitStoreUpdateErrorMessage = null
                )
            }

            val release = fetchLatestGitStoreRelease()
            if (release == null) {
                _state.update {
                    it.copy(
                        isCheckingGitStoreUpdate = false,
                        isGitStoreUpdateAvailable = false,
                        gitStoreUpdateErrorMessage = "Unable to check for updates"
                    )
                }
                return@launch
            }

            val currentVersion = BuildConfig.VERSION_NAME
            val hasUpdate = compareVersions(release.versionTag, currentVersion) > 0

            _state.update {
                it.copy(
                    isCheckingGitStoreUpdate = false,
                    isGitStoreUpdateAvailable = hasUpdate,
                    latestGitStoreVersion = release.versionTag,
                    latestGitStoreDownloadUrl = release.downloadUrl,
                    gitStoreUpdateErrorMessage = null
                )
            }
        }
    }

    private suspend fun fetchLatestGitStoreRelease(): GitStoreRelease? = withContext(Dispatchers.IO) {
        runCatching {
            val connection = (URL(LATEST_RELEASE_API_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "GitStore-Android")
            }

            if (connection.responseCode !in 200..299) return@runCatching null

            val payload = connection.inputStream.bufferedReader().use { it.readText() }
            val root = Json.parseToJsonElement(payload).jsonObject

            val tagName = root["tag_name"]?.jsonPrimitive?.content.orEmpty()
            if (tagName.isBlank()) return@runCatching null

            val htmlUrl = root["html_url"]?.jsonPrimitive?.content.orEmpty()
            val assetUrls = root["assets"]
                ?.jsonArray
                ?.mapNotNull { element ->
                    element.jsonObject["browser_download_url"]?.jsonPrimitive?.content
                }
                .orEmpty()
            val apkAssetUrl = assetUrls.firstOrNull { it.endsWith(".apk", ignoreCase = true) }
            val downloadUrl = apkAssetUrl ?: htmlUrl

            if (downloadUrl.isBlank()) return@runCatching null

            GitStoreRelease(
                versionTag = tagName,
                downloadUrl = downloadUrl
            )
        }.getOrNull()
    }

    private fun compareVersions(remote: String, local: String): Int {
        val remoteParts = versionParts(remote)
        val localParts = versionParts(local)
        val maxSize = maxOf(remoteParts.size, localParts.size)

        for (i in 0 until maxSize) {
            val remotePart = remoteParts.getOrElse(i) { 0 }
            val localPart = localParts.getOrElse(i) { 0 }

            if (remotePart != localPart) {
                return remotePart.compareTo(localPart)
            }
        }

        return 0
    }

    private fun versionParts(version: String): List<Int> {
        return Regex("\\d+")
            .findAll(version)
            .map { it.value.toIntOrNull() ?: 0 }
            .toList()
    }

    private data class GitStoreRelease(
        val versionTag: String,
        val downloadUrl: String
    )

    fun onAction(action: SettingsAction) {
        when (action) {
            SettingsAction.OnHelpClick -> {
                browserHelper.openUrl(
                    url = "https://github.com/Darkmintis/Git-Store/issues"
                )
            }

            SettingsAction.OnGitHubSignInClick -> {
                // Handled in composable - navigation to AuthenticationScreen
            }

            is SettingsAction.OnBrowserOpen -> {
                browserHelper.openUrl(
                    url = action.url,
                    useChooser = action.useChooser
                )
            }

            SettingsAction.OnCheckGitStoreUpdateClick -> {
                checkForGitStoreUpdate()
            }

            is SettingsAction.OnThemeColorSelected -> {
                viewModelScope.launch {
                    themesRepository.setThemeColor(action.themeColor)
                }
            }

            is SettingsAction.OnAmoledThemeToggled -> {
                viewModelScope.launch {
                    themesRepository.setAmoledTheme(action.enabled)
                }
            }

            SettingsAction.OnLogoutClick -> {
                _state.update {
                    it.copy(
                        isLogoutDialogVisible = true
                    )
                }
            }

            SettingsAction.OnLogoutConfirmClick -> {
                viewModelScope.launch {
                    runCatching {
                        settingsRepository.logout()
                    }.onSuccess {
                        _state.update { it.copy(isLogoutDialogVisible = false) }
                        _events.send(SettingsEvent.OnLogoutSuccessful)
                    }.onFailure { error ->
                        _state.update { it.copy(isLogoutDialogVisible = false) }
                        error.message?.let {
                            _events.send(OnLogoutError(it))
                        }
                    }
                }
            }

            SettingsAction.OnLogoutDismiss -> {
                _state.update {
                    it.copy(
                        isLogoutDialogVisible = false
                    )
                }
            }

            SettingsAction.OnNavigateBackClick -> {
                /* Handed in composable */
            }

            is SettingsAction.OnFontThemeSelected -> {
                viewModelScope.launch {
                    themesRepository.setFontTheme(action.fontTheme)
                }
            }

            is SettingsAction.OnDarkThemeChange -> {
                viewModelScope.launch {
                    themesRepository.setDarkTheme(action.isDarkTheme)
                }
            }
        }
    }

}


