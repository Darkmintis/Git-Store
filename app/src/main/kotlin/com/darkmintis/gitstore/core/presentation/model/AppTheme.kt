package com.darkmintis.gitstore.core.presentation.model

import com.darkmintis.gitstore.R

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color








import androidx.compose.ui.res.stringResource
import com.darkmintis.gitstore.core.presentation.theme.amberOrangeDark
import com.darkmintis.gitstore.core.presentation.theme.amberOrangeLight
import com.darkmintis.gitstore.core.presentation.theme.deepPurpleDark
import com.darkmintis.gitstore.core.presentation.theme.deepPurpleLight
import com.darkmintis.gitstore.core.presentation.theme.forestGreenDark
import com.darkmintis.gitstore.core.presentation.theme.forestGreenLight
import com.darkmintis.gitstore.core.presentation.theme.oceanBlueDark
import com.darkmintis.gitstore.core.presentation.theme.oceanBlueLight
import com.darkmintis.gitstore.core.presentation.theme.slateGrayDark
import com.darkmintis.gitstore.core.presentation.theme.slateGrayLight

enum class AppTheme(
    val lightScheme: ColorScheme?,
    val darkScheme: ColorScheme?,
    val primaryColor: Color?
) {
    DYNAMIC(null, null, null),
    OCEAN(oceanBlueLight, oceanBlueDark, Color(0xFF2A638A)),
    PURPLE(deepPurpleLight, deepPurpleDark, Color(0xFF6750A4)),
    FOREST(forestGreenLight, forestGreenDark, Color(0xFF356859)),
    SLATE(slateGrayLight, slateGrayDark, Color(0xFF535E6C)),
    AMBER(amberOrangeLight, amberOrangeDark, Color(0xFF8B5000));

    @Composable
    fun displayName(): String = stringResource(
        when (this) {
            DYNAMIC -> R.string.theme_dynamic
            OCEAN -> R.string.theme_ocean
            PURPLE -> R.string.theme_purple
            FOREST -> R.string.theme_forest
            SLATE -> R.string.theme_slate
            AMBER -> R.string.theme_amber
        }
    )

    companion object {
        fun fromName(name: String?): AppTheme =
            entries.find { it.name == name } ?: OCEAN
    }
}




