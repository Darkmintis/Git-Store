package com.darkmintis.gitstore.feature.search.domain.model

import com.darkmintis.gitstore.R

enum class SortBy {
    MostStars,
    MostForks,
    BestMatch;

    fun displayText(): String = when (this) {
        MostStars -> "Most Stars"
        MostForks -> "Most Forks"
        BestMatch -> "Best Match"
    }

    fun label(): Int = when (this) {
        MostStars -> R.string.sort_most_stars
        MostForks -> R.string.sort_most_forks
        BestMatch -> R.string.sort_best_match
    }

    fun toGithubParams(): Pair<String?, String> = when (this) {
        MostStars -> "stars" to "desc"
        MostForks -> "forks" to "desc"
        BestMatch -> null to "desc"
    }
}




