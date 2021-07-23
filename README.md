# Monitoring Operator for Postgres Operator

Collects Prometheus-compatible metrics from [Postgres Operator](https://github.com/zalando/postgres-operator)

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