package com.fermilabz.demo.kotlinservice.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.tcp.ProxyProvider


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
    /*   @Bean
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
   */
//    @Bean
//    @Profile("dev")
//    fun particleWebClient() : WebClient {
//        return WebClient.builder()
//                .baseUrl(BASE_URL)
//                .defaultHeader(HttpHeaders.CONTENT_TYPE, MIME_TYPE)
//                .build()
//    }

    @Bean
    @Profile("dev")
    fun alphaWebClient() : WebClient {
        return WebClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MIME_TYPE)
                .build()
    }



}