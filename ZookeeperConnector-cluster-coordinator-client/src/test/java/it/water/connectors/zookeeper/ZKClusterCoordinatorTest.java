package it.water.connectors.zookeeper;

import it.water.core.api.service.Service;
import it.water.core.api.service.cluster.ClusterCoordinatorClient;
import it.water.core.interceptors.annotations.Inject;
import it.water.core.testing.utils.junit.WaterTestExtension;
import lombok.Setter;
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
    ClusterCoordinatorClient clusterCoordinatorClient;

    @Test
    void testZKClusterCoordinatorClient() {
        Assertions.assertNotNull(clusterCoordinatorClient);
        Assertions.assertFalse(clusterCoordinatorClient.registerToCluster());
        Assertions.assertFalse(clusterCoordinatorClient.unregisterToCluster());
        Assertions.assertFalse(clusterCoordinatorClient.checkClusterLeadershipFor("example"));
        Assertions.assertFalse(clusterCoordinatorClient.peerStillExists(null));
        Assertions.assertEquals(0, clusterCoordinatorClient.getPeerNodes(null).size());
    }
}
