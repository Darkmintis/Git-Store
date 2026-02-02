package com.darkmintis.gitstore.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubRepoSearchResponse(
    @SerialName("total_count") val totalCount: Int,
    @SerialName("items") val items: List<GithubRepoNetworkModel>
)

