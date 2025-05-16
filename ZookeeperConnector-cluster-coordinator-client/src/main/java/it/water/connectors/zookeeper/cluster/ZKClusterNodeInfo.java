package it.water.connectors.zookeeper.cluster;

import it.water.connectors.zookeeper.model.ZKData;
import it.water.core.api.service.cluster.ClusterNodeInfo;
import it.water.core.api.service.cluster.ClusterNodeOptions;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ZKClusterNodeInfo implements ClusterNodeInfo {

    @NonNull
    private final ZKData zkData;

    public ZKClusterNodeInfo(byte[] zkData) {
        if (zkData == null || zkData.length == 0)
            throw new IllegalArgumentException("zkData is null or empty");
        this.zkData = ZKData.fromBytes(zkData);
    }

    @Override
    public boolean clusterModeEnabled() {
        return Boolean.parseBoolean(new String(zkData.getParam(ClusterNodeOptions.PROP_CLUSTER_MODE_ENABLED)));
    }

    @Override
    public String getNodeId() {
        return new String(zkData.getParam(ClusterNodeOptions.PROP_NODE_ID));
    }

    @Override
    public String getLayer() {
        return new String(zkData.getParam(ClusterNodeOptions.PROP_LAYER_ID));
    }

    @Override
    public String getIp() {
        return new String(zkData.getParam(ClusterNodeOptions.PROP_IP));
    }

    @Override
    public String getHost() {
        return new String(zkData.getParam(ClusterNodeOptions.PROP_HOST));
    }

    @Override
    public boolean useIpInClusterRegistration() {
        return Boolean.parseBoolean(new String(zkData.getParam(ClusterNodeOptions.PROP_USE_IP)));
    }
}
