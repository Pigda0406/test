package com.martin.exam.repository.model


data class ZooCenterResponse(
    var result: Result
) {
    data class Result(
        var count: Int,
        var limit: Int,
        var offset: Int,
        var results: List<ZooCenterDataModel>,
        var sort: String
    )
}