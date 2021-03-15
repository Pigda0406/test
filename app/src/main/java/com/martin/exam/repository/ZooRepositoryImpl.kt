package com.martin.exam.repository


import android.content.Context
import android.util.Log
import com.martin.exam.repository.local.dao.CenterDao
import com.martin.exam.repository.local.dao.PlantsDao
import com.martin.exam.repository.model.PlantsDataModel
import com.martin.exam.repository.model.ZooCenterDataModel
import com.martin.exam.repository.remote.ZooApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ZooRepositoryImpl(
    private val api: ZooApi,
    private val context: Context,
    private val centerDao: CenterDao,
    private val plantsDao: PlantsDao
) : ZooRepository {

    suspend fun fetchZooCenterData (){
        val response = api.getZooPlace("resourceAquire")
        centerDao.insert(response.body()?.result!!.results)
    }

    private suspend fun fetchPlantsData (){
        val response = api.getPlants("resourceAquire")
        plantsDao.insert(response.body()?.result!!.results)
    }

    override suspend fun getAllZooPlace(): List<ZooCenterDataModel> {
        fetchZooCenterData()
        return withContext(Dispatchers.IO) {
            centerDao.getCenters()
        }
    }

    override suspend fun getPlantByName(zooName: String): List<PlantsDataModel> {
        fetchPlantsData()
        var plants = withContext(Dispatchers.IO) {
            plantsDao.getAllPlant()
        }
        return if(plants.isNotEmpty()){
            Log.d("Martin","Local")
            plantsDao.getPlants(zooName)
        }else{
            withContext(Dispatchers.IO) {
                Log.d("Martin","Not Local")
                plantsDao.getPlants(zooName)
            }
        }
    }
}