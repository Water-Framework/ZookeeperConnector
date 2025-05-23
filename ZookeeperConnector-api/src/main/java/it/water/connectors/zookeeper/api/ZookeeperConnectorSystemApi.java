package it.water.connectors.zookeeper.api;

import it.water.core.api.interceptors.OnDeactivate;
import it.water.core.api.service.BaseSystemApi;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.zookeeper.CreateMode;

/**
 * @Generated by Water Generator
 * This interface defines the internally exposed methods allows interaction with them bypassing permission system.
 */
public interface ZookeeperConnectorSystemApi extends BaseSystemApi {

    void addZookeeperClient(ZookeeperClient zookeeperClient);

    void removeZookeeperClient(ZookeeperClient zookeeperClient);

    @OnDeactivate
    void deactivate();

    /**
     * Waits for the connection thread to complete succesfully
     *
     * @throws InterruptedException
     */
    void awaitRegistration() throws InterruptedException;

    /**
     * @return cluster path for leader election of cluster nodes and info
     */
    String getCurrentNodePath();

    /**
     * Return the cluster path in order to perform peer discovery
     *
     * @return
     */
    String getClusterPath();

    /**
     * @return
     */
    String getPeerPath(String nodeId);

    /**
     * @param serviceName
     * @return service path in order to orchestrate microservices leader election and info
     */
    String getCurrentServicePath(String serviceName, String instanceId);

    /**
     * Registers for leadership at the given path
     *
     * @param leadershipPath
     */
    void registerLeadershipComponent(String leadershipPath);

    /**
     * Unregisters for leadership at the given path
     *
     * @param leadershipPath
     */
    void unregisterLeadershipComponent(String leadershipPath);

    /**
     * This method adds a LeaderLatchListener
     *
     * @param listener       LeaderLatchListener instance
     * @param leadershipPath ZkNode path which bind listener on
     */
    void addListener(LeaderLatchListener listener, String leadershipPath);

    /**
     * This method returns true if current node is leader on given zkNode path, false otherwise
     *
     * @param mutexPath ZkNode path
     * @return True if current node is leader on given zkNode path, false otherwise
     */
    boolean isLeader(String mutexPath);

    /**
     * @param mode
     * @param path
     * @param data
     * @param createParentFolders
     * @throws Exception
     */
    @SuppressWarnings("java:S112")
    void create(CreateMode mode, String path, byte[] data, boolean createParentFolders) throws Exception;

    /**
     * @param path
     * @param data
     * @param createParentFolders
     * @throws Exception
     */
    @SuppressWarnings("java:S112")
    void create(String path, byte[] data, boolean createParentFolders) throws Exception;

    /**
     * @param path
     * @param data
     * @param createParentFolders
     * @throws Exception
     */
    @SuppressWarnings("java:S112")
    void createEphemeral(String path, byte[] data, boolean createParentFolders) throws Exception;

    /**
     * @param path
     * @param data
     * @param createParentFolders
     * @throws Exception
     */
    @SuppressWarnings("java:S112")
    void createPersistent(String path, byte[] data, boolean createParentFolders) throws Exception;

    /**
     * @param path
     * @throws Exception
     */
    @SuppressWarnings("java:S112")
    void delete(String path) throws Exception;

    /**
     * @param path
     * @return
     * @throws Exception
     */
    @SuppressWarnings("java:S112")
    byte[] read(String path) throws Exception;

    /**
     * @param path
     * @param lock if path should be locked while reading
     * @return
     * @throws Exception
     */
    @SuppressWarnings("java:S112")
    byte[] read(String path, boolean lock) throws Exception;

    /**
     * @param path path to update
     * @param data data to update
     * @throws Exception
     */
    @SuppressWarnings("java:S112")
    void update(String path, byte[] data) throws Exception;

    /**
     * @param path
     * @return
     * @throws Exception
     */
    @SuppressWarnings("java:S112")
    boolean pathExists(String path) throws Exception;

    /**
     * @return
     */
    CuratorFramework getZookeeperCuratorClient();
}