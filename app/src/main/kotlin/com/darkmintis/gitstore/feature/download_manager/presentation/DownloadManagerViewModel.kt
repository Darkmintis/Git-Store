package com.darkmintis.gitstore.feature.download_manager.presentation

import android.app.DownloadManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkmintis.gitstore.core.data.services.Downloader
import com.darkmintis.gitstore.core.data.services.FileLocationsProvider
import com.darkmintis.gitstore.core.data.services.Installer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DownloadManagerViewModel(
    private val context: Context,
    private val downloader: Downloader,
    private val installer: Installer,
    private val files: FileLocationsProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(DownloadManagerState())
    val state = _state
        .onStart { refreshDownloads() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = DownloadManagerState()
        )

    fun refreshDownloads() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val items = loadDownloadItems()
            _state.update { it.copy(downloads = items, isLoading = false) }
        }
    }

    private suspend fun loadDownloadItems(): List<DownloadItem> = withContext(Dispatchers.IO) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val activeIds = downloader.getActiveDownloadIds()
        val dir = File(files.appDownloadsDir())
        if (!dir.exists()) return@withContext emptyList()

        dir.listFiles()
            .orEmpty()
            .filter { it.isFile }
            .sortedByDescending { it.lastModified() }
            .map { file ->
                val entry = activeIds.entries.find { (name, _) ->
                    file.name.contains(name.removeSuffix(".apk"))
                }

                if (entry != null) {
                    val id = entry.value
                    val cursor = downloadManager.query(
                        DownloadManager.Query().setFilterById(id)
                    )
                    var progress: Int? = null
                    var status = DownloadStatus.DOWNLOADING

                    if (cursor.moveToFirst()) {
                        val bytes = cursor.getLong(
                            cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        )
                        val total = cursor.getLong(
                            cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                        )
                        progress = if (total > 0) ((bytes * 100L) / total).toInt() else null

                        val sysStatus = cursor.getInt(
                            cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
                        )
                        status = when (sysStatus) {
                            DownloadManager.STATUS_SUCCESSFUL -> DownloadStatus.COMPLETED
                            DownloadManager.STATUS_FAILED -> DownloadStatus.FAILED
                            else -> DownloadStatus.DOWNLOADING
                        }
                    }
                    cursor.close()

                    DownloadItem(
                        fileName = file.name,
                        filePath = file.absolutePath,
                        fileSize = file.length(),
                        status = status,
                        progress = progress
                    )
                } else {
                    DownloadItem(
                        fileName = file.name,
                        filePath = file.absolutePath,
                        fileSize = file.length(),
                        status = DownloadStatus.COMPLETED,
                        progress = 100
                    )
                }
            }
    }

    fun installApk(filePath: String) {
        viewModelScope.launch {
            try {
                val ext = filePath.substringAfterLast('.', "").lowercase()
                installer.install(filePath, ext)
            } catch (_: Exception) { }
        }
    }

    fun deleteFile(filePath: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                File(filePath).delete()
            }
            refreshDownloads()
        }
    }

    fun cancelDownload(fileName: String) {
        viewModelScope.launch {
            downloader.cancelDownload(fileName)
            refreshDownloads()
        }
    }
}
