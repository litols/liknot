package com.leafgraph.liknot.reconciler

import com.leafgraph.liknot.converter.K8sToEnvoyConfigConverter
import com.leafgraph.liknot.store.EnvoyConfigurationStore
import io.kubernetes.client.extended.controller.reconciler.Reconciler
import io.kubernetes.client.extended.controller.reconciler.Request
import io.kubernetes.client.extended.controller.reconciler.Result
import io.kubernetes.client.informer.SharedIndexInformer
import io.kubernetes.client.informer.cache.Lister
import io.kubernetes.client.openapi.models.V1EndpointSlice
import io.kubernetes.client.openapi.models.V1Ingress
import io.kubernetes.client.openapi.models.V1Service
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class IngressReconciler(
    private val ingressInformer: SharedIndexInformer<V1Ingress>,
    private val serviceInformer: SharedIndexInformer<V1Service>,
    private val endpointSliceInformer: SharedIndexInformer<V1EndpointSlice>,
    private val envoyConfigurationStore: EnvoyConfigurationStore
) : Reconciler {
    private val ingressLister: Lister<V1Ingress> = Lister(ingressInformer.indexer)
    private val endpointSliceLister: Lister<V1EndpointSlice> = Lister(endpointSliceInformer.indexer)
    private val serviceLister: Lister<V1Service> = Lister(serviceInformer.indexer)

    fun informerReady(): Boolean {
        return listOf(
            ingressInformer.hasSynced(),
            serviceInformer.hasSynced(),
            endpointSliceInformer.hasSynced()
        ).all { it }
    }

    override fun reconcile(request: Request): Result {
        val envoyCluster = serviceLister.list()
            .asSequence()
            .map { service ->
                K8sToEnvoyConfigConverter.convertToCluster(service)
            }.toList()

        val envoyCla = endpointSliceLister.list()
            .asSequence()
            .map { endpointSlice ->
                K8sToEnvoyConfigConverter.convertToClusterLoadAssignment(endpointSlice)
            }.toList()

        val envoyRoutes = ingressLister.list()
            .asSequence()
            .filter { ingress ->
                ingress.spec?.ingressClassName == INGRESS_CLASS_NAME
            }.map { ingress ->
                K8sToEnvoyConfigConverter.convertToRouteConfiguration(ingress.spec!!)
            }.toList()

        envoyConfigurationStore.reflect(
            clusters = envoyCluster,
            clusterLoadAssignments = envoyCla,
            routes = envoyRoutes
        )
        return Result(false)
    }

    companion object {
        const val NAMESPACE = "default"
        const val INGRESS_CLASS_NAME = "liknot"
        private val log = KotlinLogging.logger {}
    }
}
