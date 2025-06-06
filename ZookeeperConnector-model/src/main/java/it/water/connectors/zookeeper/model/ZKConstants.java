package it.water.connectors.zookeeper.model;

/**
 * @Author Aristide Cittadino
 */
public final class ZKConstants {
    public static final String ZOOKEEPER_CONNECTION_URL = "it.water.connectors.zookeeper.url";
    public static final String WATER_ZOOKEEPER_PATH = "it.water.connectors.zookeeper.base.path";
    @SuppressWarnings("java:S1075")
    public static final String WATER_ZOOKEEPER_DEFAULT_BASE_PATH = "/water-framework/layers";

    private ZKConstants() {}
}
