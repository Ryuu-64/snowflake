package org.ryuu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SnowflakeTest {
    private Snowflake snowflake;

    @BeforeEach
    void setUp() {
        snowflake = new Snowflake(getEpochMilli(), 0);
    }

    @Test
    void getInstanceId() {
        long initInstanceId = 0;
        long snowflakeId = snowflake.nextId();
        long instanceId = snowflake.getInstanceId(snowflakeId);
        assertEquals(initInstanceId, instanceId);
    }

    @Test
    void getTimestamp() {
        long currentTimeMillis = System.currentTimeMillis();
        long snowflakeId = snowflake.nextId();
        long timestamp = snowflake.getTimestamp(snowflakeId);
        assertEquals(currentTimeMillis, timestamp);
    }

    @Test
    void nextId() {
        HashSet<Long> snowflakeIds = new HashSet<>();
        int idGenerateCount = 1_000_000;
        for (int i = 0; i < idGenerateCount; i++) {
            snowflakeIds.add(snowflake.nextId());
        }
        assertEquals(idGenerateCount, snowflakeIds.size());
    }

    /**
     * "2020-01-01T00:00:00Z" or "2020-01-01T00:00:00.000Z"
     */
    long getEpochMilli() {
        return Instant.parse("2020-01-01T00:00:00Z").toEpochMilli();
    }
}
