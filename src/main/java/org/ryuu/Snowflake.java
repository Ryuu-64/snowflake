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

    private final long epoch;

    private final long instanceId;

    private final long nonMonotonicTimeTolerance;

    private long sequenceId = 0L;

    private long lastTimestamp = -1L;


    public Snowflake(long epoch, long instanceId) {
        this(epoch, instanceId, 0);
    }

    public Snowflake(long epoch, long instanceId, long nonMonotonicTimeTolerance) {
        if (instanceId < 0 || instanceId > MAX_INSTANCE_ID) {
            throw new IllegalArgumentException("instanceId");
        }

        if (nonMonotonicTimeTolerance < 0) {
            throw new IllegalArgumentException("nonMonotonicTimeTolerance");
        }

        this.epoch = epoch;
        this.instanceId = instanceId;
        this.nonMonotonicTimeTolerance = nonMonotonicTimeTolerance;
    }

    public long getInstanceId(long id) {
        return id >> INSTANCE_ID_SHIFT & MAX_INSTANCE_ID;
    }

    public long getTimestamp(long id) {
        return (id >> TIMESTAMP_SHIFT & MAX_TIMESTAMP) + epoch;
    }

    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < this.lastTimestamp) {
            if (this.lastTimestamp - timestamp < nonMonotonicTimeTolerance) {
                timestamp = lastTimestamp;
            } else {
                throw new IllegalStateException("Clock moved backwards.");
            }
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

        return timestamp - epoch << TIMESTAMP_SHIFT |
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
