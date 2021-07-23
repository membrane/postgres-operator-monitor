package de.predic8.pgopmon.entities;

public class DBIVKey extends DBIKey{
    private final String patroniVersion;

    public DBIVKey(String namespace, String database, String no, boolean master, String patroniVersion) {
        super(namespace, database, no, master);
        this.patroniVersion = patroniVersion;
    }

    public String getPatroniVersion() {
        return patroniVersion;
    }

    public String toPrometheusKey(String keyName) {
        return keyName + "{" +
                "namespace=\"" + prometheusEscape(this.getNamespace()) + "\"," +
                "name=\"" + prometheusEscape(this.getDatabase()) + "\"," +
                "no=\"" + prometheusEscape(this.getNo()) + "\"," +
                "master=\"" + prometheusEscape(this.isMaster() ? "1" : "0") + "\"" +
                "patroni_version=\"" + prometheusEscape(this.getPatroniVersion()) + "\"" +
                "}";

    }

}
