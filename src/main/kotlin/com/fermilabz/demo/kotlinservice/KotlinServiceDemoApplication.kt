package com.fermilabz.demo.kotlinservice

import com.fermilabz.demo.kotlinservice.alphavantage.AlphaVantageResponse
import com.fermilabz.demo.kotlinservice.alphavantage.ResponseDeserializer
import com.google.gson.GsonBuilder
import lombok.extern.log4j.Log4j2
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.netty.http.client.HttpClient
import reactor.netty.tcp.ProxyProvider
import java.time.Duration
import java.time.LocalDateTime


@SpringBootApplication
class KotlinServiceDemoApplication

fun main(args: Array<String>) {
    runApplication<KotlinServiceDemoApplication>(*args)
}

@Controller
class RSocketController(val priceService: PriceService) {

    @MessageMapping("stockPrices")
    fun pricesFor(symbol: String): Flux<StockPrice> = priceService.generatePrices(symbol)

}

@Log4j2
@Service
class PriceService(@Value(value = "\${alpha.vantage.delay-in-minutes}") val delayInMinutes: Long,
                   @Value(value = "\${alpha.vantage.api-key}") val apiKey: String,
                   var alphaWebClient: WebClient,
                   var alphaResponseDeserializer: ResponseDeserializer) {

    fun generatePrices(symbol: String): Flux<StockPrice> {
        return getStockPrice(symbol)
    }

    fun getStockPrice(symbol: String): Flux<StockPrice> {
        var gsonBldr: GsonBuilder = GsonBuilder()
        gsonBldr.registerTypeAdapter(AlphaVantageResponse::class.java, ResponseDeserializer())
        return alphaWebClient.get()
                .uri("/query?function=TIME_SERIES_INTRADAY&symbol=" + symbol + "&interval=1min&apikey=" + apiKey)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String::class.java)
                .map { body -> gsonBldr.create().fromJson(body, AlphaVantageResponse::class.java) }
                .map { response -> StockPrice(response.metaDate.symbol, response.timeSeries.get(0).high.removeSurrounding("\"").toDouble(), response.timeSeries.get(0).date) }
                .delaySubscription(Duration.ofMinutes(2))
                .repeat().doOnError { t -> println(t.stackTrace.toString()) }
    }
}


@Log4j2
@RestController
class RestController(val priceService: PriceService) {

    @GetMapping(value = ["/stocks/{symbol}"],
produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
fun prices(@PathVariable symbol: String): Flux<StockPrice> {
        return priceService.generatePrices(symbol)
}
}

@Configuration
class WebClientConfig(@Value(value = "\${alpha.vantage.api-key}") val apiKey: String) {

    var MIME_TYPE: String = "application/json"
    var BASE_URL: String = "https://www.alphavantage.co"

    /**
     * At ti, we need to setup web client to work behind the corporate proxy
     */
    @Bean
    @Profile("ti")
    fun alphaWebClientWithProxy() : WebClient {
        var httpCient: HttpClient = HttpClient.create().tcpConfiguration { t -> t.proxy { proxy -> proxy.type(ProxyProvider.Proxy.HTTP).host("webproxy.ext.ti.com").port(80) } }
        var connector: ReactorClientHttpConnector = ReactorClientHttpConnector(httpCient)
        return WebClient
                .builder()
                .clientConnector(connector)
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MIME_TYPE)
                .build();
    }

    /**
     * Particle Rx web client that will be used to bridge between particle and kafka
     * hosted in AWS
     * Operates behind TI proxy
     */
    @Bean
    @Profile("ti")
    fun particleWebClientWithProxy() : WebClient {
        var httpCient: HttpClient = HttpClient.create().tcpConfiguration { t -> t.proxy { proxy -> proxy.type(ProxyProvider.Proxy.HTTP).host("webproxy.ext.ti.com").port(80) } }
        var connector: ReactorClientHttpConnector = ReactorClientHttpConnector(httpCient)
        return WebClient
                .builder()
                .clientConnector(connector)
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MIME_TYPE)
                .build();
    }

    @Bean
    @Profile("dev")
    fun particleWebClient() : WebClient {
        return WebClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MIME_TYPE)
                .build()
    }

    @Bean
    @Profile("dev")
    fun alphaWebClient() : WebClient {
        return WebClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MIME_TYPE)
                .build()
    }



}

data class StockPrice
    (
            val symbol: String,
            val price: Double,
            val time: LocalDateTime
    )

