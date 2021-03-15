package com.martin.exam.repository.remote

import com.martin.exam.repository.model.PlantsResponse
import com.martin.exam.repository.model.ZooCenterResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ZooApi {

    @GET("5a0e5fbb-72f8-41c6-908e-2fb25eff9b8a")
    suspend fun getZooPlace(
        @Query("scope") scope: String
    ): Response<ZooCenterResponse>

    @GET("f18de02f-b6c9-47c0-8cda-50efad621c14")
    suspend fun getPlants(
            @Query("scope") scope: String
    ): Response<PlantsResponse>
}