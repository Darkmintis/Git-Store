package com.darkmintis.gitstore.feature.developer_profile.presentation.components

import com.darkmintis.gitstore.R

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp




import androidx.compose.ui.res.stringResource
import com.darkmintis.gitstore.core.presentation.components.GithubStoreBodyText
import com.darkmintis.gitstore.core.presentation.components.GithubStoreTitleText
import com.darkmintis.gitstore.feature.developer_profile.domain.model.DeveloperProfile

@Composable
fun StatsRow(profile: DeveloperProfile) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            label = stringResource(R.string.repositories),
            value = profile.publicRepos.toString(),
            modifier = Modifier.weight(1f)
        )

        StatCard(
            label = stringResource(R.string.followers),
            value = formatCount(profile.followers),
            modifier = Modifier.weight(1f)
        )

        StatCard(
            label = stringResource(R.string.following),
            value = formatCount(profile.following),
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GithubStoreTitleText(
                text = value,
                maxLines = 1
            )

            GithubStoreBodyText(
                text = label,
                maxLines = 1
            )
        }
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1000 -> "${count / 1000}k"
        else -> count.toString()
    }
}



