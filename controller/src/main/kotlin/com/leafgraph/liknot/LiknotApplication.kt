package com.leafgraph.liknot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LiknotApplication

fun main(args: Array<String>) {
    @Suppress("SpreadOperator")
    runApplication<LiknotApplication>(*args)
}
