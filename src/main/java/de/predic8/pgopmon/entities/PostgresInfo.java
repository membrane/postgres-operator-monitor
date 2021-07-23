package de.predic8.pgopmon.entities;

public class PostgresInfo {
    private final String namespace;
    private final String name;
    private final Integer instances;

    public PostgresInfo(String namespace, String name, Integer instances) {
        this.namespace = namespace;
        this.name = name;
        this.instances = instances;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public Integer getInstances() {
        return instances;
    }
}
