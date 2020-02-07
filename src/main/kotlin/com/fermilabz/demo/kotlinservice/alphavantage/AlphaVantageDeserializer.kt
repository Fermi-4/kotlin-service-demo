package com.fermilabz.demo.kotlinservice.alphavantage

import com.google.gson.*
import lombok.extern.log4j.Log4j2
import org.springframework.stereotype.Component
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Log4j2
@Component
class ResponseDeserializer : JsonDeserializer<AlphaVantageResponse> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): AlphaVantageResponse {

        var jsonObj = json?.asJsonObject

        var timeSeriesJson = jsonObj?.get("Time Series (1min)")?.asJsonObject
        var metaDataJson = jsonObj?.get("Meta Data")?.asJsonObject

        var seriesList: MutableList<TimeSeriesEntry> = ArrayList<TimeSeriesEntry>()

        var metaData = MetaData(
                metaDataJson?.get("1. Information").toString(),
                metaDataJson?.get("2. Symbol").toString(),
                metaDataJson?.get("3. Last Refreshed").toString(),
                metaDataJson?.get("4. Interval").toString(),
                metaDataJson?.get("5. Output Size").toString(),
                metaDataJson?.get("6. Time Zone").toString()
        )

        var dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        for (key in timeSeriesJson?.keySet().orEmpty()) {
            var seriesEntry = TimeSeriesEntry(
                    LocalDateTime.parse(key, dateTimeFormatter),
                    timeSeriesJson?.get(key)?.asJsonObject?.get("1. open").toString(),
                    timeSeriesJson?.get(key)?.asJsonObject?.get("2. high").toString(),
                    timeSeriesJson?.get(key)?.asJsonObject?.get("3. low").toString(),
                    timeSeriesJson?.get(key)?.asJsonObject?.get("4. close").toString(),
                    timeSeriesJson?.get(key)?.asJsonObject?.get("5. volume").toString()
                    )
            seriesList.add( seriesEntry )
        }
        return AlphaVantageResponse(metaData, seriesList)
    }
}
