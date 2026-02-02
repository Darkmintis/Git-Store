package com.darkmintis.gitstore.core.data.services

import com.darkmintis.gitstore.core.domain.model.ApkPackageInfo

interface ApkInfoExtractor {
    suspend fun extractPackageInfo(filePath: String): ApkPackageInfo?
}

