package de.predic8.pgopmon;

import de.predic8.pgopmon.entities.DBKey;
import de.predic8.pgopmon.services.MetricsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MetricsController {

    final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping(value = "/pgopmetrics", produces = "text/plain; version=0.0.4")
    public String metrics() {
        StringBuilder stringBuilder = new StringBuilder();

        metricsService.patroniInstancesWanted.forEach(
                (key, value) -> stringBuilder.append(kvToPrometheus(key, value, "patroni_instances_wanted")));

        metricsService.patroniRunning.forEach(
                (key, value) -> stringBuilder.append(kvToPrometheus(key, value, "patroni_running")));

        metricsService.patroniTimeline.forEach(
                (key, value) -> stringBuilder.append(kvToPrometheus(key, value, "patroni_timeline")));

        metricsService.patroniLocation.forEach(
                (key, value) -> stringBuilder.append(kvToPrometheus(key, value, "patroni_location")));

        metricsService.patroniReplayedLocation.forEach(
                (key, value) -> stringBuilder.append(kvToPrometheus(key, value, "patroni_replayed_location")));

        metricsService.patroniPostgresVersion.forEach(
                (key, value) -> stringBuilder.append(kvToPrometheus(key, value, "patroni_postgres_version")));

        metricsService.patroniVersion.forEach(
                (key, value) -> stringBuilder.append(kvToPrometheus(key, value, "patroni_version")));

        stringBuilder.append("patroni_last_scan ").append(metricsService.lastScan.get()).append("\n");

        return stringBuilder.toString();
    }

    private String kvToPrometheus(DBKey key, Long value, String keyName) {
        return key.toPrometheusKey(keyName) + " " + value + "\n";
    }

    private String kvToPrometheus(DBKey key, Integer value, String keyName) {
        return key.toPrometheusKey(keyName) + " " + value + "\n";
    }
}
