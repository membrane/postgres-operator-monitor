package de.predic8.pgopmon.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.predic8.pgopmon.entities.DBIKey;
import de.predic8.pgopmon.entities.DBIVKey;
import de.predic8.pgopmon.entities.DBKey;
import de.predic8.pgopmon.entities.PostgresInfo;
import okhttp3.*;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

@Service
public class StatusRetrieverService implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(StatusRetrieverService.class);

    final InfoCollector infoCollector;

    final MetricsService ms;

    final CountDownLatch cdl;

    final ObjectMapper om;

    private OkHttpClient httpClient;

    public StatusRetrieverService(InfoCollector infoCollector, MetricsService ms, CountDownLatch cdl, ObjectMapper om) {
        this.infoCollector = infoCollector;
        this.ms = ms;
        this.cdl = cdl;
        this.om = om;
    }

    @PostConstruct
    public void init() {
        httpClient = new OkHttpClient();
    }

    private static class Status {

        private final boolean running;
        private final boolean master;
        private final Long timeline;
        private final Long location;
        private final Long serverVersion;
        private final String patroniVersion;

        public Status(boolean running, boolean master, Long timeline, Long location, Long serverVersion, String patroniVersion) {
            this.running = running;
            this.master = master;
            this.timeline = timeline;
            this.location = location;
            this.serverVersion = serverVersion;
            this.patroniVersion = patroniVersion;
        }

        /*
        # master
        {"state": "running", "postmaster_start_time": "2021-07-04 18:52:14.145 UTC", "role": "master", "server_version": 120007, "cluster_unlocked": false, "xlog": {"location": 350002560888}, "timeline": 1, "replication": [{"usename": "standby", "application_name": "confluence-db-2", "client_addr": "172.17.103.21", "state": "streaming", "sync_state": "sync", "sync_priority": 1}, {"usename": "standby", "application_name": "confluence-db-1", "client_addr": "172.17.157.75", "state": "streaming", "sync_state": "async", "sync_priority": 0}], "database_system_identifier": "6981145906336170710", "patroni": {"version": "2.0.2", "scope": "confluence-db"}}
        # sync slave
        {"state": "running", "postmaster_start_time": "2021-07-04 18:52:18.241 UTC", "role": "replica", "server_version": 120007, "cluster_unlocked": false, "xlog": {"received_location": 350003107024, "replayed_location": 350003107024, "replayed_timestamp": "2021-07-04 18:53:27.672 UTC", "paused": false}, "timeline": 1, "database_system_identifier": "6981145906336170710", "patroni": {"version": "2.0.2", "scope": "confluence-db"}}
        # slave
        {"state": "running", "postmaster_start_time": "2021-07-04 18:52:19.545 UTC", "role": "replica", "server_version": 120007, "cluster_unlocked": false, "xlog": {"received_location": 350003107024, "replayed_location": 350003107024, "replayed_timestamp": "2021-07-04 18:53:27.672 UTC", "paused": false}, "timeline": 1, "database_system_identifier": "6981145906336170710", "patroni": {"version": "2.0.2", "scope": "confluence-db"}}
         */
        public Status(JsonNode response) {
            running = "running".equals(response.path("state").textValue());
            master = "master".equals(response.path("role").textValue());
            timeline = longValueOrNull(response.path("timeline"));
            location = (master) ?
                    longValueOrNull(response.path("xlog").path("location")) :
                    longValueOrNull(response.path("xlog").path("replayed_location"));
            serverVersion = longValueOrNull(response.path("server_version"));
            patroniVersion = response.path("patroni").path("version").textValue();
        }

        public boolean isRunning() {
            return running;
        }

        public boolean isMaster() {
            return master;
        }

        public Long getTimeline() {
            return timeline;
        }

        public Long getLocation() {
            return location;
        }

        public Long getServerVersion() {
            return serverVersion;
        }

        public String getPatroniVersion() {
            return patroniVersion;
        }

        private static Long longValueOrNull(JsonNode n) {
            if (n.isNumber())
                return n.longValue();
            return null;
        }
    }

    @Override
    public void run() {
        try {
            cdl.await();
        } catch (InterruptedException ignored) {
        }
        LOG.info("starting status retrieval");
        while(true) {
            try {
                updateMetricsService();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }

            try {
                //noinspection BusyWait
                Thread.sleep(60000);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void updateMetricsService() {
        List<PostgresInfo> pgs = infoCollector.getPGs();
        HashSet<DBKey> updatedDBKeys = new HashSet<>();
        HashSet<DBIKey> updatedDBIKeys = new HashSet<>();
        HashSet<DBIKey> updatedDBIMasterKeys = new HashSet<>();
        HashSet<DBIKey> updatedDBINonMasterKeys = new HashSet<>();
        HashSet<DBIKey> updatedDBITLKeys = new HashSet<>();
        HashSet<DBIKey> updatedDBPostgresVersionKeys = new HashSet<>();
        HashSet<DBIVKey> updatedDBPatroniVersionKeys = new HashSet<>();

        for (PostgresInfo pg : pgs) {
            DBKey key = new DBKey(pg.getNamespace(), pg.getName());
            updatedDBKeys.add(key);

            ms.patroniInstancesWanted.put(key, pg.getInstances());

            List<Pair<String, String>> ips = infoCollector.getInstanceNumbersAndIPs(pg.getNamespace(), pg.getName());

            for (Pair<String, String> entry : ips) {
                Status status = getStatus(entry.getRight());
                boolean isMaster = status != null && status.isMaster();
                DBIKey key2 = new DBIKey(pg.getNamespace(), pg.getName(), entry.getLeft(), isMaster);

                updatedDBIKeys.add(key2);

                ms.patroniRunning.put(key2, status != null && status.isRunning() ? 1 : 0);

                if (status == null)
                    continue;

                if (status.getServerVersion() != null) {
                    ms.patroniPostgresVersion.put(key2, status.getServerVersion());
                    updatedDBPostgresVersionKeys.add(key2);
                }

                if (status.getPatroniVersion() != null) {
                    DBIVKey key3 = new DBIVKey(
                            key2.getNamespace(), key2.getDatabase(), key2.getNo(),
                            key2.isMaster(), status.getPatroniVersion()
                    );
                    ms.patroniVersion.put(key3, 1L);
                    updatedDBPatroniVersionKeys.add(key3);
                }

                if (status.getTimeline() != null) {
                    ms.patroniTimeline.put(key2, status.getTimeline());
                    updatedDBITLKeys.add(key2);
                }

                if (status.getLocation() != null)
                    if (isMaster) {
                        ms.patroniLocation.put(key2, status.getLocation());
                        updatedDBIMasterKeys.add(key2);
                    } else {
                        ms.patroniReplayedLocation.put(key2, status.getLocation());
                        updatedDBINonMasterKeys.add(key2);
                    }

            }

        }

        removeStaleKeys(updatedDBKeys, ms.patroniInstancesWanted.keySet());
        removeStaleKeys(updatedDBIKeys, ms.patroniRunning.keySet());
        removeStaleKeys(updatedDBITLKeys, ms.patroniTimeline.keySet());
        removeStaleKeys(updatedDBIMasterKeys, ms.patroniLocation.keySet());
        removeStaleKeys(updatedDBINonMasterKeys, ms.patroniReplayedLocation.keySet());
        removeStaleKeys(updatedDBPostgresVersionKeys, ms.patroniPostgresVersion.keySet());
        removeStaleKeys(updatedDBPatroniVersionKeys, ms.patroniVersion.keySet());

        ms.lastScan.set(System.currentTimeMillis());
    }

    public Status getStatus(String ip) {
        if (ip == null || ip.length() == 0)
            return null;

        Call call = httpClient.newCall(new Request.Builder().get().url("http://" + ip + ":8008/health").build());
        try {
            Response res = call.execute();

            if (res.code() != 200) {
                LOG.info("while retrieving " + ip + " status " + res.code() + " was returned.");
                return null;
            }

            ResponseBody body = res.body();
            if (body == null || body.contentLength() == 0) {
                LOG.info("while retrieving " + ip + " no body was returned.");
                return null;
            }

            return new Status(om.valueToTree(body.string()));

        } catch (Exception e) {
            LOG.info("while retrieving " + ip, e);
            return null;
        }
    }

    private <T> void removeStaleKeys(Set<T> updated, Set<T> setToRemoveFrom) {
        setToRemoveFrom.removeIf(t -> !updated.contains(t));
    }
}
