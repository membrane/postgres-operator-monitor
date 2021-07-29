# Monitoring Operator for Postgres Operator

Collects Prometheus-compatible metrics from [Postgres Operator](https://github.com/zalando/postgres-operator)

## Deployment

Create Namespace if not already present
```bash
kubectl apply -f kubernetes/namespace.yaml
```

Create Roles
```bash
kubectl apply -f kubernetes/roles.yaml
```

Create the actual deployment
```bash
kubectl apply -f kubernetes/template.yaml
```

## Prometheus Config
```
      - job_name: 'pg-op-mon'
        scrape_interval: 150s
        metrics_path: '/pgopmetrics'
        static_configs:
          - targets: [
            'pg-op-mon.monitoring.svc.cluster.local:80',
          ]
```
