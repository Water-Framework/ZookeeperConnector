package it.water.connectors.zookeeper.api;

import it.water.core.api.service.cluster.ClusterNodeOptions;

/**
 * @Author Aristide Cittadino
 * Interfaces which support the custom client registration that need to be sure Zookeeper System Api has correctly connected to zk cluster
 */
public interface ZookeeperClient {
    void onConnectionOpened(ZookeeperConnectorSystemApi zookeeperConnectorSystemApi, ClusterNodeOptions clusterNodeOptions);
    void onConnectionClosed();
}
