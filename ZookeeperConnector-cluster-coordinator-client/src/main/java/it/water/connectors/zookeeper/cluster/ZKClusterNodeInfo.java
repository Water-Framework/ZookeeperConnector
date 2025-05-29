package it.water.connectors.zookeeper.cluster;

import it.water.connectors.zookeeper.model.ZKData;
import it.water.core.api.service.cluster.ClusterNodeInfo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ZKClusterNodeInfo implements ClusterNodeInfo {

    @NonNull
    private final ZKData zkData;

    public ZKClusterNodeInfo(byte[] zkDataBytes) {
        if (zkDataBytes == null || zkDataBytes.length == 0)
            this.zkData = new ZKData();
        else
            this.zkData = ZKData.fromBytes(zkDataBytes);
    }

    @Override
    public boolean clusterModeEnabled() {
        return Boolean.parseBoolean(new String(zkData.getParam(CLUSTER_MODE_FIELD_NAME)));
    }

    @Override
    public String getNodeId() {
        return new String(zkData.getParam(NODE_ID_FIELD_NAME));
    }

    @Override
    public String getLayer() {
        return new String(zkData.getParam(LAYER_FIELD_NAME));
    }

    @Override
    public String getIp() {
        return new String(zkData.getParam(IP_FIELD_NAME));
    }

    @Override
    public String getHost() {
        return new String(zkData.getParam(HOST_FIELD_NAME));
    }

    @Override
    public boolean useIpInClusterRegistration() {
        return Boolean.parseBoolean(new String(zkData.getParam(IP_FIELD_NAME)));
    }
}
