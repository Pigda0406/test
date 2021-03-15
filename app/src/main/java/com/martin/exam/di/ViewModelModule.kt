package com.martin.exam.di

import com.martin.exam.ui.main.MainViewModel
import com.martin.exam.ui.zooDetail.ZooDetailViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainViewModel(repository = get()) }
    viewModel { ZooDetailViewModel(repository = get()) }
}