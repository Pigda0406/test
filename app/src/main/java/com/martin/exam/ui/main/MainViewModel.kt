package com.martin.exam.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martin.exam.repository.ZooRepository
import com.martin.exam.repository.model.ZooCenterDataModel
import kotlinx.coroutines.launch


class MainViewModel(private val repository: ZooRepository) : ViewModel() {
    val zooPlace = MutableLiveData<List<ZooCenterDataModel>>()

    fun getZooPlace() {
        viewModelScope.launch {
            zooPlace.postValue(repository.getAllZooPlace())
        }
    }


}