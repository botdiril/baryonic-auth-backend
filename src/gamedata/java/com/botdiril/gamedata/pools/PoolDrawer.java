package com.botdiril.gamedata.pools;

import java.util.ArrayList;
import java.util.List;

import com.botdiril.util.BotdirilRnd;

public class PoolDrawer<T>
{
    private final List<PoolWrapper<T>> pools = new ArrayList<>();
    private long weightSum = 0;

    public PoolDrawer<T> add(long weight, LootPool<T> pool)
    {
        this.pools.add(new PoolWrapper<>(weight, pool));
        this.weightSum += weight;

        return this;
    }

    public T draw()
    {
        var rd = BotdirilRnd.rdg().nextLong(0, this.weightSum);
        var ptr = 0;

        for (var pool : this.pools)
        {
            ptr += pool.weight();

            if (ptr >= rd)
            {
                var internalPool = pool.pool();
                return internalPool.draw();
            }
        }

        return null;
    }
}
