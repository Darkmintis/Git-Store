package com.darkmintis.gitstore.core.data.mappers

import com.darkmintis.gitstore.core.domain.model.GithubRepoSummary
import com.darkmintis.gitstore.core.domain.model.GithubUser
import com.darkmintis.gitstore.core.data.model.GithubRepoNetworkModel

fun GithubRepoNetworkModel.toSummary(): GithubRepoSummary = GithubRepoSummary(
    id = id,
    name = name,
    fullName = fullName,
    owner = GithubUser(
        id = owner.id,
        login = owner.login,
        avatarUrl = owner.avatarUrl,
        htmlUrl = owner.htmlUrl
    ),
    description = description,
    htmlUrl = htmlUrl,
    stargazersCount = stargazersCount,
    forksCount = forksCount,
    language = language,
    topics = topics,
    releasesUrl = releasesUrl,
    updatedAt = updatedAt,
    defaultBranch = defaultBranch
)



