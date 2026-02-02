package com.darkmintis.gitstore.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import com.darkmintis.gitstore.app.di.initKoin

class GithubStoreApp : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@GithubStoreApp)
        }
    }
}

