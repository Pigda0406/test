package com.martin.exam.repository

import com.martin.exam.repository.model.PlantsDataModel
import com.martin.exam.repository.model.ZooCenterDataModel

interface ZooRepository {
    suspend fun getAllZooPlace() : List<ZooCenterDataModel>
    suspend fun getPlantByName(zooName : String) : List<PlantsDataModel>

}




