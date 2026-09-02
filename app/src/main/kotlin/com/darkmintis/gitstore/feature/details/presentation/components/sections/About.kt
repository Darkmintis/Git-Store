package com.darkmintis.gitstore.feature.details.presentation.components.sections

import com.darkmintis.gitstore.R

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.compose.Markdown
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import com.darkmintis.gitstore.core.data.services.TranslationService

import io.github.fletchmckee.liquid.liquefiable
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import androidx.compose.ui.res.stringResource
import com.darkmintis.gitstore.feature.details.presentation.utils.LocalTopbarLiquidState
import com.darkmintis.gitstore.feature.details.presentation.utils.MarkdownImageTransformer
import com.darkmintis.gitstore.feature.details.presentation.utils.rememberMarkdownColors
import com.darkmintis.gitstore.feature.details.presentation.utils.rememberMarkdownTypography

fun LazyListScope.about(
    readmeMarkdown: String,
    readmeLanguage: String?,
) {
    item {
        val liquidState = LocalTopbarLiquidState.current
        val translationService = koinInject<TranslationService>()
        val isAutoTranslateEnabled by translationService.isAutoTranslateEnabled.collectAsState()
        val currentTargetLang by translationService.targetLanguage.collectAsState()

        var translatedMarkdown by remember(readmeMarkdown, currentTargetLang) { mutableStateOf<String?>(null) }
        var showOriginal by remember(readmeMarkdown, currentTargetLang) { mutableStateOf(false) }
        var isTranslating by remember(readmeMarkdown, currentTargetLang) { mutableStateOf(false) }
        var manualTranslateRequested by remember(readmeMarkdown, currentTargetLang) { mutableStateOf(false) }
        var translationFailed by remember(readmeMarkdown, currentTargetLang) { mutableStateOf(false) }

        val canTranslateReadme = remember(readmeMarkdown, currentTargetLang) {
            readmeMarkdown.isNotBlank() && translationService.needsTranslation(readmeMarkdown)
        }

        LaunchedEffect(readmeMarkdown, currentTargetLang) {
            translatedMarkdown = null
            showOriginal = false
            manualTranslateRequested = false
            translationFailed = false
            isTranslating = false
        }

        LaunchedEffect(
            readmeMarkdown,
            currentTargetLang,
            isAutoTranslateEnabled,
            manualTranslateRequested,
        ) {
            if (!canTranslateReadme) return@LaunchedEffect
            if (!isAutoTranslateEnabled && !manualTranslateRequested) return@LaunchedEffect

            isTranslating = true
            translationFailed = false
            if (isAutoTranslateEnabled && !manualTranslateRequested) {
                delay(400)
            }

            val result = translationService.translateMarkdown(readmeMarkdown)
            isTranslating = false

            if (result != readmeMarkdown) {
                translatedMarkdown = result
                showOriginal = false
            } else if (manualTranslateRequested) {
                translationFailed = true
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(Modifier.height(16.dp))

        val langCode = translationService.getEffectiveLanguageCode()
        val hasTranslation = translatedMarkdown != null && translatedMarkdown != readmeMarkdown
        val showingTranslation = hasTranslation &&
            !showOriginal &&
            (isAutoTranslateEnabled || manualTranslateRequested)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.about_this_app),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.liquefiable(liquidState)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isTranslating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (canTranslateReadme) {
                    FilledTonalButton(
                        onClick = {
                            when {
                                hasTranslation -> showOriginal = !showOriginal
                                translationFailed -> {
                                    translationFailed = false
                                    manualTranslateRequested = true
                                }
                                else -> manualTranslateRequested = true
                            }
                        },
                        modifier = Modifier.liquefiable(liquidState),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = when {
                                hasTranslation && !showOriginal ->
                                    stringResource(R.string.original)
                                hasTranslation ->
                                    stringResource(R.string.show_translation, langCode.uppercase())
                                translationFailed ->
                                    stringResource(R.string.translation_unavailable)
                                else ->
                                    stringResource(R.string.translate_with_language, langCode.uppercase())
                            },
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                readmeLanguage?.let {
                    Text(
                        text = if (showingTranslation) langCode else it,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.liquefiable(liquidState)
                    )
                }
            }
        }

        val displayedContent = if (showingTranslation) translatedMarkdown!! else readmeMarkdown

        Surface(
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            val colors = rememberMarkdownColors()
            val typography = rememberMarkdownTypography()
            val flavour = remember { GFMFlavourDescriptor() }

            Markdown(
                content = displayedContent,
                colors = colors,
                typography = typography,
                flavour = flavour,
                imageTransformer = MarkdownImageTransformer,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
