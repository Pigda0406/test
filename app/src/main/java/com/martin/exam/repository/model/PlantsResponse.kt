package com.martin.exam.repository.model


data class PlantsResponse(
    var result: Result
) {
    data class Result(
        var count: Int,
        var limit: Int,
        var offset: Int,
        var results: List<PlantsDataModel>,
        var sort: String
    )
}