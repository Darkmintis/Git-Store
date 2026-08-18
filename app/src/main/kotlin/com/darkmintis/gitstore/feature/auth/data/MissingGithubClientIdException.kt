package com.darkmintis.gitstore.feature.auth.data

class MissingGithubClientIdException : IllegalStateException(
    "Missing GitHub CLIENT_ID. Add GITHUB_CLIENT_ID to local.properties or CI secrets."
)
