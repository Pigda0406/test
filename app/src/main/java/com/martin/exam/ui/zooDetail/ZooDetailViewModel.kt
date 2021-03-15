package com.martin.exam.ui.zooDetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martin.exam.repository.ZooRepository
import com.martin.exam.repository.model.PlantsDataModel
import kotlinx.coroutines.launch


class ZooDetailViewModel(private val repository: ZooRepository)  : ViewModel() {
    val zooPlants = MutableLiveData<List<PlantsDataModel>>()

    fun getZooPlants(name:String) {
        viewModelScope.launch {
            zooPlants.postValue(repository.getPlantByName(name))
        }
    }


}