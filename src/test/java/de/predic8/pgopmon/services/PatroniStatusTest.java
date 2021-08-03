package de.predic8.pgopmon.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.predic8.pgopmon.services.StatusRetrieverService.PatroniStatus;
import org.junit.Test;

import static org.junit.Assert.*;

public class PatroniStatusTest {

    private final ObjectMapper om = new ObjectMapper();

    @Test
    public void statusMasterTest() throws Exception {
        final String masterResponse = "{\"state\": \"running\", \"postmaster_start_time\": \"2021-07-04 18:52:14.145 UTC\", \"role\": \"master\", \"server_version\": 120007, \"cluster_unlocked\": false, \"xlog\": {\"location\": 350002560888}, \"timeline\": 1, \"replication\": [{\"usename\": \"standby\", \"application_name\": \"confluence-db-2\", \"client_addr\": \"172.17.103.21\", \"state\": \"streaming\", \"sync_state\": \"sync\", \"sync_priority\": 1}, {\"usename\": \"standby\", \"application_name\": \"confluence-db-1\", \"client_addr\": \"172.17.157.75\", \"state\": \"streaming\", \"sync_state\": \"async\", \"sync_priority\": 0}], \"database_system_identifier\": \"6981145906336170710\", \"patroni\": {\"version\": \"2.0.2\", \"scope\": \"confluence-db\"}}";
        PatroniStatus masterStatus = new PatroniStatus(om.readTree(masterResponse));

        assertTrue(masterStatus.isRunning());
        assertTrue(masterStatus.isMaster());
        assertEquals(1L, (long) masterStatus.getTimeline());
        assertEquals(350002560888L, (long) masterStatus.getLocation());
        assertEquals(120007L, (long) masterStatus.getServerVersion());
        assertEquals("2.0.2", masterStatus.getPatroniVersion());
    }

    @Test
    public void statusSyncSlaveTest() throws Exception {
        final String syncSlaveResponse = "{\"state\": \"running\", \"postmaster_start_time\": \"2021-07-04 18:52:18.241 UTC\", \"role\": \"replica\", \"server_version\": 120007, \"cluster_unlocked\": false, \"xlog\": {\"received_location\": 350003107024, \"replayed_location\": 350003107024, \"replayed_timestamp\": \"2021-07-04 18:53:27.672 UTC\", \"paused\": false}, \"timeline\": 1, \"database_system_identifier\": \"6981145906336170710\", \"patroni\": {\"version\": \"2.0.2\", \"scope\": \"confluence-db\"}}";
        PatroniStatus syncSlaveStatus = new PatroniStatus(om.readTree(syncSlaveResponse));

        assertTrue(syncSlaveStatus.isRunning());
        assertFalse(syncSlaveStatus.isMaster());
        assertEquals(1L, (long) syncSlaveStatus.getTimeline());
        assertEquals(350003107024L, (long) syncSlaveStatus.getLocation());
        assertEquals(120007L, (long) syncSlaveStatus.getServerVersion());
        assertEquals("2.0.2", syncSlaveStatus.getPatroniVersion());
    }

    @Test
    public void statusSlaveTest() throws Exception {
        final String slaveResponse = "{\"state\": \"running\", \"postmaster_start_time\": \"2021-07-04 18:52:19.545 UTC\", \"role\": \"replica\", \"server_version\": 120007, \"cluster_unlocked\": false, \"xlog\": {\"received_location\": 350003107024, \"replayed_location\": 350003107024, \"replayed_timestamp\": \"2021-07-04 18:53:27.672 UTC\", \"paused\": false}, \"timeline\": 1, \"database_system_identifier\": \"6981145906336170710\", \"patroni\": {\"version\": \"2.0.2\", \"scope\": \"confluence-db\"}}";
        PatroniStatus slaveStatus = new PatroniStatus(om.readTree(slaveResponse));

        assertTrue(slaveStatus.isRunning());
        assertFalse(slaveStatus.isMaster());
        assertEquals(1L, (long) slaveStatus.getTimeline());
        assertEquals(350003107024L, (long) slaveStatus.getLocation());
        assertEquals(120007L, (long) slaveStatus.getServerVersion());
        assertEquals("2.0.2", slaveStatus.getPatroniVersion());
    }

}
