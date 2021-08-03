# Monitoring Operator for Postgres Operator

Collects Prometheus-compatible metrics from [Postgres Operator](https://github.com/zalando/postgres-operator)

## Metrics
pg-op-mon exposes these metrics:
```
patroni_instances_wanted{namespace='demo',database='demo'} 3
patroni_running{namespace='demo',database='demo',no='0',master='1'} 1
patroni_running{namespace='demo',database='demo',no='1',master='0'} 1
patroni_running{namespace='demo',database='demo',no='2',master='0'} 1
patroni_timeline{namespace='demo',database='demo',no='0',master='1'} 80
patroni_timeline{namespace='demo',database='demo',no='1',master='0'} 80
patroni_timeline{namespace='demo',database='demo',no='2',master='0'} 80
patroni_location{namespace='demo',database='demo',no='0',master='1'} 81002324952
patroni_replayed_location{namespace='demo',database='demo',no='1',master='0'} 81002358616
patroni_replayed_location{namespace='demo',database='demo',no='2',master='0'} 81002358616
patroni_version{namespace='demo',database='demo',no='0',master='1',patroni_version='1.6.3'} 1
patroni_version{namespace='demo',database='demo',no='1',master='0',patroni_version='1.6.3'} 1
patroni_version{namespace='demo',database='demo',no='2',master='0',patroni_version='1.6.3'} 1
patroni_postgres_version{namespace='demo',database='demo',no='0',master='1'} 120007
patroni_postgres_version{namespace='demo',database='demo',no='1',master='0'} 120007
patroni_postgres_version{namespace='demo',database='demo',no='2',master='0'} 120007

last_scan 1234567
```
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
```yaml
  - job_name: 'pg-op-mon'
    scrape_interval: 150s
    metrics_path: '/pgopmetrics'
    static_configs:
      - targets: [
        'pg-op-mon.monitoring.svc.cluster.local:80',
      ]
```
