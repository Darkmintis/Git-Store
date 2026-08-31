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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.compose.Markdown
import kotlinx.coroutines.launch
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
        val currentTargetLang by translationService.targetLanguage.collectAsState()
        val coroutineScope = rememberCoroutineScope()

        var isTranslated by remember(readmeMarkdown, currentTargetLang) { mutableStateOf(false) }
        var isTranslating by remember(readmeMarkdown, currentTargetLang) { mutableStateOf(false) }
        var translatedMarkdown by remember(readmeMarkdown, currentTargetLang) { mutableStateOf<String?>(null) }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(Modifier.height(16.dp))

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

                val langCode = translationService.getEffectiveLanguageCode()

                FilledTonalButton(
                    onClick = {
                        if (isTranslated) {
                            isTranslated = false
                        } else {
                            if (translatedMarkdown != null) {
                                isTranslated = true
                            } else {
                                isTranslating = true
                                coroutineScope.launch {
                                    val res = translationService.translateMarkdown(readmeMarkdown)
                                    translatedMarkdown = res
                                    isTranslated = true
                                    isTranslating = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.liquefiable(liquidState),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isTranslated) "Original" else "🌐 Traduire (${langCode.uppercase()})",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                readmeLanguage?.let {
                    Text(
                        text = if (isTranslated) langCode else it,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.liquefiable(liquidState)
                    )
                }
            }
        }

        val displayedContent = if (isTranslated && translatedMarkdown != null) translatedMarkdown!! else readmeMarkdown

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
                modifier = Modifier
                    .fillMaxWidth()
                    .liquefiable(liquidState),
            )
        }
    }
}



