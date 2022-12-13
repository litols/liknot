package com.leafgraph.liknot.reconciler

import io.kubernetes.client.extended.controller.reconciler.Reconciler
import io.kubernetes.client.extended.controller.reconciler.Request
import io.kubernetes.client.extended.controller.reconciler.Result
import io.kubernetes.client.informer.SharedIndexInformer
import io.kubernetes.client.informer.cache.Lister
import io.kubernetes.client.openapi.models.V1Node
import io.kubernetes.client.openapi.models.V1Pod
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class NodePrintingReconciler(
    private val nodeInformer: SharedIndexInformer<V1Node>,
    private val podInformer: SharedIndexInformer<V1Pod>
) : Reconciler {
    private val nodeLister: Lister<V1Node> = Lister(nodeInformer.indexer)
    private val podLister: Lister<V1Pod> = Lister(podInformer.indexer)

    fun informerReady(): Boolean {
        return podInformer.hasSynced() && nodeInformer.hasSynced()
    }

    override fun reconcile(request: Request): Result {
        val node = nodeLister[request.name]

        log.info { "get all pods in namespace $NAMESPACE" }
        podLister.namespace(NAMESPACE).list().stream()
            .map { pod: V1Pod ->
                pod.metadata!!
                    .name
            }
            .forEach { log.info { it } }

        log.info { "triggered reconciling:  ${node.metadata!!.name}" }
        return Result(false)
    }

    companion object {
        const val NAMESPACE = "default"
        private val log = KotlinLogging.logger {}
    }
}
