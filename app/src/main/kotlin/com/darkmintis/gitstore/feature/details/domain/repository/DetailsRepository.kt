package com.darkmintis.gitstore.feature.details.domain.repository

import com.darkmintis.gitstore.core.domain.model.GithubRelease
import com.darkmintis.gitstore.core.domain.model.GithubRepoSummary
import com.darkmintis.gitstore.core.domain.model.GithubUserProfile
import com.darkmintis.gitstore.feature.details.domain.model.RepoStats

typealias ReadmeContent = String
typealias ReadmePath = String
typealias LanguageCode = String

interface DetailsRepository {
    suspend fun getRepositoryById(id: Long): GithubRepoSummary

    suspend fun getLatestPublishedRelease(
        owner: String,
        repo: String,
        defaultBranch: String
    ): GithubRelease?

    suspend fun getReadme(
        owner: String,
        repo: String,
        defaultBranch: String
    ): Triple<ReadmeContent, LanguageCode?, ReadmePath>?

    suspend fun getRepoStats(owner: String, repo: String): RepoStats

    suspend fun getUserProfile(username: String): GithubUserProfile // ADD THIS
}

