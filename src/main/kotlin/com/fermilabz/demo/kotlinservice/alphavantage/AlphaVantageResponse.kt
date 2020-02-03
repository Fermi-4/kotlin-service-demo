package com.fermilabz.demo.kotlinservice.alphavantage

import java.time.LocalDateTime

data class AlphaVantageResponse(
        val metaDate: MetaData,
        val timeSeries: MutableList<TimeSeriesEntry>
)

data class MetaData(
    val information: String,
    val symbol: String,
    val lastRefreshed: String,
    val interval: String,
    val outputSize: String,
    val timeZone: String
)

data class TimeSeriesEntry(
        val date: LocalDateTime,
        val open: String,
        val high: String,
        val low: String,
        val close: String,
        val volume: String
)
