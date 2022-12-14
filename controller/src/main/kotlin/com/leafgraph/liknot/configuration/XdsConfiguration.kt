package com.leafgraph.liknot.configuration

import com.linecorp.armeria.common.grpc.GrpcSerializationFormats
import com.linecorp.armeria.server.docs.DocService
import com.linecorp.armeria.server.grpc.GrpcService
import com.linecorp.armeria.server.logging.LoggingService
import com.linecorp.armeria.spring.ArmeriaServerConfigurator
import io.envoyproxy.controlplane.cache.v3.SimpleCache
import io.envoyproxy.controlplane.server.V3DiscoveryServer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class XdsConfiguration {

    @Bean
    fun xdsServerConfigurator(
        v3DiscoveryServer: V3DiscoveryServer
    ): ArmeriaServerConfigurator {
        return ArmeriaServerConfigurator { builder ->
            builder.serviceUnder("/docs", DocService())
            builder.maxRequestLength(Int.MAX_VALUE.toLong()) // なんでも受け入れていただく
            builder.requestTimeoutMillis(0)
            builder.service(
                GrpcService.builder()
                    .addServices(
                        v3DiscoveryServer.clusterDiscoveryServiceImpl,
                        v3DiscoveryServer.listenerDiscoveryServiceImpl,
                        v3DiscoveryServer.routeDiscoveryServiceImpl,
                        v3DiscoveryServer.endpointDiscoveryServiceImpl,
                        v3DiscoveryServer.aggregatedDiscoveryServiceImpl
                    )
                    .enableUnframedRequests(true)
                    .supportedSerializationFormats(
                        GrpcSerializationFormats.JSON,
                        GrpcSerializationFormats.PROTO,
                        GrpcSerializationFormats.PROTO_WEB,
                        GrpcSerializationFormats.PROTO_WEB_TEXT
                    )
                    .build()
            )
            builder.decorator(LoggingService.newDecorator())
        }
    }

    @Bean
    fun discoveryServer(simpleCache: SimpleCache<String>): V3DiscoveryServer {
        return V3DiscoveryServer(simpleCache)
    }

    @Bean
    fun simpleCache(): SimpleCache<String> {
        return SimpleCache { _ -> "hash" }
    }
}
