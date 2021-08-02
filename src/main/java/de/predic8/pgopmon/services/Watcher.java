package de.predic8.pgopmon.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.predic8.kubernetesclient.client.NamespacedApiClient;
import de.predic8.kubernetesclient.genericapi.ARList;
import de.predic8.kubernetesclient.genericapi.ArbitraryResourceApi;
import de.predic8.pgopmon.crd.Postgresql;
import de.predic8.pgopmon.crd.PostgresqlOps;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.JSON;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Component
@DependsOn("kubernetesClient")
public class Watcher implements Runnable {

	Logger logger = LoggerFactory.getLogger(Watcher.class);

	private final InfoCollector worker;
	private final PostgresqlOps op;
	final ApiClient slowApiClient;
	final CoreV1Api api;
	final CountDownLatch cdl;
	final NamespacedApiClient apiClient;
	final ObjectMapper om;


	public Watcher(NamespacedApiClient apiClient, PostgresqlOps op, InfoCollector worker, ObjectMapper om, @Qualifier("slowApiClient") ApiClient slowApiClient, CountDownLatch cdl, CoreV1Api api) {
		this.apiClient = apiClient;
		this.op = op;
		this.worker = worker;
		this.om = om;
		this.slowApiClient = slowApiClient;
		this.cdl = cdl;
		this.api = api;
	}

	@Override
	public void run() {
		try {
			logger.info("Start monitoring the pods...");
			monitorPods();

			logger.info("Listing Postgresqls...");
			monitorPostgresql();

			cdl.countDown();

			while(true)
				Thread.sleep(1000000);

		} catch (Exception e){
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void monitorPods() {
		try {
			V1PodList pods = api.listPodForAllNamespaces(null, null, null, "app=spilo", null, null, null, null, null);
			worker.resetPods(pods.getItems());

			ArbitraryResourceApi<V1Pod> api2 = new ArbitraryResourceApi<>(apiClient, slowApiClient, null, "v1", "pods");
			api2.watchAsync(null, requireNonNull(pods.getMetadata()).getResourceVersion(), V1Pod.class, null, "app=spilo", getPodWatcher());
		} catch (ApiException e) {
			e.printStackTrace();
			waitAndRestart("pod watcher", this::monitorPods, 5000);
		}
	}

	private void monitorPostgresql() {
		try {
			ARList<Postgresql> list = op.list();
			worker.resetPostgresqls(convertToPostgresqlList(list));

			logger.info("Start monitoring the Postgresqls...");
			op.watch(list.getMetadata().getResourceVersion(), getPostgresqlWatcher());

		} catch (Exception e) {
			e.printStackTrace();
			waitAndRestart("postgres watcher", this::monitorPostgresql, 5000);
		}
	}

	private de.predic8.kubernetesclient.genericapi.Watcher<V1Pod> getPodWatcher() {
		return new de.predic8.kubernetesclient.genericapi.Watcher<V1Pod>() {
			@Override
			public void eventReceived(Action action, V1Pod resource) {
				if (action == Action.DELETED)
					worker.removedPod(resource);
				else
					worker.updatedPod(resource);
			}

			@Override
			public void onClose(ApiException cause) {
				logger.info("pod watcher closed.", cause);
				waitAndRestart("pod watcher", () -> monitorPods(), 5000);
			}
		};
	}

	private de.predic8.kubernetesclient.genericapi.Watcher<Postgresql> getPostgresqlWatcher() {
		return new de.predic8.kubernetesclient.genericapi.Watcher<Postgresql>() {
			@Override
			public void eventReceived(Action action, Postgresql resource) {
				if (action == Action.DELETED)
					worker.removedPostgresql(resource);
				else
					worker.updatedPostgresql(resource);
			}

			@Override
			public void onClose(ApiException cause) {
				logger.info("postgres watcher closed.", cause);
				waitAndRestart("postgres watcher", () -> monitorPostgresql(), 5000);
			}
		};
	}

	private void waitAndRestart(String logName, Runnable restart, long delay) {
		logger.info("{} failed.", logName);
		try {
			Thread.sleep(delay);
		} catch (InterruptedException ignored) { }
		logger.info("restarting {}.", logName);
		restart.run();
	}

	private List<Postgresql> convertToPostgresqlList(ARList<Postgresql> list) {
		JSON json = new JSON();
		return list.getItems().stream()
				.map((Object o) -> toPostgresql(json, o))
				.collect(Collectors.toList());
	}

	private Postgresql toPostgresql(JSON json, Object o) {
		return json.deserialize(json.serialize(o), Postgresql.class);
	}
}
