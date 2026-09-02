package com.darkmintis.gitstore.feature.details.presentation.components.sections

import com.darkmintis.gitstore.R

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.compose.Markdown
import org.koin.compose.koinInject
import com.darkmintis.gitstore.core.data.services.TranslationService

import io.github.fletchmckee.liquid.liquefiable
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import androidx.compose.ui.res.stringResource
import com.darkmintis.gitstore.core.domain.model.GithubRelease
import com.darkmintis.gitstore.feature.details.presentation.utils.LocalTopbarLiquidState
import com.darkmintis.gitstore.feature.details.presentation.utils.rememberMarkdownColors
import com.darkmintis.gitstore.feature.details.presentation.utils.rememberMarkdownTypography

fun LazyListScope.whatsNew(latestRelease: GithubRelease) {
    item {
        val liquidState = LocalTopbarLiquidState.current
        val translationService = koinInject<TranslationService>()
        val isAutoTranslateEnabled by translationService.isAutoTranslateEnabled.collectAsState()
        val currentTargetLang by translationService.targetLanguage.collectAsState()

        val rawDescription = latestRelease.description
        var translatedDescription by remember(rawDescription, currentTargetLang) { mutableStateOf<String?>(null) }
        var isTranslated by remember(rawDescription, currentTargetLang) { mutableStateOf(false) }
        var manualTranslateRequested by remember(rawDescription, currentTargetLang) { mutableStateOf(false) }
        var translationFailed by remember(rawDescription, currentTargetLang) { mutableStateOf(false) }

        val needsTranslation = remember(rawDescription, currentTargetLang) {
            rawDescription != null && translationService.needsTranslation(rawDescription)
        }

        LaunchedEffect(rawDescription, currentTargetLang) {
            translatedDescription = null
            isTranslated = false
            manualTranslateRequested = false
            translationFailed = false
        }

        LaunchedEffect(rawDescription, currentTargetLang, isAutoTranslateEnabled, manualTranslateRequested) {
            if (rawDescription == null || !needsTranslation) return@LaunchedEffect
            if (!isAutoTranslateEnabled && !manualTranslateRequested) return@LaunchedEffect
            val res = translationService.translate(rawDescription)
            if (res != rawDescription) {
                translatedDescription = res
                translationFailed = false
                if (isAutoTranslateEnabled || manualTranslateRequested) {
                    isTranslated = true
                }
            } else {
                translationFailed = true
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.whats_new),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.liquefiable(liquidState),
                fontWeight = FontWeight.Bold,
            )

            val langCode = translationService.getEffectiveLanguageCode().uppercase()
            val currentTranslated = translatedDescription
            val hasTranslation = currentTranslated != null && currentTranslated != rawDescription
            if (needsTranslation && (hasTranslation || !isAutoTranslateEnabled || translationFailed)) {
                TextButton(
                    onClick = {
                        when {
                            hasTranslation -> isTranslated = !isTranslated
                            translationFailed -> {
                                translationFailed = false
                                manualTranslateRequested = true
                            }
                            else -> manualTranslateRequested = true
                        }
                    },
                    modifier = Modifier.liquefiable(liquidState)
                ) {
                    Text(
                        text = when {
                            hasTranslation && isTranslated ->
                                stringResource(R.string.original)
                            hasTranslation ->
                                stringResource(R.string.show_translation, langCode)
                            translationFailed ->
                                stringResource(R.string.translation_unavailable)
                            else ->
                                stringResource(R.string.translate_with_language, langCode)
                        },
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        latestRelease.tagName,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.liquefiable(liquidState)
                    )

                    Text(
                        latestRelease.publishedAt.take(10),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.liquefiable(liquidState)
                    )
                }

                Spacer(Modifier.height(12.dp))

                val colors = rememberMarkdownColors()
                val typography = rememberMarkdownTypography()
                val flavour = remember { GFMFlavourDescriptor() }

                val currentTrans = translatedDescription
                val displayedDesc: String? = if (isTranslated && currentTrans != null) currentTrans else rawDescription

                Markdown(
                    content = displayedDesc ?: stringResource(R.string.no_release_notes),
                    colors = colors,
                    typography = typography,
                    flavour = flavour,
                    imageTransformer = Coil3ImageTransformerImpl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquefiable(liquidState),
                )
            }
        }
    }
}





