package com.leafgraph.liknot.configuration

import com.leafgraph.liknot.reconciler.IngressReconciler
import com.leafgraph.liknot.reconciler.NodePrintingReconciler
import io.kubernetes.client.extended.controller.Controller
import io.kubernetes.client.extended.controller.ControllerManager
import io.kubernetes.client.extended.controller.builder.ControllerBuilder
import io.kubernetes.client.informer.SharedInformerFactory
import io.kubernetes.client.openapi.models.V1EndpointSlice
import io.kubernetes.client.openapi.models.V1Ingress
import io.kubernetes.client.openapi.models.V1Node
import io.kubernetes.client.openapi.models.V1Service
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class KubernetesControllerConfiguration {

    @Bean
    fun controllerManager(
        sharedInformerFactory: SharedInformerFactory,
        controllers: Array<Controller>
    ): ControllerManager {
        return ControllerManager(sharedInformerFactory, *controllers)
    }

    @Bean
    fun nodePrintingController(
        sharedInformerFactory: SharedInformerFactory,
        reconciler: NodePrintingReconciler
    ): Controller {
        return ControllerBuilder.defaultBuilder(sharedInformerFactory)
            .watch { queue -> ControllerBuilder.controllerWatchBuilder(V1Node::class.java, queue).build() }
            .withReconciler(reconciler)
            .withWorkerCount(1)
            .withReadyFunc { reconciler.informerReady() }
            .build()
    }

    @Bean
    fun ingressController(
        sharedInformerFactory: SharedInformerFactory,
        reconciler: IngressReconciler
    ): Controller {
        return ControllerBuilder.defaultBuilder(sharedInformerFactory)
            .watch { queue -> ControllerBuilder.controllerWatchBuilder(V1Ingress::class.java, queue).build() }
            .watch { queue -> ControllerBuilder.controllerWatchBuilder(V1Service::class.java, queue).build() }
            .watch { queue ->
                ControllerBuilder.controllerWatchBuilder(V1EndpointSlice::class.java, queue).build()
            }
            .withReconciler(reconciler)
            .withWorkerCount(1)
            .withReadyFunc { reconciler.informerReady() }
            .build()
    }
}
