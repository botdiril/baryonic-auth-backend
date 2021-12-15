package com.botdiril.gamedata.item;

import org.jetbrains.annotations.Nullable;

import java.util.*;

import com.botdiril.gamedata.IGameObject;
import com.botdiril.gamedata.ItemLookup;

public class Item implements IGameObject
{
    private static final Map<String, Item> items = new HashMap<>();

    public static @Nullable Item getItemByID(int id)
    {
        return items.get(ItemLookup.getName(id));
    }

    public static @Nullable Item getItemByName(String name)
    {
        return items.get(name.toLowerCase(Locale.ROOT));
    }

    public static Collection<Item> items()
    {
        return Collections.unmodifiableCollection(items.values());
    }

    private final String name;

    private final int id;

    public Item(String name)
    {
        this.name = name;

        this.id = ItemLookup.make(this.name);
        items.put(this.name, this);
    }

    @Override
    public int getID()
    {
        return this.id;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Item it)
            return it.getID() == this.getID();

        return false;
    }

    @Override
    public int hashCode()
    {
        return this.getID();
    }

    public ItemPair ofAmount(long amount)
    {
        return ItemPair.of(this, amount);
    }
}
