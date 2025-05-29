package it.water.connectors.zookeeper.cluster;

import it.water.connectors.zookeeper.api.ZookeeperConnectorSystemApi;
import it.water.connectors.zookeeper.model.ZKData;
import it.water.core.api.service.Service;
import it.water.core.api.service.cluster.*;
import it.water.core.interceptors.annotations.Inject;
import it.water.core.testing.utils.junit.WaterTestExtension;
import lombok.Setter;
import org.apache.curator.test.TestingServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

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
    private ZKClusterCoordinatorClient peer1 = new ZKClusterCoordinatorClient();
    private ZKClusterCoordinatorClient peer2 = new ZKClusterCoordinatorClient();
    private ZKClusterCoordinatorClient peer3 = new ZKClusterCoordinatorClient();

    @BeforeAll
    public void startZookeeper() throws Exception {
        zkServer = new TestingServer(2181);
        peer1 = new ZKClusterCoordinatorClient();
        peer2 = new ZKClusterCoordinatorClient();
        peer3 = new ZKClusterCoordinatorClient();
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

    @Test
    @Order(3)
    void testClusterCoordination() {
        //registering to cluster
        this.zookeeperConnectorSystemApi.addZookeeperClient(peer1);
        this.zookeeperConnectorSystemApi.addZookeeperClient(peer2);
        this.zookeeperConnectorSystemApi.addZookeeperClient(peer3);
        //forcing node options in order to override the injected one
        peer1.setClusterNodeOptions(createClusterNodeOptions("peer1", "layer", "127.0.0.1", "localhost"));
        peer2.setClusterNodeOptions(createClusterNodeOptions("peer2", "layer", "127.0.0.1", "localhost"));
        peer3.setClusterNodeOptions(createClusterNodeOptions("peer3", "layer", "127.0.0.2", "remoteHost"));
        peer1.registerToCluster();
        ClusterObserver clusterObserver = createFakeClusterObserver();
        peer1.subscribeToClusterEvents(clusterObserver);
        peer2.registerToCluster();
        peer3.registerToCluster();
        //3 peers plus the current node so all peers are 4
        await().atMost(30, SECONDS).until(() -> clusterCoordinatorClient.getPeerNodes().size() == 4);
        peer1.unsubscribeToClusterEvents(clusterObserver);
    }

    @Order(4)
    void testPeersInteractions() {
        ClusterNodeInfo peer1NodeOptions = clusterCoordinatorClient.getPeerNodes().stream().filter(peers -> peers.getNodeId().equals("peer1")).findFirst().get();
        Assertions.assertTrue(clusterCoordinatorClient.peerStillExists(peer1NodeOptions));
        peer1.unregisterToCluster();
        peer1.onDeactivate();
        await().atMost(30, SECONDS).until(() -> clusterCoordinatorClient.getPeerNodes().size() == 3);
        Assertions.assertFalse(clusterCoordinatorClient.peerStillExists(peer1NodeOptions));
        peer1.registerToCluster();
        await().atMost(30, SECONDS).until(() -> clusterCoordinatorClient.getPeerNodes().size() == 4);
        Assertions.assertTrue(clusterCoordinatorClient.peerStillExists(peer1NodeOptions));
        //we just write as an example but all peers in this test refer to the same client
        String leadershipPath = "leadershipTest";
        peer1.registerForLeadership(leadershipPath);
        peer2.registerForLeadership(leadershipPath);
        peer3.registerForLeadership(leadershipPath);
        clusterCoordinatorClient.registerForLeadership(leadershipPath);
        Assertions.assertTrue(peer1.checkClusterLeadershipFor(leadershipPath) ^ peer2.checkClusterLeadershipFor(leadershipPath) ^ peer3.checkClusterLeadershipFor(leadershipPath) ^ this.clusterCoordinatorClient.checkClusterLeadershipFor(leadershipPath));
        clusterCoordinatorClient.unregisterForLeadership(leadershipPath);
        Assertions.assertTrue(peer1.checkClusterLeadershipFor(leadershipPath) ^ peer2.checkClusterLeadershipFor(leadershipPath) ^ peer3.checkClusterLeadershipFor(leadershipPath));
        peer1.unregisterForLeadership(leadershipPath);
        Assertions.assertTrue(peer2.checkClusterLeadershipFor(leadershipPath) ^ peer3.checkClusterLeadershipFor(leadershipPath));
        peer2.unregisterForLeadership(leadershipPath);
        Assertions.assertTrue(peer3.checkClusterLeadershipFor(leadershipPath));
    }

    private ClusterNodeOptions createClusterNodeOptions(String nodeId, String layer, String ip, String host) {
        return new ClusterNodeOptions() {
            @Override
            public boolean clusterModeEnabled() {
                return true;
            }

            @Override
            public String getNodeId() {
                return nodeId;
            }

            @Override
            public String getLayer() {
                return layer;
            }

            @Override
            public String getIp() {
                return ip;
            }

            @Override
            public String getHost() {
                return host;
            }

            @Override
            public boolean useIpInClusterRegistration() {
                return false;
            }
        };
    }

    private ClusterObserver createFakeClusterObserver() {
        return new ClusterObserver() {
            @Override
            public void onClusterEvent(ClusterEvent clusterEvent, ClusterNodeInfo clusterNodeInfo, byte[] bytes) {
                // do nothing
            }
        };
    }
}
