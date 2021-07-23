package de.predic8.pgopmon.services;

import de.predic8.pgopmon.entities.DBIKey;
import de.predic8.pgopmon.entities.DBIVKey;
import de.predic8.pgopmon.entities.DBKey;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MetricsService {

    /*
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
     */
    public ConcurrentHashMap<DBKey, Integer> patroniInstancesWanted = new ConcurrentHashMap<>();
    public ConcurrentHashMap<DBIKey, Integer> patroniRunning = new ConcurrentHashMap<>();
    public ConcurrentHashMap<DBIKey, Long> patroniTimeline = new ConcurrentHashMap<>();
    public ConcurrentHashMap<DBIKey, Long> patroniLocation = new ConcurrentHashMap<>();
    public ConcurrentHashMap<DBIKey, Long> patroniReplayedLocation = new ConcurrentHashMap<>();
    public ConcurrentHashMap<DBIVKey, Long> patroniVersion = new ConcurrentHashMap<>();
    public ConcurrentHashMap<DBIKey, Long> patroniPostgresVersion = new ConcurrentHashMap<>();

    public AtomicLong lastScan = new AtomicLong();
}
