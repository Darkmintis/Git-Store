package com.darkmintis.gitstore.app.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import com.darkmintis.gitstore.core.data.services.AndroidApkInfoExtractor
import com.darkmintis.gitstore.core.data.services.AndroidLocalizationManager
import com.darkmintis.gitstore.core.data.services.AndroidPackageMonitor
import com.darkmintis.gitstore.core.data.services.PackageMonitor
import com.darkmintis.gitstore.core.data.local.data_store.createDataStore
import com.darkmintis.gitstore.core.data.local.db.AppDatabase
import com.darkmintis.gitstore.core.data.local.db.initDatabase
import com.darkmintis.gitstore.core.presentation.utils.AndroidAppLauncher
import com.darkmintis.gitstore.core.presentation.utils.AndroidBrowserHelper
import com.darkmintis.gitstore.core.presentation.utils.AndroidClipboardHelper
import com.darkmintis.gitstore.core.presentation.utils.AppLauncher
import com.darkmintis.gitstore.core.presentation.utils.BrowserHelper
import com.darkmintis.gitstore.core.presentation.utils.ClipboardHelper
import com.darkmintis.gitstore.feature.auth.data.AndroidTokenStore
import com.darkmintis.gitstore.feature.auth.data.TokenStore
import com.darkmintis.gitstore.core.data.services.AndroidDownloader
import com.darkmintis.gitstore.core.data.services.AndroidFileLocationsProvider
import com.darkmintis.gitstore.core.data.services.AndroidInstaller
import com.darkmintis.gitstore.core.data.services.Downloader
import com.darkmintis.gitstore.core.data.services.FileLocationsProvider
import com.darkmintis.gitstore.core.data.services.Installer
import com.darkmintis.gitstore.core.data.services.LocalizationManager

val platformModule: Module = module {
    single<Downloader> {
        AndroidDownloader(
            context = get(),
            files = get()
        )
    }

    single<Installer> {
        AndroidInstaller(
            context = get(),
            apkInfoExtractor = AndroidApkInfoExtractor(androidContext())
        )
    }

    single<FileLocationsProvider> {
        AndroidFileLocationsProvider(context = get())
    }

    single<DataStore<Preferences>> {
        createDataStore(androidContext())
    }

    single<BrowserHelper> {
        AndroidBrowserHelper(androidContext())
    }

    single<ClipboardHelper> {
        AndroidClipboardHelper(androidContext())
    }

    single<TokenStore> {
        AndroidTokenStore(
            dataStore = get()
        )
    }

    single<AppDatabase> {
        initDatabase(androidContext())
    }

    single<PackageMonitor> {
        AndroidPackageMonitor(androidContext())
    }

    single<LocalizationManager> {
        AndroidLocalizationManager()
    }

    single<AppLauncher> {
        AndroidAppLauncher(androidContext())
    }
}

