package com.botdiril.gamedata.metrics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum EnumMetric
{
    COINS("coins", Long.class),
    KEKS("keks", Long.class),
    TOKENS("tokens", Long.class),
    KEYS("keys", Long.class),
    DUST("dust", Long.class),
    LEVEL("level", Integer.class),
    XP("xp", Long.class);

    private static final String PREFIX = "um_";
    private static final Map<String, EnumMetric> METRIC_MAP = new HashMap<>();

    static
    {
        Arrays.stream(EnumMetric.values()).forEach(metric -> METRIC_MAP.put(metric.key, metric));
    }

    private final String key;
    private final String dbKey;
    private final Class<?> valueType;

    EnumMetric(String key, Class<?> valueType)
    {
        this.key = key;
        this.dbKey = PREFIX + key;
        this.valueType = valueType;
    }

    public String getKey()
    {
        return this.key;
    }

    public String getDBKey()
    {
        return this.dbKey;
    }

    public Class<?> getValueType()
    {
        return this.valueType;
    }

    public static Optional<EnumMetric> getMetric(String key)
    {
        return Optional.ofNullable(METRIC_MAP.get(key));
    }
}
