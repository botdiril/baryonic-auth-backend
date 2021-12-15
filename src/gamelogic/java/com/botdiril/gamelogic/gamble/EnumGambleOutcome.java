package com.botdiril.gamelogic.gamble;

import com.botdiril.gamelogic.weighted.IWeightedRandom;

public enum EnumGambleOutcome implements IWeightedRandom<EnumGambleOutcome>
{
    LOSE_EVERYTHING(6.8, gi -> GambleResult.of(-gi.gambledKeks())),

    LOSE_THREE_QUARTERS(15, gi -> GambleResult.of(Math.round(-gi.gambledKeks() * 0.75))),

    LOSE_HALF(30, gi -> GambleResult.of(Math.round(-gi.gambledKeks() * 0.5))),

    LOSE_QUARTER(72, gi -> GambleResult.of(Math.round(-gi.gambledKeks() * 0.25))),

    WIN_THIRD(40, gi -> GambleResult.of(Math.round(gi.gambledKeks() * 0.33))),

    WIN_HALF(22, gi -> GambleResult.of(Math.round(gi.gambledKeks() * 0.5))),

    WIN_DOUBLE(8, gi -> GambleResult.of(gi.gambledKeks())),

    WIN_TRIPLE(5, gi -> GambleResult.of(gi.gambledKeks() * 2)),

    WIN_QUADRUPLE(1, gi -> GambleResult.of(gi.gambledKeks() * 3)),

    JACKPOT(0.2, gi -> GambleResult.of(gi.jackpotPool()));

    private final double weight;
    private final GambleFunction calc;

    EnumGambleOutcome(double weight, GambleFunction calc)
    {
        this.weight = weight;
        this.calc = calc;
    }

    public GambleResult apply(GambleInput gambleInput)
    {
        var gambleResult = this.calc.gambleModifier(gambleInput);
        gambleResult.setOutcome(this);
        return gambleResult;
    }

    public double getWeight()
    {
        return this.weight;
    }
}
