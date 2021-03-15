package com.martin.exam.repository.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.martin.exam.repository.model.PlantsDataModel


@Dao
interface PlantsDao {
    @Query("SELECT * FROM plants_table WHERE location LIKE '%' || :centerName  || '%'  OR location LIKE '%' || '全園普遍分佈'  || '%' ORDER BY nameEn ASC")
    suspend fun getPlants(centerName : String) : List<PlantsDataModel>

    @Query("SELECT * FROM plants_table ORDER BY nameEn ASC")
    suspend fun getAllPlant() : List<PlantsDataModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plantsDataModel: List<PlantsDataModel>)
}