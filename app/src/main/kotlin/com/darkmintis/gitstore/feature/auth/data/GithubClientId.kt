package com.darkmintis.gitstore.feature.auth.data

import com.darkmintis.gitstore.BuildConfig

fun getGithubClientId(): String {
    return BuildConfig.GITHUB_CLIENT_ID.trim()
}
