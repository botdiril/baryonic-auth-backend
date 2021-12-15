package com.botdiril.gamedata.achievement;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.botdiril.gamedata.IGameObject;
import com.botdiril.gamedata.ItemLookup;

public class Achievement implements IGameObject
{
    private final int id;
    private final String name;

    private static final Map<String, Achievement> storage = new HashMap<>();

    public Achievement(String name)
    {
        this.name = name;

        this.id = ItemLookup.make(this.name);
        storage.put(this.name, this);
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public int getID()
    {
        return this.id;
    }

    public static Achievement getByName(String name)
    {
        return storage.get(name.toLowerCase(Locale.ROOT));
    }
}
