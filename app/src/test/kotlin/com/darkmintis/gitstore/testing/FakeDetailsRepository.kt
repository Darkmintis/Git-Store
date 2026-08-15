package com.darkmintis.gitstore.testing

import com.darkmintis.gitstore.core.domain.model.GithubRelease
import com.darkmintis.gitstore.core.domain.model.GithubRepoSummary
import com.darkmintis.gitstore.core.domain.model.GithubUserProfile
import com.darkmintis.gitstore.feature.details.domain.model.RepoStats
import com.darkmintis.gitstore.feature.details.domain.repository.DetailsRepository
import com.darkmintis.gitstore.feature.details.domain.repository.LanguageCode
import com.darkmintis.gitstore.feature.details.domain.repository.ReadmeContent
import com.darkmintis.gitstore.feature.details.domain.repository.ReadmePath

class FakeDetailsRepository(
    var repository: GithubRepoSummary = FakeSearchRepository.sampleRepo()
) : DetailsRepository {
    override suspend fun getRepositoryById(id: Long): GithubRepoSummary = repository

    override suspend fun getLatestPublishedRelease(
        owner: String,
        repo: String,
        defaultBranch: String
    ): GithubRelease? = null

    override suspend fun getReadme(
        owner: String,
        repo: String,
        defaultBranch: String
    ): Triple<ReadmeContent, LanguageCode?, ReadmePath>? = Triple("# README", "en", "README.md")

    override suspend fun getRepoStats(owner: String, repo: String): RepoStats =
        RepoStats(stars = 1, forks = 0, openIssues = 0)

    override suspend fun getUserProfile(username: String): GithubUserProfile =
        GithubUserProfile(
            id = 1,
            login = username,
            name = username,
            bio = null,
            avatarUrl = "https://example.com/a.png",
            htmlUrl = "https://github.com/$username",
            followers = 0,
            following = 0,
            publicRepos = 1,
            location = null,
            company = null,
            blog = null,
            twitterUsername = null
        )
}
