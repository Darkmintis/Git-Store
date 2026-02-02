package com.darkmintis.gitstore.feature.details.presentation.utils

import android.os.Build

fun isLiquidTopbarEnabled(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
}



