package com.darkmintis.gitstore.app.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            platformModule,
            coreModule,
            authModule,
            homeModule,
            searchModule,
            favouritesModule,
            starredReposModule,
            detailsModule,
            repoAuthorModule,
            settingsModule,
            appsModule
        )
    }
}

