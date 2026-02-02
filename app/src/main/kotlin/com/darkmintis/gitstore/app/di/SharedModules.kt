package com.darkmintis.gitstore.app.di

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import com.darkmintis.gitstore.MainViewModel
import com.darkmintis.gitstore.app.app_state.AppStateManager
import com.darkmintis.gitstore.core.data.services.PackageMonitor
import com.darkmintis.gitstore.core.data.data_source.DefaultTokenDataSource
import com.darkmintis.gitstore.core.data.data_source.TokenDataSource
import com.darkmintis.gitstore.core.data.local.db.AppDatabase
import com.darkmintis.gitstore.core.data.repository.FavouritesRepositoryImpl
import com.darkmintis.gitstore.core.data.repository.InstalledAppsRepositoryImpl
import com.darkmintis.gitstore.core.data.repository.StarredRepositoryImpl
import com.darkmintis.gitstore.core.data.repository.ThemesRepositoryImpl
import com.darkmintis.gitstore.core.domain.getPlatform
import com.darkmintis.gitstore.core.domain.repository.FavouritesRepository
import com.darkmintis.gitstore.core.domain.repository.InstalledAppsRepository
import com.darkmintis.gitstore.core.domain.repository.ThemesRepository
import com.darkmintis.gitstore.feature.apps.data.repository.AppsRepositoryImpl
import com.darkmintis.gitstore.feature.apps.domain.repository.AppsRepository
import com.darkmintis.gitstore.feature.apps.presentation.AppsViewModel
import com.darkmintis.gitstore.network.buildAuthedGitHubHttpClient
import com.darkmintis.gitstore.feature.auth.data.repository.AuthenticationRepositoryImpl
import com.darkmintis.gitstore.feature.auth.domain.repository.AuthenticationRepository
import com.darkmintis.gitstore.feature.auth.presentation.AuthenticationViewModel
import com.darkmintis.gitstore.feature.details.data.repository.DetailsRepositoryImpl
import com.darkmintis.gitstore.feature.details.domain.repository.DetailsRepository
import com.darkmintis.gitstore.feature.details.presentation.DetailsViewModel
import com.darkmintis.gitstore.core.data.services.Downloader
import com.darkmintis.gitstore.core.data.services.Installer
import com.darkmintis.gitstore.core.domain.repository.StarredRepository
import com.darkmintis.gitstore.core.domain.use_cases.SyncInstalledAppsUseCase
import com.darkmintis.gitstore.feature.developer_profile.data.repository.DeveloperProfileRepositoryImpl
import com.darkmintis.gitstore.feature.developer_profile.domain.repository.DeveloperProfileRepository
import com.darkmintis.gitstore.feature.favourites.presentation.FavouritesViewModel
import com.darkmintis.gitstore.feature.home.data.data_source.CachedTrendingDataSource
import com.darkmintis.gitstore.feature.home.data.repository.HomeRepositoryImpl
import com.darkmintis.gitstore.feature.home.domain.repository.HomeRepository
import com.darkmintis.gitstore.feature.home.presentation.HomeViewModel
import com.darkmintis.gitstore.feature.developer_profile.presentation.DeveloperProfileViewModel
import com.darkmintis.gitstore.feature.search.data.repository.SearchRepositoryImpl
import com.darkmintis.gitstore.feature.search.domain.repository.SearchRepository
import com.darkmintis.gitstore.feature.search.presentation.SearchViewModel
import com.darkmintis.gitstore.feature.settings.data.repository.SettingsRepositoryImpl
import com.darkmintis.gitstore.feature.settings.domain.repository.SettingsRepository
import com.darkmintis.gitstore.feature.settings.presentation.SettingsViewModel
import com.darkmintis.gitstore.feature.starred_repos.presentation.StarredReposViewModel
import com.darkmintis.gitstore.network.RateLimitHandler

