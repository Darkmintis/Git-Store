package com.darkmintis.gitstore.core.domain

import com.darkmintis.gitstore.core.domain.model.PlatformType

class AndroidPlatform : Platform {
    override val type: PlatformType
        get() = PlatformType.ANDROID

}

fun getPlatform(): Platform {
    return AndroidPlatform()
}


