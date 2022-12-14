package com.leafgraph.liknot.configuration

import io.kubernetes.client.extended.controller.ControllerManager
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.concurrent.Executors

@Component
class ControllerManagerRunner(
    private val controllerManager: ControllerManager
) {

    @EventListener(ApplicationReadyEvent::class)
    fun startController() {
        Executors.newSingleThreadExecutor().execute {
            log.info { "starting controller manager" }
            controllerManager.run()
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
