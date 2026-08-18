package com.darkmintis.gitstore.feature.auth.data

import com.darkmintis.gitstore.BuildConfig

const val GITHUB_OAUTH_SCOPES = "read:user public_repo"

fun getGithubClientId(): String {
    return BuildConfig.GITHUB_CLIENT_ID.trim()
}
