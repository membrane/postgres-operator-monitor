package de.predic8.pgopmon;

import de.predic8.pgopmon.services.StatusRetrieverService;
import de.predic8.pgopmon.services.Watcher;
import de.predic8.kubernetesclient.client.NamespacedApiClient;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
@EnableAsync
public class PostgresOperatorMonitorApplication {
	private static final Logger log = LoggerFactory.getLogger(PostgresOperatorMonitorApplication.class);

	@Value("${kubernetes.client.namespace}")
	private String namespace;

	public static void main(String[] args) {
		try {
			SpringApplication.run(PostgresOperatorMonitorApplication.class, args);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Bean
	public KubernetesClient client(@Value("${kubernetes.master}") String master) {
		try {
			Config config = new ConfigBuilder()
					.withConnectionTimeout(60000)
					.withRequestTimeout(60000)
					.build();
			config.setMasterUrl(master);
			return new DefaultKubernetesClient(config);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Bean
	public NamespacedApiClient namespacedApiClient() {
		return new NamespacedApiClient() {
			@Override
			public String getMyNamespace() {
				return namespace;
			}
		};
	}

	@Bean
	public CountDownLatch countDownLatch() {
		return new CountDownLatch(1);
	}

	@Bean
	public Object threadStarter(StatusRetrieverService statusRetrieverService, Watcher watcher) {
		log.info("Initializing threadStarter");
		Thread t = new Thread(new Runnable() {

			private Thread watcherThread;
			private Thread jobThread;

			@Override
			public void run() {
				if (needsToBeStarted(jobThread)) {
					jobThread = new Thread(statusRetrieverService);
					jobThread.setName("worker");
					jobThread.start();
				}

				if (needsToBeStarted(watcherThread)) {
					watcherThread = new Thread(watcher);
					watcherThread.setName("watcher");
					watcherThread.start();
				}

			}

			private boolean needsToBeStarted(Thread t) {
				if (t == null) return true;
				return !t.isAlive();
			}
		});
		t.start();
		return t;
	}


}
