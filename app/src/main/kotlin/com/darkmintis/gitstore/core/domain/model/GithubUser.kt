package com.darkmintis.gitstore.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GithubUser(
    val id: Long,
    val login: String,
    val avatarUrl: String,
    val htmlUrl: String
)

data class GithubUserProfile(
    val id: Long,
    val login: String,
    val name: String?,
    val bio: String?,
    val avatarUrl: String,
    val htmlUrl: String,
    val followers: Int,
    val following: Int,
    val publicRepos: Int,
    val location: String?,
    val company: String?,
    val blog: String?,
    val twitterUsername: String?
)

