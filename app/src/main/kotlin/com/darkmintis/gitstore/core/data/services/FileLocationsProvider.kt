package com.darkmintis.gitstore.core.data.services

interface FileLocationsProvider {
    fun appDownloadsDir(): String
    fun userDownloadsDir(): String
    fun setExecutableIfNeeded(path: String)
}


