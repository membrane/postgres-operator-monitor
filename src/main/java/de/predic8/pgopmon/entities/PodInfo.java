package de.predic8.pgopmon.entities;

public class PodInfo {
    private final String ip;
    private final String namespace;
    private final String name;
    private final String version;

    public PodInfo(String ip, String namespace, String name, String version) {
        this.ip = ip;
        this.namespace = namespace;
        this.name = name;
        this.version = version;
    }

    public String getIp() {
        return ip;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }
}
