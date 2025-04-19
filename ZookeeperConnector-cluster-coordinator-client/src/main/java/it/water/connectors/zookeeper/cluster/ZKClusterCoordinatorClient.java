package it.water.connectors.zookeeper.cluster;

import it.water.connectors.zookeeper.api.ZookeeperConnectorSystemApi;
import it.water.core.api.service.cluster.ClusterCoordinatorClient;
import it.water.core.api.service.cluster.ClusterNodeInfo;
import it.water.core.api.service.cluster.ClusterNodeOptions;
import it.water.core.api.service.cluster.ClusterObserver;
import it.water.core.interceptors.annotations.FrameworkComponent;
import it.water.core.interceptors.annotations.Inject;
import lombok.Setter;

import java.util.Collection;
import java.util.List;

@FrameworkComponent
public class ZKClusterCoordinatorClient implements ClusterCoordinatorClient {
    @Inject
    @Setter
    private ClusterNodeOptions clusterNodeOptions;

    @Inject
    @Setter
    private ZookeeperConnectorSystemApi zookeeperConnectorSystemApi;

    @Override
    public void subscribeToClusterEvents(ClusterObserver clusterObserver) {

    }

    @Override
    public void unsubscribeToClusterEvents(ClusterObserver clusterObserver) {

    }

    @Override
    public boolean registerToCluster() {
        return false;
    }

    @Override
    public boolean unregisterToCluster() {
        return false;
    }

    @Override
    public boolean checkClusterLeadershipFor(String s) {
        return false;
    }

    @Override
    public boolean peerStillExists(ClusterNodeOptions clusterNodeOptions) {
        return false;
    }

    @Override
    public Collection<ClusterNodeInfo> getPeerNodes(ClusterNodeOptions clusterNodeOptions) {
        return List.of();
    }
}
