package de.predic8.pgopmon.entities;

public class DBIKey extends DBKey {
    private final String no;
    private final boolean master;

    public DBIKey(String namespace, String database, String no, boolean master) {
        super(namespace, database);
        this.no = no;
        this.master = master;
    }

    public String getNo() {
        return no;
    }

    public boolean isMaster() {
        return master;
    }

    public String toPrometheusKey(String keyName) {
        return keyName + "{" +
                "namespace=\"" + prometheusEscape(this.getNamespace()) + "\"," +
                "name=\"" + prometheusEscape(this.getDatabase()) + "\"," +
                "no=\"" + prometheusEscape(this.getNo()) + "\"," +
                "master=\"" + prometheusEscape(this.isMaster() ? "1" : "0") + "\"" +
                "}";

    }
}
