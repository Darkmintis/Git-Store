package com.darkmintis.gitstore.core.presentation.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun GithubStoreTitleText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    fontWeight: FontWeight = FontWeight.Bold
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        fontWeight = fontWeight,
        softWrap = maxLines > 1
    )
}

/**
 * Subtitle text component
 * Used for: Secondary headings, usernames, timestamps, metadata
 */
@Composable
fun GithubStoreSubtitleText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    fontWeight: FontWeight? = null
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        fontWeight = fontWeight,
        softWrap = maxLines > 1
    )
}

/**
 * Body text component
 * Used for: Descriptions, messages, general content, labels
 */
@Composable
fun GithubStoreBodyText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    fontWeight: FontWeight? = null
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        fontWeight = fontWeight,
        softWrap = maxLines > 1
    )
}

/**
 * Large body text component
 * Used for: Prominent descriptions, highlighted content
 */
@Composable
fun GithubStoreLargeBodyText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    fontWeight: FontWeight? = null
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyLarge,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        fontWeight = fontWeight,
        softWrap = maxLines > 1
    )
}

/**
 * Small body text component
 * Used for: Fine print, secondary information, captions
 */
@Composable
fun GithubStoreSmallBodyText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    fontWeight: FontWeight? = null
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        fontWeight = fontWeight,
        softWrap = maxLines > 1
    )
}

/**
 * Button text component
 * Used for: Button labels, action text
 */
@Composable
fun GithubStoreButtonText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalTextStyle.current.color,
    textAlign: TextAlign? = null,
    maxLines: Int = 1,
    fontWeight: FontWeight = FontWeight.SemiBold
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        fontWeight = fontWeight,
        softWrap = false
    )
}

/**
 * Label text component
 * Used for: Form labels, chip labels, small UI elements
 */
@Composable
fun GithubStoreLabelText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign? = null,
    maxLines: Int = 1,
    fontWeight: FontWeight = FontWeight.Medium
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        fontWeight = fontWeight,
        softWrap = false
    )
}

/**
 * Display text component (extra large)
 * Used for: Hero sections, splash screens, very prominent headings
 */
@Composable
fun GithubStoreDisplayText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    fontWeight: FontWeight = FontWeight.Bold
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.displayMedium,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        fontWeight = fontWeight,
        softWrap = maxLines > 1
    )
}

/**
 * Headline text component
 * Used for: Section headers, category titles
 */
@Composable
fun GithubStoreHeadlineText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    fontWeight: FontWeight = FontWeight.SemiBold
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.headlineMedium,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        fontWeight = fontWeight,
        softWrap = maxLines > 1
    )
}

