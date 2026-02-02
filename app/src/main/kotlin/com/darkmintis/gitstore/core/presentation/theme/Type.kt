package com.darkmintis.gitstore.core.presentation.theme

import com.darkmintis.gitstore.R
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.darkmintis.gitstore.core.presentation.model.FontTheme

val jetbrainsMonoFontFamily = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
    Font(R.font.jetbrains_mono_bold, FontWeight.Bold),
)

val interFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_bold, FontWeight.Bold),
)

val baseline = Typography()

@Composable
fun getAppTypography(fontTheme: FontTheme = FontTheme.CUSTOM): Typography {
    return when (fontTheme) {
        FontTheme.SYSTEM -> baseline
        FontTheme.CUSTOM -> Typography(
            displayLarge = baseline.displayLarge.copy(fontFamily = jetbrainsMonoFontFamily),
            displayMedium = baseline.displayMedium.copy(fontFamily = jetbrainsMonoFontFamily),
            displaySmall = baseline.displaySmall.copy(fontFamily = jetbrainsMonoFontFamily),
            headlineLarge = baseline.headlineLarge.copy(fontFamily = jetbrainsMonoFontFamily),
            headlineMedium = baseline.headlineMedium.copy(fontFamily = jetbrainsMonoFontFamily),
            headlineSmall = baseline.headlineSmall.copy(fontFamily = jetbrainsMonoFontFamily),
            titleLarge = baseline.titleLarge.copy(fontFamily = jetbrainsMonoFontFamily),
            titleMedium = baseline.titleMedium.copy(fontFamily = jetbrainsMonoFontFamily),
            titleSmall = baseline.titleSmall.copy(fontFamily = jetbrainsMonoFontFamily),
            bodyLarge = baseline.bodyLarge.copy(fontFamily = interFontFamily),
            bodyMedium = baseline.bodyMedium.copy(fontFamily = interFontFamily),
            bodySmall = baseline.bodySmall.copy(fontFamily = interFontFamily),
            labelLarge = baseline.labelLarge.copy(fontFamily = interFontFamily),
            labelMedium = baseline.labelMedium.copy(fontFamily = interFontFamily),
            labelSmall = baseline.labelSmall.copy(fontFamily = interFontFamily),
        )
    }
}



