package com.darkmintis.gitstore.feature.home.presentation.model

import com.darkmintis.gitstore.R

import androidx.compose.runtime.Composable




import androidx.compose.ui.res.stringResource

enum class HomeCategory {
    TRENDING,
    NEW,
    POPULAR;

    @Composable
    fun displayText(): String {
        return when (this) {
            TRENDING -> stringResource(R.string.home_category_trending)
            NEW -> stringResource(R.string.home_category_new)
            POPULAR -> stringResource(R.string.home_category_popular)
        }
    }
}



