package com.darkmintis.gitstore.feature.favourites.presentation.mappers

import android.content.Context
import com.darkmintis.gitstore.core.data.local.db.entities.FavoriteRepo
import com.darkmintis.gitstore.core.presentation.utils.formatAddedAt
import com.darkmintis.gitstore.feature.favourites.presentation.model.FavouriteRepository

suspend fun FavoriteRepo.toFavouriteRepositoryUi(context: Context): FavouriteRepository {
    return FavouriteRepository(
        repoId = repoId,
        repoName = repoName,
        repoOwner = repoOwner,
        repoOwnerAvatarUrl = repoOwnerAvatarUrl,
        repoDescription = repoDescription,
        primaryLanguage = primaryLanguage,
        repoUrl = repoUrl,
        latestRelease = latestVersion,
        latestReleaseUrl = latestReleaseUrl,
        addedAtFormatter = formatAddedAt(addedAt, context)
    )
}
