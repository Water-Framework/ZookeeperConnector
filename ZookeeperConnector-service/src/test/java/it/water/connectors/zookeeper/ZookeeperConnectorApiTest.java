package it.water.connectors.zookeeper;

import it.water.connectors.zookeeper.api.ZookeeperConnectorApi;
import it.water.connectors.zookeeper.api.ZookeeperConnectorSystemApi;
import it.water.connectors.zookeeper.model.ZKConstants;
import it.water.connectors.zookeeper.model.ZKData;
import it.water.core.api.bundle.ApplicationProperties;
import it.water.core.api.bundle.Runtime;
import it.water.core.api.registry.ComponentRegistry;
import it.water.core.api.service.Service;
import it.water.core.interceptors.annotations.Inject;
import it.water.core.testing.utils.junit.WaterTestExtension;
import lombok.Setter;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.CreateMode;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Generated with Water Generator.
 * Test class for ZookeeperConnector Services.
 */
@ExtendWith(WaterTestExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ZookeeperConnectorApiTest implements Service {

    @Inject
    @Setter
    private ComponentRegistry componentRegistry;

    @Inject
    @Setter
    private ZookeeperConnectorApi zookeeperconnectorApi;

    @Inject
    @Setter
    private ZookeeperConnectorSystemApi zookeeperConnectorSystemApi;

    @Inject
    @Setter
    private ApplicationProperties applicationProperties;

    @Inject
    @Setter
    private Runtime runtime;

    private TestingServer zkServer;

    @BeforeAll
    public void startZookeeper() throws Exception {
        zkServer = new TestingServer(2181);
    }

    @AfterAll
    public void stopZookeeper() throws Exception {
        zkServer.close();
    }

    /**
     * Testing basic injection of basic component for zookeeperconnector entity.
     */
    @Test
    @Order(1)
    void zookeeperConnectorStartsCorrectly() {
        this.zookeeperconnectorApi = this.componentRegistry.findComponent(ZookeeperConnectorApi.class, null);
        Assertions.assertNotNull(this.zookeeperconnectorApi);
        Assertions.assertNotNull(this.componentRegistry.findComponent(ZookeeperConnectorSystemApi.class, null));
    }

    @Test
    @Order(2)
    void awaitZookeeperConnection() throws InterruptedException {
        //waits for zookeeper client to be connected in order to go ahead with other tests
        this.zookeeperConnectorSystemApi.awaitRegistration();
        Assertions.assertNotNull(zookeeperConnectorSystemApi.getZookeeperCuratorClient());
        Assertions.assertEquals(CuratorFrameworkState.STARTED, zookeeperConnectorSystemApi.getZookeeperCuratorClient().getState());
    }

    @Test
    @Order(3)
    @SuppressWarnings("java:S2925")
    void testLeaderLatch() {
        try {
            String leadershipPath = "/testPath";
            TestLeaderLatchListener testLeaderLatchListener = new TestLeaderLatchListener();
            Assertions.assertFalse(testLeaderLatchListener.isPathLeader());
            zookeeperConnectorSystemApi.registerLeadershipComponent(leadershipPath);
            zookeeperConnectorSystemApi.addListener(testLeaderLatchListener, leadershipPath);
            Thread.sleep(1000);
            //let's test listeners
            Assertions.assertTrue(testLeaderLatchListener.isPathLeader());
            //testing directly with connector API
            Assertions.assertTrue(zookeeperconnectorApi.isLeader(leadershipPath));
            zookeeperConnectorSystemApi.unregisterLeadershipComponent(leadershipPath);
            Assertions.assertFalse(zookeeperConnectorSystemApi.isLeader(leadershipPath));
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    @Order(4)
    void testsForCoverage() {
        String zkBasePath = applicationProperties.getPropertyOrDefault(ZKConstants.WATER_ZOOKEEPER_PATH, ZKConstants.WATER_ZOOKEEPER_DEFAULT_BASE_PATH);
        Assertions.assertEquals(zkBasePath + "/services/aservice/instanceA", zookeeperConnectorSystemApi.getCurrentServicePath("aservice", "instanceA"));
        Assertions.assertNotNull(zkBasePath);
        Assertions.assertNotNull(ZKConstants.ZOOKEEPER_CONNECTION_URL);
    }

    @Test
    @Order(5)
    void testNodeCreation() {
        byte[] data = "{\"myData\":\"my Data\"}".getBytes();
        String dataString = new String(data);
        byte[] update = "{\"myData\":\"my update\"}".getBytes();
        String updateString = new String(update);
        String testPath = "/test/path";
        try {
            Assertions.assertFalse(zookeeperConnectorSystemApi.pathExists(testPath));
            zookeeperConnectorSystemApi.create("/random", data, true);
            zookeeperConnectorSystemApi.create(CreateMode.PERSISTENT, testPath, data, true);
            Assertions.assertTrue(zookeeperConnectorSystemApi.pathExists(testPath));
            Assertions.assertEquals(dataString, new String(zookeeperConnectorSystemApi.read(testPath)));
            zookeeperConnectorSystemApi.createEphemeral(testPath + "/ephemeral", data, true);
            Assertions.assertEquals(dataString, new String(zookeeperConnectorSystemApi.read(testPath + "/ephemeral")));
            zookeeperConnectorSystemApi.createPersistent(testPath + "/persistent", data, true);
            Assertions.assertEquals(dataString, new String(zookeeperConnectorSystemApi.read(testPath + "/persistent", true)));
            zookeeperConnectorSystemApi.update(testPath + "/persistent", update);
            Assertions.assertEquals(updateString, new String(zookeeperConnectorSystemApi.read(testPath + "/persistent")));
            zookeeperConnectorSystemApi.delete(testPath + "/persistent");
            Assertions.assertFalse(zookeeperConnectorSystemApi.pathExists(testPath + "/persistent"));
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }
    }

    @Test
    @Order(6)
    @SuppressWarnings("java:S5845")
    void testZKData() {
        String data = "new Data";
        ZKData zkData = new ZKData();
        ZKData zkData2 = new ZKData();
        ZKData zkData3 = new ZKData();
        ZKData zkData4 = new ZKData();
        zkData.addParam("param1", "value1".getBytes());
        zkData.addParam("param2", "value2".getBytes());
        zkData.addParam("param3", "value3".getBytes());
        zkData2.addParam("param1", "value1".getBytes());
        zkData2.addParam("param2", "value2".getBytes());
        zkData3.addParam("param1", "value1".getBytes());
        zkData3.addParam("param2", "value2".getBytes());
        zkData3.addParam("param3", "value3".getBytes());
        zkData3.addParam("param4", "value4".getBytes());
        zkData4.addParam("param1", "value1".getBytes());
        zkData4.addParam("param2", "value2".getBytes());
        zkData4.addParam("param3", "valueDIFFERENT".getBytes());
        Assertions.assertNotEquals(data,zkData);
        Assertions.assertNotEquals(zkData, zkData2);
        Assertions.assertNotEquals(zkData, zkData3);
        Assertions.assertNotEquals(zkData, zkData4);
        Assertions.assertNotEquals(0,zkData.hashCode());
        Assertions.assertTrue(zkData.getParams().containsKey("param1"));
        Assertions.assertTrue(zkData.getParams().containsKey("param2"));
        Assertions.assertTrue(zkData.getParams().containsKey("param3"));
        Assertions.assertEquals("value1",new String(zkData.getParam("param1")));
        Assertions.assertEquals("value2",new String(zkData.getParam("param2")));
        Assertions.assertEquals("value3",new String(zkData.getParam("param3")));
        zkData.removeParam("param3");
        Assertions.assertFalse(zkData.getParams().containsKey("param3"));
        try {
            zookeeperConnectorSystemApi.create("/testZkData", zkData.getBytes(), true);
            Assertions.assertEquals(zkData, ZKData.fromBytes(zookeeperConnectorSystemApi.read("/testZkData")));
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }
    }

    @Test
    @Order(7)
    void testDeactivation(){
        Assertions.assertDoesNotThrow(() -> componentRegistry.unregisterComponent(ZookeeperConnectorSystemApi.class, zookeeperConnectorSystemApi));
    }
}
