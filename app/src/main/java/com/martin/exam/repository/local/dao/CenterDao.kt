package com.martin.exam.repository.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.martin.exam.repository.model.ZooCenterDataModel

@Dao
interface CenterDao {
    @Query("SELECT * FROM center_table ORDER BY id ASC")
    fun getCenters() : List<ZooCenterDataModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(centerDataModel: List<ZooCenterDataModel>)
}