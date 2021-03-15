package com.martin.exam.di

import com.martin.exam.repository.remote.ZooApi
import org.koin.dsl.module
import retrofit2.Retrofit

val apiModule = module {

    fun provideCountriesApi(retrofit: Retrofit): ZooApi {
        return retrofit.create(ZooApi::class.java)
    }
    single { provideCountriesApi(get()) }

}