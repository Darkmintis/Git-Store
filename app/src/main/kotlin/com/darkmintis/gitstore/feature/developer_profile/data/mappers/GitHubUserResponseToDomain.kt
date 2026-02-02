package com.darkmintis.gitstore.feature.developer_profile.data.mappers

import com.darkmintis.gitstore.feature.developer_profile.data.dto.GitHubUserResponse
import com.darkmintis.gitstore.feature.developer_profile.domain.model.DeveloperProfile

fun GitHubUserResponse.toDomain() = DeveloperProfile(
    login = login,
    name = name,
    avatarUrl = avatarUrl,
    bio = bio,
    company = company,
    location = location,
    email = email,
    blog = blog,
    twitterUsername = twitterUsername,
    publicRepos = publicRepos,
    publicGists = publicGists,
    followers = followers,
    following = following,
    createdAt = createdAt,
    updatedAt = updatedAt,
    htmlUrl = htmlUrl
)

