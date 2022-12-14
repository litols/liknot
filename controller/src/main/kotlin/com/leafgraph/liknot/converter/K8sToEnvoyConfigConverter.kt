package com.leafgraph.liknot.converter

import io.envoyproxy.envoy.config.cluster.v3.Cluster
import io.envoyproxy.envoy.config.core.v3.Address
import io.envoyproxy.envoy.config.core.v3.AggregatedConfigSource
import io.envoyproxy.envoy.config.core.v3.ApiVersion
import io.envoyproxy.envoy.config.core.v3.ConfigSource
import io.envoyproxy.envoy.config.core.v3.SocketAddress
import io.envoyproxy.envoy.config.endpoint.v3.ClusterLoadAssignment
import io.envoyproxy.envoy.config.endpoint.v3.Endpoint
import io.envoyproxy.envoy.config.endpoint.v3.LbEndpoint
import io.envoyproxy.envoy.config.endpoint.v3.LocalityLbEndpoints
import io.envoyproxy.envoy.config.route.v3.Route
import io.envoyproxy.envoy.config.route.v3.RouteAction
import io.envoyproxy.envoy.config.route.v3.RouteConfiguration
import io.envoyproxy.envoy.config.route.v3.RouteMatch
import io.envoyproxy.envoy.config.route.v3.VirtualHost
import io.kubernetes.client.openapi.models.V1EndpointSlice
import io.kubernetes.client.openapi.models.V1IngressRule
import io.kubernetes.client.openapi.models.V1IngressSpec
import io.kubernetes.client.openapi.models.V1Service

object K8sToEnvoyConfigConverter {
    private val configSource = ConfigSource.newBuilder()
        .setAds(AggregatedConfigSource.getDefaultInstance())
        .setResourceApiVersion(ApiVersion.V3)
        .build()

    fun convertToCluster(service: V1Service): Cluster {
        return service.metadata?.name!!.let { name ->
            Cluster.newBuilder()
                .setName(getClusterNameFromK8sService(name))
                .setLbPolicy(Cluster.LbPolicy.ROUND_ROBIN)
                .setType(Cluster.DiscoveryType.EDS)
                .setEdsClusterConfig(
                    Cluster.EdsClusterConfig.newBuilder()
                        .setEdsConfig(configSource)
                        .build()
                )
                .build()
        }
    }

    fun convertToClusterLoadAssignment(endpointSlice: V1EndpointSlice): ClusterLoadAssignment {
        val localityLbEndpointBuilder = LocalityLbEndpoints.newBuilder()
        endpointSlice.endpoints?.asSequence()
            ?.filter { it.conditions?.ready == true && it.conditions?.serving == true }
            ?.forEach { endpoint ->
                endpoint.addresses.forEach { address ->
                    localityLbEndpointBuilder.addLbEndpoints(
                        LbEndpoint.newBuilder()
                            .setEndpoint(
                                Endpoint.newBuilder().setAddress(
                                    Address.newBuilder().setSocketAddress(
                                        SocketAddress.newBuilder()
                                            .setAddress(address)
                                            .setPortValue(endpointSlice.ports?.get(0)?.port!!)
                                            .setProtocol(SocketAddress.Protocol.TCP)
                                    )
                                )
                            )
                    )
                }
            }

        return ClusterLoadAssignment.newBuilder().apply {
            clusterName = getClusterNameFromK8sService(
                endpointSlice.metadata?.labels?.get("kubernetes.io/service-name")!!
            )
            addEndpoints(localityLbEndpointBuilder.build())
        }.build()
    }

    fun convertToRouteConfiguration(ingressSpec: V1IngressSpec): RouteConfiguration {
        val aggregatedIngressRule = ingressSpec.rules?.groupBy { it.host!! } ?: emptyMap()

        return RouteConfiguration.newBuilder()
            .setName("ingress_routes")
            .addAllVirtualHosts(
                aggregatedIngressRule.asSequence().map { (host, ingressRules) ->
                    VirtualHost.newBuilder()
                        .addDomains(host)
                        .addAllRoutes(
                            ingressRules.map { ingressRule ->
                                convertToRoute(ingressRule)
                            }.flatten()
                        )
                        .build()
                }.toList()
            ).build()
    }

    fun convertToRoute(ingressRule: V1IngressRule): List<Route> {
        return ingressRule.http?.paths?.map { ingressPath ->
            Route.newBuilder().apply {
                setMatch(
                    RouteMatch.newBuilder().apply {
                        when (ingressPath.pathType) {
                            "Exact" -> path = ingressPath.path
                            "Prefix" -> prefix = ingressPath.path
                        }
                    }
                )
                setRoute(
                    RouteAction.newBuilder()
                        .setCluster(getClusterNameFromK8sService(ingressPath.backend?.service?.name!!))
                        .setHostRewriteLiteral(
                            "${ingressRule.host}:${ingressPath.backend.service?.port?.number ?: "80"}"
                        )
                )
            }.build()
        } ?: emptyList()
    }

    fun getClusterNameFromK8sService(service: String): String {
        return "service_$service"
    }
}
