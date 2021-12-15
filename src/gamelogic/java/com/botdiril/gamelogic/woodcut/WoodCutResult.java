package com.botdiril.gamelogic.woodcut;

import java.util.Set;

import com.botdiril.gamedata.achievement.Achievement;
import com.botdiril.gamedata.item.ItemDrops;

public record WoodCutResult(
    EnumWoodCutOutcome outcome,
    EnumWoodCutOutcome.EnumWoodCutYield yieldModifier,
    long earnedWood,
    long earnedXP,
    ItemDrops earnedItems,
    Set<Achievement> achievements
)
{
}
