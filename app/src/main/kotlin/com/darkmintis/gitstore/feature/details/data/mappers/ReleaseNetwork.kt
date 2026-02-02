package com.darkmintis.gitstore.feature.details.data.mappers

import com.darkmintis.gitstore.core.domain.model.GithubRelease
import com.darkmintis.gitstore.core.domain.model.GithubUser
import com.darkmintis.gitstore.feature.details.data.dto.ReleaseNetwork

fun ReleaseNetwork.toDomain(): GithubRelease = GithubRelease(
    id = id,
    tagName = tagName,
    name = name,
    author = GithubUser(
        id = author.id,
        login = author.login,
        avatarUrl = author.avatarUrl,
        htmlUrl = author.htmlUrl
    ),
    publishedAt = publishedAt ?: createdAt ?: "",
    description = body,
    assets = assets.map { assetNetwork -> assetNetwork.toDomain() },
    tarballUrl = tarballUrl,
    zipballUrl = zipballUrl,
    htmlUrl = htmlUrl
)


