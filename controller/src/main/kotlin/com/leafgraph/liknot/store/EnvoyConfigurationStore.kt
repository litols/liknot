package com.leafgraph.liknot.store

import com.google.protobuf.util.JsonFormat
import io.envoyproxy.controlplane.cache.v3.SimpleCache
import io.envoyproxy.controlplane.cache.v3.Snapshot
import io.envoyproxy.envoy.config.cluster.v3.Cluster
import io.envoyproxy.envoy.config.endpoint.v3.ClusterLoadAssignment
import io.envoyproxy.envoy.config.route.v3.RouteConfiguration
import mu.KotlinLogging
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class EnvoyConfigurationStore(
    val cache: SimpleCache<String>

) {

    fun reflect(
        clusters: List<Cluster>,
        clusterLoadAssignments: List<ClusterLoadAssignment>,
        routes: List<RouteConfiguration>
    ) {
        val newSnapshot = Snapshot.create(
            clusters,
            clusterLoadAssignments,
            emptyList(),
            routes,
            emptyList(),
            UUID.randomUUID().toString()
        )

        log.info { "clusters=$clusters" }
        log.info { "cla=$clusterLoadAssignments" }
        log.info { "routes=$routes" }
        cache.setSnapshot("hash", newSnapshot)
    }

    companion object {
        private val log = KotlinLogging.logger {}
        val FORMATTER = JsonFormat.printer()
    }
}
