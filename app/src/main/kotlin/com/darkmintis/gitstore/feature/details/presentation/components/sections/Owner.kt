package com.darkmintis.gitstore.feature.details.presentation.components.sections

import com.darkmintis.gitstore.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.fletchmckee.liquid.liquefiable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.darkmintis.gitstore.core.domain.model.GithubUserProfile
import com.darkmintis.gitstore.feature.details.presentation.DetailsAction
import com.darkmintis.gitstore.feature.details.presentation.utils.LocalTopbarLiquidState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun LazyListScope.author(
    author: GithubUserProfile?,
    onAction: (DetailsAction) -> Unit
) {
    item {
        val liquidState = LocalTopbarLiquidState.current

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.author),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .padding(bottom = 12.dp)
                .liquefiable(liquidState),
            fontWeight = FontWeight.Bold,
        )

        OutlinedCard(
            onClick = {
                author?.login?.let { author ->
                    onAction(
                        DetailsAction.OpenDeveloperProfile(
                            author
                        )
                    )
                }
            },
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = author?.avatarUrl,
                    contentDescription = author?.login,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .liquefiable(liquidState)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    author?.login?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.liquefiable(liquidState)
                        )
                    }

                    author?.bio?.let { bio ->
                        Text(
                            text = bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 2,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.liquefiable(liquidState)
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    author?.htmlUrl?.let {
                        Row(
                            modifier = Modifier.clickable {
                                onAction(DetailsAction.OpenAuthorInBrowser)
                            },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_github),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = stringResource(R.string.profile),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                author?.login?.let { author ->
                    IconButton(
                        onClick = {
                            onAction(DetailsAction.OpenDeveloperProfile(author))
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = stringResource(R.string.open_developer_profile),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}



