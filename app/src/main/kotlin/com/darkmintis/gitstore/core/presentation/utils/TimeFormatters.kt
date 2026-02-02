package com.darkmintis.gitstore.core.presentation.utils

import android.content.Context
import com.darkmintis.gitstore.R
import kotlinx.datetime.Clock as DateClock
import kotlinx.datetime.Instant as DateInstant
import androidx.compose.runtime.Composable
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.ui.res.stringResource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

@Composable
fun formatUpdatedAt(isoInstant: String): String {
    val updated = DateInstant.parse(isoInstant)
    val now = DateInstant.fromEpochMilliseconds(DateClock.System.now().toEpochMilliseconds())
    val diff: Duration = now - updated

    val hoursDiff = diff.inWholeHours
    val daysDiff = diff.inWholeDays

    return when {
        hoursDiff < 1 -> stringResource(R.string.updated_just_now)
        hoursDiff < 24 -> stringResource(R.string.updated_hours_ago, hoursDiff)
        daysDiff == 1L -> stringResource(R.string.updated_yesterday)
        daysDiff < 7 -> stringResource(R.string.updated_days_ago, daysDiff)
        else -> {
            val date = updated.toLocalDateTime(TimeZone.currentSystemDefault()).date
            stringResource(R.string.updated_on_date, date.toString())
        }
    }
}

suspend fun formatUpdatedAt(epochMillis: Long, context: Context): String {
    val updated = DateInstant.fromEpochMilliseconds(epochMillis)
    val now = DateClock.System.now()
    val diff: Duration = now - updated

    val hoursDiff = diff.inWholeHours
    val daysDiff = diff.inWholeDays

    return when {
        hoursDiff < 1 ->
            context.getString(R.string.updated_just_now)

        hoursDiff < 24 ->
            context.getString(R.string.updated_hours_ago, hoursDiff)

        daysDiff == 1L ->
            context.getString(R.string.updated_yesterday)

        daysDiff < 7 ->
            context.getString(R.string.updated_days_ago, daysDiff)

        else -> {
            val date = updated
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
            context.getString(R.string.updated_on_date, date.toString())
        }
    }
}
suspend fun formatAddedAt(epochMillis: Long, context: Context): String {
    val updated = DateInstant.fromEpochMilliseconds(epochMillis)
    val now = DateClock.System.now()
    val diff: Duration = now - updated

    val hoursDiff = diff.inWholeHours
    val daysDiff = diff.inWholeDays

    return when {
        hoursDiff < 1 ->
            context.getString(R.string.added_just_now)

        hoursDiff < 24 ->
            context.getString(R.string.added_hours_ago, hoursDiff)

        daysDiff == 1L ->
            context.getString(R.string.added_yesterday)

        daysDiff < 7 ->
            context.getString(R.string.added_days_ago, daysDiff)

        else -> {
            val date = updated
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
            context.getString(R.string.added_on_date, date.toString())
        }
    }
}





