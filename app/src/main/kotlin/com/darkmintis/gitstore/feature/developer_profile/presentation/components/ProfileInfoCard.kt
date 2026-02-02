package com.darkmintis.gitstore.feature.developer_profile.presentation.components

import coil3.compose.AsyncImage

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.darkmintis.gitstore.core.presentation.components.GithubStoreBodyText
import com.darkmintis.gitstore.core.presentation.components.GithubStoreSubtitleText
import com.darkmintis.gitstore.core.presentation.components.GithubStoreTitleText
import com.darkmintis.gitstore.feature.developer_profile.domain.model.DeveloperProfile
import com.darkmintis.gitstore.feature.developer_profile.presentation.DeveloperProfileAction

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProfileInfoCard(
    profile: DeveloperProfile,
    onAction: (DeveloperProfileAction) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                AsyncImage(
                    model = profile.avatarUrl,
                    contentDescription = profile.name ?: profile.login,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    GithubStoreTitleText(
                        text = profile.name ?: profile.login,
                        maxLines = 2
                    )

                    Spacer(Modifier.height(4.dp))

                    GithubStoreSubtitleText(
                        text = "@${profile.login}",
                        maxLines = 1
                    )

                    profile.location?.let { location ->
                        Spacer(Modifier.height(8.dp))

                        InfoChip(
                            icon = Icons.Default.LocationOn,
                            text = location
                        )
                    }

                    profile.bio?.let { bio ->
                        Spacer(modifier = Modifier.height(8.dp))

                        GithubStoreBodyText(
                            text = bio,
                            maxLines = 4
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                itemVerticalAlignment = Alignment.CenterVertically
            ) {
                profile.company?.let { company ->
                    InfoChip(
                        icon = Icons.Default.Business,
                        text = company
                    )
                }

                profile.blog?.takeIf { it.isNotBlank() }?.let { blog ->
                    val displayUrl = blog.removePrefix("https://").removePrefix("http://")
                    AssistChip(
                        onClick = {
                            val url = if (!blog.startsWith("http")) "https://$blog" else blog
                            onAction(DeveloperProfileAction.OnOpenLink(url))
                        },
                        label = {
                            Text(
                                text = displayUrl,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }

                profile.twitterUsername?.let { twitter ->
                    AssistChip(
                        onClick = {
                            onAction(DeveloperProfileAction.OnOpenLink("https://twitter.com/$twitter"))
                        },
                        label = {
                            Text(
                                text = "@$twitter",
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Tag,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        GithubStoreBodyText(
            text = text,
            maxLines = 1
        )
    }
}


