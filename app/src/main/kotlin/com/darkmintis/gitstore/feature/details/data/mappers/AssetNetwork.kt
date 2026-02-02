package com.darkmintis.gitstore.feature.details.data.mappers

import com.darkmintis.gitstore.core.domain.model.GithubAsset
import com.darkmintis.gitstore.core.domain.model.GithubUser
import com.darkmintis.gitstore.feature.details.data.dto.AssetNetwork

fun AssetNetwork.toDomain(): GithubAsset = GithubAsset(
    id = id,
    name = name,
    contentType = contentType,
    size = size,
    downloadUrl = downloadUrl,
    uploader = GithubUser(
        id = uploader.id,
        login = uploader.login,
        avatarUrl = uploader.avatarUrl,
        htmlUrl = uploader.htmlUrl
    )
)

