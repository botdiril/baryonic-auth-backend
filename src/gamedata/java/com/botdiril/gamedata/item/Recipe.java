package com.botdiril.gamedata.item;

import java.util.List;

import com.botdiril.gamedata.IGameObject;

public record Recipe(
    List<ItemPair> components,
    long amount,
    IGameObject result
)
{

}
