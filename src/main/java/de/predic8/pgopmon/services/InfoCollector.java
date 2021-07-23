package de.predic8.pgopmon.services;

import de.predic8.pgopmon.crd.Postgresql;
import de.predic8.pgopmon.entities.PodInfo;
import de.predic8.pgopmon.entities.PostgresInfo;
import io.kubernetes.client.openapi.models.V1Pod;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InfoCollector {

    private static final Logger LOG = LoggerFactory.getLogger(InfoCollector.class);

    Map<String, PodInfo> podInfo = new HashMap<>();

    private PodInfo info(V1Pod pod) {
        return new PodInfo(pod.getStatus() != null ? pod.getStatus().getPodIP() : null,
                pod.getMetadata().getNamespace(),
                pod.getMetadata().getName(),
                pod.getMetadata().getLabels() != null ? pod.getMetadata().getLabels().get("version") : null);
    }

    public void resetPods(List<V1Pod> pods) {
        synchronized (this) {
            podInfo.clear();
            pods.stream().forEach(pod -> podInfo.put(pod.getMetadata().getUid(), info(pod)));
        }
    }

    public void updatedPod(V1Pod pod) {
        synchronized (this) {
            podInfo.put(pod.getMetadata().getUid(), info(pod));
        }
    }

    public void removedPod(V1Pod pod) {
        synchronized (this) {
            podInfo.remove(pod.getMetadata().getUid());
        }
    }

    Map<String, PostgresInfo> postgresInfo = new HashMap<>();

    private PostgresInfo info(Postgresql pg) {
        return new PostgresInfo(pg.getMetadata().getNamespace(), pg.getMetadata().getName(), getNumberOfInstances(pg));
    }

    private Integer getNumberOfInstances(Postgresql pg) {
        return pg.getSpec() == null || pg.getSpec().get("numberOfInstances") == null ? null : ((Double) pg.getSpec().get("numberOfInstances")).intValue();
    }

    public void resetPostgresqls(List<Postgresql> postgresqls) {
        synchronized (this) {
            postgresInfo.clear();
            postgresqls.forEach(pg -> postgresInfo.put(pg.getMetadata().getUid(), info(pg)));
        }
    }

    public void updatedPostgresql(Postgresql postgresql) {
        synchronized (this) {
            postgresInfo.put(postgresql.getMetadata().getUid(), info(postgresql));
        }
    }

    public void removedPostgresql(Postgresql postgresql) {
        synchronized (this) {
            postgresInfo.remove(postgresql.getMetadata().getUid());
        }
    }

    public List<PostgresInfo> getPGs() {
        synchronized (this) {
            return new ArrayList<>(postgresInfo.values());
        }
    }

    public List<Pair<String, String>> getInstanceNumbersAndIPs(String namespace, String name) {
        List<Pair<String, String>> res = new ArrayList<>();
        synchronized (this) {
            podInfo.values().forEach(pod -> {
                if (pod.getNamespace().equals(namespace) && name.equals(pod.getVersion()) && pod.getIp() != null) {
                    String instanceNumber = pod.getName().startsWith(name + "-") ? pod.getName().substring(name.length() + 1) : "?";
                    res.add(new ImmutablePair<>(instanceNumber, pod.getIp()));
                }
            });
        }
        return res;
    }
}