# ZookeeperConnector Module — Cluster Coordination & Distributed Configuration

## Purpose
Provides Water Framework integration with Apache ZooKeeper for distributed coordination scenarios: leader election, peer discovery, cluster membership tracking, and distributed configuration. Uses Apache Curator 5.x as the ZooKeeper client abstraction. Suitable for multi-node Water deployments requiring consensus, synchronization, or cluster-aware behavior.

## Sub-modules

| Sub-module | Runtime | Key Classes |
|---|---|---|
| `ZookeeperConnector-api` | All | `ZookeeperConnectorApi`, `ZookeeperConnectorSystemApi`, `ZookeeperClient`, `ClusterCoordinatorClient`, `ClusterEventObserver`, `ZKData` |
| `ZookeeperConnector-model` | All | `ZKData`, `ZKConstants`, node path conventions |
| `ZookeeperConnector-service` | Water/OSGi | Apache Curator-based `ZookeeperClientImpl`, `ZookeeperConnectorSystemServiceImpl` |
| `ZookeeperConnector-cluster-coordinator-client` | All | `ClusterCoordinatorClientImpl` — peer discovery, leader election, cluster events |

## ZookeeperClient Interface

```java
public interface ZookeeperClient {

    // Node creation
    void create(ZKMode mode, String path, byte[] data, boolean createParents);
    // Modes: PERSISTENT, EPHEMERAL, PERSISTENT_SEQUENTIAL, EPHEMERAL_SEQUENTIAL

    // Read
    byte[] read(String path, boolean lock);       // lock=true acquires InterProcessMutex

    // Update
    void update(String path, byte[] data);

    // Delete
    void delete(String path);

    // Existence
    boolean pathExists(String path);

    // Leadership
    void registerLeadershipComponent(String electionPath);
    boolean isLeader(String electionPath);
    void addLeadershipListener(LeaderLatchListener listener, String electionPath);
    void removeLeadershipListener(LeaderLatchListener listener, String electionPath);
}
```

## ZKData — JSON Payload Serialization

All ZooKeeper node data is stored as structured JSON via `ZKData`:

```java
public class ZKData {
    private Map<String, String> data = new LinkedHashMap<>();

    // Builder pattern
    public ZKData put(String key, String value) { data.put(key, value); return this; }
    public String get(String key) { return data.get(key); }

    // Serialization
    public byte[] getBytes();                           // JSON → bytes
    public static ZKData fromBytes(byte[] bytes);       // bytes → JSON → ZKData
}
```

Usage:
```java
ZKData nodeData = new ZKData()
    .put("host", "192.168.1.10")
    .put("port", "8080")
    .put("status", "UP");

zookeeperClient.create(ZKMode.EPHEMERAL, "/water/nodes/node-1", nodeData.getBytes(), true);
```

## ZKConstants — Path Conventions

```java
public class ZKConstants {
    public static final String ROOT          = "/water";
    public static final String NODES         = ROOT + "/nodes";
    public static final String LEADER        = ROOT + "/leader";
    public static final String CONFIG        = ROOT + "/config";
    public static final String SERVICES      = ROOT + "/services";
}
```

## ClusterCoordinatorClient

High-level API for common cluster patterns:

```java
public interface ClusterCoordinatorClient {

    // Register this JVM as a cluster peer (EPHEMERAL node — auto-removed on disconnect)
    void registerToCluster(String nodeId, ZKData nodeInfo);

    // Deregister
    void unregisterFromCluster(String nodeId);

    // Peer discovery
    List<String> getPeerNodeIds();
    ZKData getPeerInfo(String nodeId);

    // Cluster events
    void subscribeToClusterEvents(ClusterEventObserver observer);
    void unsubscribeFromClusterEvents(ClusterEventObserver observer);

    // Distributed configuration
    void setConfig(String key, String value);
    String getConfig(String key);
    void watchConfig(String key, ConfigChangeCallback callback);
}
```

## ClusterEventObserver

