package com.darkmintis.gitstore.feature.download_manager.presentation

data class DownloadManagerState(
    val downloads: List<DownloadItem> = emptyList(),
    val isLoading: Boolean = true,
)

data class DownloadItem(
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val status: DownloadStatus,
    val progress: Int? = null,
)

enum class DownloadStatus {
    DOWNLOADING,
    COMPLETED,
    FAILED,
}
