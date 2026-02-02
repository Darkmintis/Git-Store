package com.darkmintis.gitstore

sealed interface MainAction {
    data object DismissRateLimitDialog : MainAction
}