val coreModule: Module = module {
    // Token Management
    single<TokenDataSource> {
        DefaultTokenDataSource(
            tokenStore = get()
        )
    }

    // Rate Limiting
    single { RateLimitHandler() }

    // App State Management
    single {
        AppStateManager(
            rateLimitHandler = get(),
            tokenDataSource = get()
        )
    }

    // Platform
    single { getPlatform() }

    // HTTP Client
    single {
        buildAuthedGitHubHttpClient(
            tokenDataSource = get(),
            rateLimitHandler = get()
        )
    }

    // Theme Management
    single<ThemesRepository> {
        ThemesRepositoryImpl(
            preferences = get()
        )
    }

    single {
        CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    // Database DAOs (kept for repositories that need them)
    single { get<AppDatabase>().installedAppDao }
    single { get<AppDatabase>().favoriteRepoDao }
    single { get<AppDatabase>().updateHistoryDao }
    single { get<AppDatabase>().starredReposDao }

    single<SyncInstalledAppsUseCase> {
        SyncInstalledAppsUseCase(
            packageMonitor = get(),
            installedAppsRepository = get(),
            platform = get()
        )
    }

    // Repositories
    single<FavouritesRepository> {
        FavouritesRepositoryImpl(
            dao = get(),
            installedAppsDao = get(),
            detailsRepository = get()
        )
    }

    single<InstalledAppsRepository> {
        InstalledAppsRepositoryImpl(
            database = get(),
            dao = get(),
            historyDao = get(),
            detailsRepository = get(),
            installer = get(),
            downloader = get()
        )
    }

    // ViewModels
    viewModel {
        MainViewModel(
            tokenDataSource = get(),
            themesRepository = get(),
            appStateManager = get(),
            installedAppsRepository = get(),
            packageMonitor = get(),
            platform = get()
        )
    }
}

val authModule: Module = module {
    // Repository
    single<AuthenticationRepository> {
        AuthenticationRepositoryImpl(tokenDataSource = get())
    }

    // ViewModel
    viewModel {
        AuthenticationViewModel(
            authenticationRepository = get(),
            browserHelper = get(),
            clipboardHelper = get(),
            scope = get(),
            application = get()
        )
    }
}

val homeModule: Module = module {
    // Repository
    single<HomeRepository> {
        HomeRepositoryImpl(
            githubNetworkClient = get(),
            platform = get(),
            appStateManager = get(),
            cachedDataSource = get()
        )
    }

    single<CachedTrendingDataSource> {
        CachedTrendingDataSource(
            platform = get()
        )
    }

    // ViewModel
    viewModel {
        HomeViewModel(
            application = get(),
            homeRepository = get(),
            installedAppsRepository = get(),
            platform = get(),
            syncInstalledAppsUseCase = get(),
            favouritesRepository = get(),
            starredRepository = get()
        )
    }
}

val searchModule: Module = module {
    // Repository
    single<SearchRepository> {
        SearchRepositoryImpl(
            githubNetworkClient = get(),
            appStateManager = get()
        )
    }


    // ViewModel
    viewModel {
        SearchViewModel(
            application = get(),
            searchRepository = get(),
            installedAppsRepository = get(),
            syncInstalledAppsUseCase = get(),
            favouritesRepository = get(),
            starredRepository = get()
        )
    }
}
val favouritesModule: Module = module {
    // ViewModel
    viewModel {
        FavouritesViewModel(
            application = androidContext() as Application,
            favouritesRepository = get()
        )
    }
}

val detailsModule: Module = module {
    // Repository
    single<DetailsRepository> {
        DetailsRepositoryImpl(
            github = get(),
            appStateManager = get(),
            localizationManager = get()
        )
    }

    // ViewModel
    viewModel { params ->
        DetailsViewModel(
            repositoryId = params.get(),
            detailsRepository = get(),
            downloader = get<Downloader>(),
            installer = get<Installer>(),
            platform = get(),
            helper = get(),
            installedAppsRepository = get(),
            favouritesRepository = get(),
            packageMonitor = get<PackageMonitor>(),
            syncInstalledAppsUseCase = get(),
            starredRepository = get(),
            application = get()
        )
    }
}
val repoAuthorModule: Module = module {
    // Repository
    single<DeveloperProfileRepository> {
        DeveloperProfileRepositoryImpl(
            httpClient = get(),
            platform = get(),
            installedAppsDao = get(),
            favouritesRepository = get()
        )
    }

    // ViewModel
    viewModel { params ->
        DeveloperProfileViewModel(
            application = get(),
            repository = get(),
            favouritesRepository = get(),
            username = params.get(),
        )
    }
}

val settingsModule: Module = module {
    // Repository
    single<SettingsRepository> {
        SettingsRepositoryImpl(
            tokenDataSource = get()
        )
    }

    // ViewModel
    viewModel {
        SettingsViewModel(
            browserHelper = get(),
            themesRepository = get(),
            settingsRepository = get()
        )
    }
}
val starredReposModule: Module = module {
    // Repository
    single<StarredRepository> {
        StarredRepositoryImpl(
            httpClient = get(),
            dao = get(),
            installedAppsDao = get(),
            platform = get()
        )
    }

    // ViewModel
    viewModel {
        StarredReposViewModel(
            application = get(),
            starredRepository = get(),
            favouritesRepository = get(),
            tokenDataSource = get()
        )
    }
}

val appsModule: Module = module {
    // Repository
    single<AppsRepository> {
        AppsRepositoryImpl(
            appLauncher = get(),
            appsRepository = get()
        )
    }

    // ViewModel
    viewModel {
        AppsViewModel(
            appsRepository = get(),
            installedAppsRepository = get(),
            installer = get(),
            downloader = get(),
            packageMonitor = get(),
            detailsRepository = get(),
            platform = get(),
            syncInstalledAppsUseCase = get(),
            application = get()
        )
    }
}

