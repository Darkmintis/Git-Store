package com.darkmintis.gitstore.feature.auth.data

class MissingGithubClientIdException : IllegalStateException(
    "Missing GitHub CLIENT_ID. Set githubClientId in app/build.gradle.kts."
)
