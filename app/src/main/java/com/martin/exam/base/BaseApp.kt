package com.martin.exam.base

import android.app.Application
import com.martin.exam.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class BaseApp : Application() {

    override fun onCreate() {
        super.onCreate()
      startKoin {
            androidLogger()
            androidContext(this@BaseApp)
            modules(
                apiModule,
                viewModelModule,
                repositoryModule,
                networkModule, databaseModule
            )
        }
    }
}