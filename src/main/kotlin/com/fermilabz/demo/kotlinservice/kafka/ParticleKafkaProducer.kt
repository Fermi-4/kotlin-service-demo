package com.fermilabz.demo.kotlinservice.kafka

import org.springframework.beans.factory.annotation.Value
import java.util.*

class ParticleKafkaProducer(@Value("\${kafka.servers.fermi}") var BOOTSTRAP_SERVERS: String,
                            @Value("\${kafka.topics.particle}") var TOPIC: String) {



    fun getProducer() {
        var properties: Properties = Properties()
    }

}