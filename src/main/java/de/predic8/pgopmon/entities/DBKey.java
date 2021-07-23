package de.predic8.pgopmon.entities;

public class DBKey {
    private final String namespace;
    private final String database;

    public DBKey(String namespace, String database) {
        this.namespace = namespace;
        this.database = database;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getDatabase() {
        return database;
    }

    protected static String prometheusEscape(String s) {
        return s.replaceAll("[^A-Za-z0-9]", "_");
    }

    public String toPrometheusKey(String keyName) {
        return keyName + "{" +
                "namespace=\"" + prometheusEscape(this.getNamespace()) + "\"," +
                "name=\"" + prometheusEscape(this.getDatabase()) + "\"" +
                "}";
    }
}
