package it.water.connectors.zookeeper;

import it.water.connectors.zookeeper.api.ZookeeperConnectorSystemApi;
import it.water.connectors.zookeeper.model.ZKData;
import it.water.core.api.service.Service;
import it.water.core.api.service.cluster.ClusterCoordinatorClient;
import it.water.core.api.service.cluster.ClusterNodeOptions;
import it.water.core.interceptors.annotations.Inject;
import it.water.core.testing.utils.junit.WaterTestExtension;
import lombok.Setter;
import org.apache.curator.test.TestingServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Generated with Water Generator.
 * Test class for ZookeeperConnector Services.
 */
@ExtendWith(WaterTestExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ZKClusterCoordinatorTest implements Service {
    @Inject
    @Setter
    private ClusterCoordinatorClient clusterCoordinatorClient;

    @Inject
    @Setter
    private ZookeeperConnectorSystemApi zookeeperConnectorSystemApi;

    @Inject
    @Setter
    private ClusterNodeOptions clusterNodeOptions;

    private TestingServer zkServer;

    @BeforeAll
    public void startZookeeper() throws Exception {
        zkServer = new TestingServer(2181);
    }

    @AfterAll
    public void stopZookeeper() throws Exception {
        zkServer.close();
    }

    @Test
    @Order(1)
    void testingClusterCoordinatorBasics() throws InterruptedException {
        Assertions.assertNotNull(clusterCoordinatorClient);
        Assertions.assertNotNull(clusterNodeOptions);
        clusterCoordinatorClient.awaitConnection();
    }

    @Test
    @Order(2)
    void checkClusterCoordinatorInitializedCorrectly() throws Exception {
        Assertions.assertTrue(zookeeperConnectorSystemApi.pathExists(zookeeperConnectorSystemApi.getCurrentNodePath()));
        ZKData nodeData = ZKData.fromBytes(zookeeperConnectorSystemApi.read(zookeeperConnectorSystemApi.getCurrentNodePath()));
        String nodeId = new String(nodeData.getParam(ClusterNodeOptions.NODE_ID_FIELD_NAME));
        String nodeIp = new String(nodeData.getParam(ClusterNodeOptions.IP_FIELD_NAME));
        String nodeLayer = new String(nodeData.getParam(ClusterNodeOptions.LAYER_FIELD_NAME));
        String nodeHost = new String(nodeData.getParam(ClusterNodeOptions.HOST_FIELD_NAME));
        boolean nodeUseIp = Boolean.parseBoolean(new String(nodeData.getParam(ClusterNodeOptions.IP_REGISTRATION_FIELD_NAME)));
        boolean nodeClusterMode = Boolean.parseBoolean(new String(nodeData.getParam(ClusterNodeOptions.CLUSTER_MODE_FIELD_NAME)));
        Assertions.assertEquals(nodeId, clusterNodeOptions.getNodeId());
        Assertions.assertEquals(nodeIp, clusterNodeOptions.getIp());
        Assertions.assertEquals(nodeLayer, clusterNodeOptions.getLayer());
        Assertions.assertEquals(nodeHost, clusterNodeOptions.getHost());
        Assertions.assertEquals(nodeUseIp, clusterNodeOptions.useIpInClusterRegistration());
        Assertions.assertEquals(nodeClusterMode, clusterNodeOptions.clusterModeEnabled());
    }
}
