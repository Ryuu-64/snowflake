package org.ryuu;

public class Snowflake {
    private static final int TIMESTAMP_BITS = 41;
    private static final int INSTANCE_ID_BITS = 10;
    private static final int SEQUENCE_BITS = 12;
    private static final int INSTANCE_ID_SHIFT = SEQUENCE_BITS;
    private static final int TIMESTAMP_SHIFT = SEQUENCE_BITS + INSTANCE_ID_BITS;
    private static final long MAX_INSTANCE_ID = (1L << INSTANCE_ID_BITS) - 1;
    private static final long MAX_TIMESTAMP = (1L << TIMESTAMP_BITS) - 1;
    private static final long MAX_SEQUENCE_ID = (1L << SEQUENCE_BITS) - 1;
    private final long twepoch;
    private final long instanceId;
    private long sequenceId = 0L;
    private long lastTimestamp = -1L;

    public Snowflake(long twepoch, long instanceId) {
        if (instanceId < 0 || instanceId > MAX_INSTANCE_ID) {
            throw new IllegalArgumentException("instanceId");
        }

        this.twepoch = twepoch;
        this.instanceId = instanceId;
    }

    public long getInstanceId(long id) {
        return id >> INSTANCE_ID_SHIFT & MAX_INSTANCE_ID;
    }

    public long getTimestamp(long id) {
        return (id >> TIMESTAMP_SHIFT & MAX_TIMESTAMP) + twepoch;
    }

    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < this.lastTimestamp) {
            throw new IllegalStateException("Clock moved backwards.");
        }

        if (timestamp == this.lastTimestamp) {
            long sequence = (this.sequenceId + 1) & MAX_SEQUENCE_ID;
            if (sequence == 0) {
                timestamp = getNextTimestamp(lastTimestamp);
            }
            this.sequenceId = sequence;
        } else {
            sequenceId = 0L;
        }

        lastTimestamp = timestamp;

        return timestamp - twepoch << TIMESTAMP_SHIFT |
               instanceId << INSTANCE_ID_SHIFT |
               sequenceId;
    }

    private long getNextTimestamp(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp == lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        if (timestamp < lastTimestamp) {
            throw new IllegalStateException();
        }
        return timestamp;
    }
}