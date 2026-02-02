package com.darkmintis.gitstore.feature.details.presentation.components.sections

import com.darkmintis.gitstore.R

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp





import io.github.fletchmckee.liquid.liquefiable
import androidx.compose.ui.res.stringResource
import com.darkmintis.gitstore.feature.details.domain.model.RepoStats
import com.darkmintis.gitstore.feature.details.presentation.components.StatItem
import com.darkmintis.gitstore.feature.details.presentation.utils.LocalTopbarLiquidState

fun LazyListScope.stats(
    repoStats: RepoStats,
) {
    item {
        val liquidState = LocalTopbarLiquidState.current

        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatItem(
                label = stringResource(R.string.forks),
                stat = repoStats.forks,
                modifier = Modifier
                    .weight(1.5f)
                    .liquefiable(liquidState)
            )

            StatItem(
                label = stringResource(R.string.stars),
                stat = repoStats.stars,
                modifier = Modifier
                    .weight(2f)
                    .liquefiable(liquidState)
            )

            StatItem(
                label = stringResource(R.string.issues),
                stat = repoStats.openIssues,
                modifier = Modifier
                    .weight(1f)
                    .liquefiable(liquidState)
            )
        }
    }
}



