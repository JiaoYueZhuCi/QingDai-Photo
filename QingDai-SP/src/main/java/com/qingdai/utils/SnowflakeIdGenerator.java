package com.qingdai.utils;

public class SnowflakeIdGenerator {
    // 起始时间戳（2023-01-01 00:00:00）
    private static final long START_TIMESTAMP = 1672531200000L;

    // 各部分的位数
    private static final long SEQUENCE_BIT = 12;   // 序列号位数
    private static final long MACHINE_BIT = 5;     // 机器ID位数
    private static final long DATACENTER_BIT = 5;  // 数据中心ID位数

    // 最大值计算（位运算优化）
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);
    private static final long MAX_MACHINE = ~(-1L << MACHINE_BIT);
    private static final long MAX_DATACENTER = ~(-1L << DATACENTER_BIT);

    // 各部分的左移位数
    private static final long MACHINE_SHIFT = SEQUENCE_BIT;
    private static final long DATACENTER_SHIFT = SEQUENCE_BIT + MACHINE_BIT;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BIT + MACHINE_BIT + DATACENTER_BIT;

    private final long machineId;     // 机器ID（0-31）
    private final long datacenterId;  // 数据中心ID（0-31）
    private long sequence = 0L;      // 序列号
    private long lastTimestamp = -1L; // 上次生成时间戳

    public SnowflakeIdGenerator(long machineId, long datacenterId) {
        if (machineId > MAX_MACHINE || machineId < 0) {
            throw new IllegalArgumentException("机器ID范围错误: 0~" + MAX_MACHINE);
        }
        if (datacenterId > MAX_DATACENTER || datacenterId < 0) {
            throw new IllegalArgumentException("数据中心ID范围错误: 0~" + MAX_DATACENTER);
        }
        this.machineId = machineId;
        this.datacenterId = datacenterId;
    }

    /**
     * 生成下一个唯一ID（线程安全）
     */
    public synchronized long nextId() {
        long currentTimestamp = getCurrentTimestamp();

        // 时钟回拨检查
        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨异常，拒绝生成ID。回拨时间: " +
                (lastTimestamp - currentTimestamp) + "ms");
        }

        // 同一毫秒内生成
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) { // 当前毫秒序列号用尽，等待下一毫秒
                currentTimestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L; // 新毫秒重置序列号
        }

        lastTimestamp = currentTimestamp;

        // 组合各部分生成最终ID
        return ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_SHIFT)
                | (machineId << MACHINE_SHIFT)
                | sequence;
    }

    /**
     * 获取当前时间戳（毫秒）
     */
    private long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 阻塞直到下一毫秒
     */
    private long waitNextMillis(long lastTimestamp) {
        long currentTimestamp = getCurrentTimestamp();
        while (currentTimestamp <= lastTimestamp) {
            currentTimestamp = getCurrentTimestamp();
        }
        return currentTimestamp;
    }
}
