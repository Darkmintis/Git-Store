
package com.darkmintis.gitstore.feature.developer_profile.presentation.components

import kotlinx.datetime.Clock as DateClock
import kotlinx.datetime.Instant as DateInstant

import coil3.compose.AsyncImage
import com.darkmintis.gitstore.R
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp



















import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.darkmintis.gitstore.core.presentation.components.GithubStoreBodyText
import com.darkmintis.gitstore.core.presentation.components.GithubStoreSubtitleText
import com.darkmintis.gitstore.core.presentation.components.GithubStoreTitleText
import com.darkmintis.gitstore.core.presentation.theme.GithubStoreTheme
import com.darkmintis.gitstore.feature.developer_profile.domain.model.DeveloperRepository

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DeveloperRepoItem(
    repository: DeveloperRepository,
    onItemClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onItemClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    GithubStoreTitleText(
                        text = repository.name,
                        maxLines = 1
                    )

                    GithubStoreSubtitleText(
                        text = stringResource(
                            id = R.string.updated_on_date,
                            formatRelativeDate(repository.updatedAt)
                        ).replaceFirstChar { it.uppercase() },
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                FilledIconToggleButton(
                    checked = repository.isFavorite,
                    onCheckedChange = { onToggleFavorite() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (repository.isFavorite) {
                            Icons.Filled.Favorite
                        } else {
                            Icons.Outlined.FavoriteBorder
                        },
                        contentDescription = if (repository.isFavorite) {
                            stringResource(R.string.remove_from_favourites)
                        } else {
                            stringResource(R.string.add_to_favourites)
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            repository.description?.let { description ->
                Spacer(modifier = Modifier.height(8.dp))

                GithubStoreBodyText(
                    text = description,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RepoStat(
                    icon = Icons.Default.Star,
                    value = formatCount(repository.stargazersCount),
                    contentDescription = "${repository.stargazersCount} ${stringResource(R.string.stars)}"
                )

                RepoStat(
                    icon = Icons.AutoMirrored.Filled.CallSplit,
                    value = formatCount(repository.forksCount),
                    contentDescription = "${repository.forksCount} ${stringResource(R.string.forks)}"
                )

                if (repository.openIssuesCount > 0) {
                    RepoStat(
                        icon = Icons.Outlined.Warning,
                        value = formatCount(repository.openIssuesCount),
                        contentDescription = "${repository.openIssuesCount} ${stringResource(R.string.issues)}"
                    )
                }

                repository.language?.let { language ->
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                text = language,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.height(32.dp)
                    )
                }
            }

            val repoBadges = buildList {
                if (repository.hasInstallableAssets) {
                    add(
                        RepoBadge(
                            text = repository.latestVersion
                                ?: stringResource(R.string.has_release),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
                if (repository.isInstalled) {
                    add(
                        RepoBadge(
                            text = stringResource(R.string.installed),
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    )
                }
            }

            if (repoBadges.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repoBadges.forEach { badge ->
                        Badge(
                            containerColor = badge.containerColor
                        ) {
                            Text(
                                text = badge.text,
                                style = MaterialTheme.typography.labelSmall,
                                color = badge.contentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RepoStat(
    icon: ImageVector,
    value: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        GithubStoreSubtitleText(
            text = value,
            maxLines = 1
        )
    }
}

private data class RepoBadge(
    val text: String,
    val containerColor: Color,
    val contentColor: Color
)

@Composable
private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> stringResource(R.string.count_millions, count / 1_000_000)
        count >= 1000 -> stringResource(R.string.count_thousands, count / 1000)
        else -> count.toString()
    }
}

@Composable
private fun formatRelativeDate(dateString: String): String {
    val instant = try {
        DateInstant.parse(dateString)
    } catch (_: IllegalArgumentException) {
        return dateString
    }
    val now = DateClock.System.now()
    val duration = now - instant

    return when {
        duration.inWholeDays > 365 -> stringResource(
            R.string.time_years_ago,
            (duration.inWholeDays / 365).toInt()
        )

        duration.inWholeDays > 30 -> stringResource(
            R.string.time_months_ago,
            (duration.inWholeDays / 30).toInt()
        )

        duration.inWholeDays > 0 -> stringResource(
            R.string.time_days_ago,
            duration.inWholeDays.toInt()
        )

        duration.inWholeHours > 0 -> stringResource(
            R.string.time_hours_ago,
            duration.inWholeHours.toInt()
        )

        duration.inWholeMinutes > 0 -> stringResource(
            R.string.time_minutes_ago,
            duration.inWholeMinutes.toInt()
        )

        else -> stringResource(R.string.just_now)
    }
}

@Preview
@Composable
private fun PreviewDeveloperRepoItem() {
    GithubStoreTheme {
        DeveloperRepoItem(
            repository = DeveloperRepository(
                id = 1,
                name = "awesome-kotlin-app",
                fullName = "developer/awesome-kotlin-app",
                description = "An amazing Kotlin Multiplatform application that demonstrates modern Android development",
                htmlUrl = "",
                stargazersCount = 2340,
                forksCount = 456,
                openIssuesCount = 23,
                language = "Kotlin",
                hasReleases = true,
                hasInstallableAssets = true,
                isInstalled = true,
                isFavorite = false,
                latestVersion = "v1.5.2",
                updatedAt = DateClock.System.now().toString(),
                pushedAt = null
            ),
            onItemClick = {},
            onToggleFavorite = {}
        )
    }
}





