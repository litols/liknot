package com.leafgraph.liknot.configuration

import com.leafgraph.liknot.reconciler.NodePrintingReconciler
import io.kubernetes.client.extended.controller.Controller
import io.kubernetes.client.extended.controller.ControllerManager
import io.kubernetes.client.extended.controller.builder.ControllerBuilder
import io.kubernetes.client.informer.SharedInformerFactory
import io.kubernetes.client.openapi.models.V1Node
import io.kubernetes.client.openapi.models.V1Pod
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.atomic.AtomicReference

@Configuration(proxyBeanMethods = false)
class KubernetesControllerConfiguration {

    private val controllerManagerReference = AtomicReference<ControllerManager>()

    @Bean
    fun controllerRunner(
        sharedInformerFactory: SharedInformerFactory,
        controllers: Array<Controller>
    ): ApplicationRunner {
        return ApplicationRunner { _ ->
            @Suppress("SpreadOperator")
            val controllerManager = ControllerManager(sharedInformerFactory, *controllers)
            controllerManagerReference.set(controllerManager)
            controllerManager.run()
        }
    }

    @Bean
    fun nodePrintingController(
        sharedInformerFactory: SharedInformerFactory,
        reconciler: NodePrintingReconciler
    ): Controller {
        return ControllerBuilder.defaultBuilder(sharedInformerFactory)
            .watch { queue -> ControllerBuilder.controllerWatchBuilder(V1Node::class.java, queue).build() }
            .watch { queue -> ControllerBuilder.controllerWatchBuilder(V1Pod::class.java, queue).build() }
            .withReconciler(reconciler)
            .withWorkerCount(1)
            .withReadyFunc { reconciler.informerReady() }
            .build()
    }
}
