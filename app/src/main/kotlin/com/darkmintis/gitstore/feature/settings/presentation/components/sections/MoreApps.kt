package com.darkmintis.gitstore.feature.settings.presentation.components.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.Context
import coil3.compose.AsyncImage
import com.darkmintis.gitstore.feature.settings.presentation.SettingsAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URL
import androidx.compose.ui.platform.LocalContext

private const val PROMO_APPS_URL = "https://darkmintis.dev/promo-apps.json"
private const val PREFS_NAME = "promo_apps_cache"
private const val PREFS_KEY_JSON = "promo_json"
private const val PREFS_KEY_TIMESTAMP = "promo_timestamp"
private const val CACHE_TTL_MS = 24 * 60 * 60 * 1000L

private data class PromoApp(
    val iconUrl: String,
    val playStoreUrl: String,
    val enabled: Boolean
)

private suspend fun fetchBlinkPromoApp(context: Context): PromoApp? = withContext(Dispatchers.IO) {
    runCatching {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cachedJson = prefs.getString(PREFS_KEY_JSON, null)
        val cachedTimestamp = prefs.getLong(PREFS_KEY_TIMESTAMP, 0L)
        val now = System.currentTimeMillis()
        val payload = if (cachedJson != null && (now - cachedTimestamp) < CACHE_TTL_MS) {
            cachedJson
        } else {
            val fresh = URL(PROMO_APPS_URL).readText()
            prefs.edit()
                .putString(PREFS_KEY_JSON, fresh)
                .putLong(PREFS_KEY_TIMESTAMP, now)
                .apply()
            fresh
        }
        val root = Json.parseToJsonElement(payload).jsonObject
        val apps = root["apps"]?.jsonObject ?: return@runCatching null
        val blink = apps["blink"]?.jsonObject ?: return@runCatching null

        val iconUrl = blink["icon"]?.jsonPrimitive?.content.orEmpty()
        val playStoreUrl = blink["play_store"]?.jsonPrimitive?.content.orEmpty()
        val enabled = blink["enabled"]?.jsonPrimitive?.contentOrNull?.toBooleanStrictOrNull() ?: false

        if (iconUrl.isBlank() || playStoreUrl.isBlank()) return@runCatching null

        PromoApp(
            iconUrl = iconUrl,
            playStoreUrl = playStoreUrl,
            enabled = enabled
        )
    }.getOrNull()
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun LazyListScope.moreApps(
    onAction: (SettingsAction) -> Unit,
) {
    item {
        // Section Header with gradient background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFF6B35), // Orange
                            Color(0xFFFF4136)  // Red
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "More Apps by Darkmintis",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        val context = LocalContext.current
        val blinkPromo by produceState<PromoApp?>(initialValue = null) {
            value = fetchBlinkPromoApp(context)
        }

        val promoApp = blinkPromo
        PromoSlotsRow(
            blinkIconUrl = promoApp?.takeIf { it.enabled }?.iconUrl,
            onBlinkClick = {
                val playStoreUrl = promoApp?.takeIf { it.enabled }?.playStoreUrl ?: return@PromoSlotsRow
                onAction(SettingsAction.OnBrowserOpen(playStoreUrl))
            }
        )
    }
}

@Composable
private fun PromoSlotsRow(
    blinkIconUrl: String?,
    onBlinkClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { index ->
            val isBlinkSlot = index == 0 && !blinkIconUrl.isNullOrBlank()

            ElevatedCard(
                onClick = {
                    if (isBlinkSlot) {
                        onBlinkClick()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (isBlinkSlot) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
                    }
                ),
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = if (isBlinkSlot) 4.dp else 0.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (isBlinkSlot) {
                                Modifier
                            } else {
                                Modifier.border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isBlinkSlot) {
                        AsyncImage(
                            model = blinkIconUrl,
                            contentDescription = "Blink",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp))
                        )
                    }
                }
            }
        }
    }
}
