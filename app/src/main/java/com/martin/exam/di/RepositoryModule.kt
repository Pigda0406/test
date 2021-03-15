package com.martin.exam.di

import android.content.Context
import com.martin.exam.repository.ZooRepository
import com.martin.exam.repository.ZooRepositoryImpl
import com.martin.exam.repository.local.dao.CenterDao
import com.martin.exam.repository.local.dao.PlantsDao
import com.martin.exam.repository.remote.ZooApi
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {

    fun provideZooRepository(api: ZooApi, context: Context, centerDao : CenterDao,plantDao:PlantsDao): ZooRepository {
        return ZooRepositoryImpl(api, context, centerDao,plantDao)
    }

    single { provideZooRepository(get(), androidContext(), get(),get()) }

}