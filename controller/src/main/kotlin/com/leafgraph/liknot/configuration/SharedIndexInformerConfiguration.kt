package com.leafgraph.liknot.configuration

import io.kubernetes.client.informer.SharedIndexInformer
import io.kubernetes.client.informer.SharedInformerFactory
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.models.V1Endpoints
import io.kubernetes.client.openapi.models.V1EndpointsList
import io.kubernetes.client.openapi.models.V1Node
import io.kubernetes.client.openapi.models.V1NodeList
import io.kubernetes.client.openapi.models.V1Pod
import io.kubernetes.client.openapi.models.V1PodList
import io.kubernetes.client.util.generic.GenericKubernetesApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class SharedIndexInformerConfiguration {

    @Bean
    fun endpointsInformer(
        apiClient: ApiClient,
        sharedInformerFactory: SharedInformerFactory
    ): SharedIndexInformer<V1Endpoints> {
        val genericApi = GenericKubernetesApi(
            V1Endpoints::class.java,
            V1EndpointsList::class.java,
            "",
            "v1",
            "endpoints",
            apiClient
        )
        return sharedInformerFactory.sharedIndexInformerFor(
            genericApi,
            V1Endpoints::class.java,
            0
        )
    }

    @Bean
    fun nodeInformer(
        apiClient: ApiClient,
        sharedInformerFactory: SharedInformerFactory
    ): SharedIndexInformer<V1Node> {
        val genericApi = GenericKubernetesApi(
            V1Node::class.java,
            V1NodeList::class.java,
            "",
            "v1",
            "nodes",
            apiClient
        )
        return sharedInformerFactory.sharedIndexInformerFor(
            genericApi,
            V1Node::class.java,
            60 * 1000L
        )
    }

    @Bean
    fun podInformer(
        apiClient: ApiClient,
        sharedInformerFactory: SharedInformerFactory
    ): SharedIndexInformer<V1Pod> {
        val genericApi = GenericKubernetesApi(
            V1Pod::class.java,
            V1PodList::class.java,
            "",
            "v1",
            "pods",
            apiClient
        )
        return sharedInformerFactory.sharedIndexInformerFor(
            genericApi,
            V1Pod::class.java,
            0
        )
    }
}
