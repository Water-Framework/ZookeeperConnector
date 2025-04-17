package it.water.connectors.zookeeper.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Author Aristide Cittadino
 * This class maps data from/to Zookeeper Cluster
 */
public class ZKData {
    private static Logger log = LoggerFactory.getLogger(ZKData.class.getName());
    private static ObjectMapper mapper = new ObjectMapper();

    private Map<String, byte[]> params;

    public ZKData() {
        this.params = new HashMap<>();
    }

    public void addParam(String key, byte[] o) {
        this.params.put(key, o);
    }

    public void removeParam(String key) {
        this.params.remove(key);
    }

    @JsonIgnore
    public byte[] getParam(String key) {
        return this.params.get(key);
    }

    public Map<String, byte[]> getParams() {
        return params;
    }

    @JsonIgnore
    public byte[] getBytes() {
        try {
            return this.toJson().getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return new byte[]{};
    }

    public String toJson() {
        try {
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static ZKData fromBytes(byte[] value) {
        try {
            return mapper.readValue(value, ZKData.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ZKData zkData = (ZKData) o;
        boolean oneVerse = this.params.keySet().stream().anyMatch(key -> !zkData.params.containsKey(key));
        boolean otherVerse = zkData.params.keySet().stream().anyMatch(key -> !this.params.containsKey(key));
        if(oneVerse != otherVerse) return false;
        return this.params.keySet().stream().allMatch(key -> (new String(zkData.params.get(key))).equals(new String(this.params.get(key))));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(params);
    }
}
