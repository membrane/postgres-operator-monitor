package de.predic8.pgopmon.crd;

import de.predic8.kubernetesclient.genericapi.ARList;
import de.predic8.kubernetesclient.genericapi.ArbitraryResourceApi;
import de.predic8.kubernetesclient.genericapi.Watcher;
import de.predic8.kubernetesclient.util.KubeUtil;
import de.predic8.kubernetesclient.util.KubernetesVersion;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class PostgresqlOps {
    private ArbitraryResourceApi<Postgresql> ops;

    public PostgresqlOps(KubernetesVersion kubernetesVersion, KubeUtil kubeUtil, ApiClient apiClient,
                         @Qualifier("slowApiClient") ApiClient slowApiClient) {
        this.kubernetesVersion = kubernetesVersion;
        this.kubeUtil = kubeUtil;
        this.apiClient = apiClient;
        this.slowApiClient = slowApiClient;
    }

    final KubernetesVersion kubernetesVersion;
    final KubeUtil kubeUtil;
    final ApiClient apiClient;
    final ApiClient slowApiClient;

    @PostConstruct
    public void init() {
        if (kubernetesVersion.supportsCRD()) {
            ops = new ArbitraryResourceApi<>(apiClient, slowApiClient, "acid.zalan.do", "v1", "postgresqls");
            return;
        }
        throw new RuntimeException("Not implemented.");
    }

    public ARList<Postgresql> list() throws ApiException {
        return ops.list(null, null, null, null, null, null, null, null, null, null);
    }


    public void watch(String resourceVersion, Watcher<Postgresql> watcher) throws ApiException {
        ops.watchAsync(null, resourceVersion, Postgresql.class, watcher);
    }

    public Postgresql getByName(String namespace, String clusterName) throws ApiException {
        return kubeUtil.ifExists(ops.readCall(namespace, clusterName, null, null, null, null), Postgresql.class);
    }

}
