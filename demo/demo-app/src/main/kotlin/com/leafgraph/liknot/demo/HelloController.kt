package com.leafgraph.liknot.demo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloController {

    @GetMapping("/hello")
    fun hello(): HelloResponse {
        return HelloResponse()
    }
}

data class HelloResponse(
    val data: String = "123"
)
