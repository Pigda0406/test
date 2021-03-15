package com.martin.exam.di


import android.app.Application
import androidx.room.Room
import com.martin.exam.repository.local.dao.CenterDao
import com.martin.exam.repository.local.dao.PlantsDao
import com.martin.exam.repository.local.database.AppDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val databaseModule = module {

    fun provideDatabase(application: Application): AppDatabase {
       return Room.databaseBuilder(application, AppDatabase::class.java, "appDataBase")
            .fallbackToDestructiveMigration()
            .build()
    }

    fun provideCenterDao(database: AppDatabase): CenterDao {
        return  database.centerDao()
    }

    fun providePlantsDao(database: AppDatabase): PlantsDao {
        return  database.plantsDao()
    }

    single { provideDatabase(androidApplication()) }
    single { provideCenterDao(get()) }
    single { providePlantsDao(get()) }


}