```java
public interface ClusterEventObserver {
    void onPeerConnected(String nodeId, ZKData nodeInfo);
    void onPeerDisconnected(String nodeId);
    void onPeerInfoChanged(String nodeId, ZKData newInfo);
}
```

## Leadership Election Pattern

```java
@FrameworkComponent
public class ScheduledJobCoordinator implements Service {
    @Inject @Setter private ZookeeperClient zookeeperClient;

    private static final String ELECTION_PATH = ZKConstants.LEADER + "/scheduler";

    @OnActivate
    void init() {
        zookeeperClient.registerLeadershipComponent(ELECTION_PATH);
    }

    // Called periodically — only leader executes the job
    public void runJob() {
        if (zookeeperClient.isLeader(ELECTION_PATH)) {
            // This node is the current leader — execute
            doWork();
        }
        // Otherwise skip — another node is leader
    }
}
```

## Peer Discovery Pattern

```java
@FrameworkComponent
public class GatewayLoadBalancer implements Service {
    @Inject @Setter private ClusterCoordinatorClient clusterClient;

    @OnActivate
    void init() {
        // Register self
        ZKData myInfo = new ZKData()
            .put("host", localHost)
            .put("port", String.valueOf(localPort))
            .put("weight", "1");
        clusterClient.registerToCluster("gateway-" + instanceId, myInfo);

        // Watch for peer changes
        clusterClient.subscribeToClusterEvents(new ClusterEventObserver() {
            public void onPeerConnected(String id, ZKData info) { addPeer(id, info); }
            public void onPeerDisconnected(String id) { removePeer(id); }
            public void onPeerInfoChanged(String id, ZKData info) { updatePeer(id, info); }
        });
    }
}
```

## ZookeeperConnectorApi vs SystemApi

- **`ZookeeperConnectorApi`** — Public API with permission checks (for managing ZK connection config if stored as entity)
- **`ZookeeperConnectorSystemApi`** — System API, bypasses permissions — used for all coordination operations

## Configuration

```properties
water.connectors.zookeeper.connection.string=localhost:2181
water.connectors.zookeeper.session.timeout.ms=30000
water.connectors.zookeeper.connection.timeout.ms=10000
water.connectors.zookeeper.retry.attempts=3
water.connectors.zookeeper.retry.sleep.ms=1000
water.connectors.zookeeper.namespace=water     # all paths prefixed with /water
```

## Dependencies
- `it.water.core:Core-api` — `@FrameworkComponent`, lifecycle, `SystemApi`
- `org.apache.curator:curator-framework` — ZooKeeper client abstraction
- `org.apache.curator:curator-recipes` — `LeaderLatch`, `PathChildrenCache`, `TreeCache`
- `org.apache.curator:curator-test` — `TestingServer` for in-memory ZK testing

## Testing

Uses Apache Curator's `TestingServer` for in-memory ZooKeeper (no external server needed):

```java
@ExtendWith(WaterTestExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ZookeeperConnectorTest implements Service {
    @Inject @Setter private ZookeeperConnectorSystemApi zkApi;

    private TestingServer zkServer;

    @BeforeAll
    void startZk() throws Exception {
        zkServer = new TestingServer();
        zkServer.start();
        // configure ZK connection string: zkServer.getConnectString()
    }

    @AfterAll
    void stopZk() throws Exception {
        if (zkServer != null) zkServer.close();
    }
}
```

## Code Generation Rules
- EPHEMERAL nodes are automatically removed when the ZooKeeper session expires (process crash, network partition) — always use EPHEMERAL for cluster membership
- `ZKData` is the only allowed payload format — never store raw strings or binary data directly
- Leadership: one `LeaderLatch` per election path; register once in `@OnActivate`, release in `@OnDeactivate`
- No REST controllers in this module (coordination is programmatic only) — no Karate tests needed
- Distributed lock: use `read(path, lock=true)` for critical sections; prefer short lock durations
- Namespace all paths under `ZKConstants.ROOT` ("/water") to avoid conflicts with other ZooKeeper users in the same cluster
