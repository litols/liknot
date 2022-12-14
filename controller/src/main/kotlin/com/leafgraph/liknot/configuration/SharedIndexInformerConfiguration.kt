package com.leafgraph.liknot.configuration

import io.kubernetes.client.informer.SharedIndexInformer
import io.kubernetes.client.informer.SharedInformerFactory
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.models.V1EndpointSlice
import io.kubernetes.client.openapi.models.V1EndpointSliceList
import io.kubernetes.client.openapi.models.V1Ingress
import io.kubernetes.client.openapi.models.V1IngressList
import io.kubernetes.client.openapi.models.V1Node
import io.kubernetes.client.openapi.models.V1NodeList
import io.kubernetes.client.openapi.models.V1Pod
import io.kubernetes.client.openapi.models.V1PodList
import io.kubernetes.client.openapi.models.V1Service
import io.kubernetes.client.openapi.models.V1ServiceList
import io.kubernetes.client.util.generic.GenericKubernetesApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class SharedIndexInformerConfiguration {

    @Bean
    fun endpointSliceInformer(
        apiClient: ApiClient,
        sharedInformerFactory: SharedInformerFactory
    ): SharedIndexInformer<V1EndpointSlice> {
        val genericApi = GenericKubernetesApi(
            V1EndpointSlice::class.java,
            V1EndpointSliceList::class.java,
            "discovery.k8s.io",
            "v1",
            "endpointslices",
            apiClient
        )
        return sharedInformerFactory.sharedIndexInformerFor(
            genericApi,
            V1EndpointSlice::class.java,
            0
        )
    }

    @Bean
    fun serviceInformer(
        apiClient: ApiClient,
        sharedInformerFactory: SharedInformerFactory
    ): SharedIndexInformer<V1Service> {
        val genericApi = GenericKubernetesApi(
            V1Service::class.java,
            V1ServiceList::class.java,
            "",
            "v1",
            "services",
            apiClient
        )
        return sharedInformerFactory.sharedIndexInformerFor(
            genericApi,
            V1Service::class.java,
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

    @Bean
    fun ingressInformer(
        apiClient: ApiClient,
        sharedInformerFactory: SharedInformerFactory
    ): SharedIndexInformer<V1Ingress> {
        val genericApi = GenericKubernetesApi(
            V1Ingress::class.java,
            V1IngressList::class.java,
            "networking.k8s.io",
            "v1",
            "ingresses",
            apiClient
        )
        return sharedInformerFactory.sharedIndexInformerFor(
            genericApi,
            V1Ingress::class.java,
            0
        )
    }
}
