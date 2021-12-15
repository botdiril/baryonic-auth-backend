package com.botdiril.gamedata.pools;

public record PoolWrapper<T>(long weight, LootPool<T> pool)
{
}
