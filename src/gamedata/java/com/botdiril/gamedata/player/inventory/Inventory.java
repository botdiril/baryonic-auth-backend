package com.botdiril.gamedata.player.inventory;

import org.jetbrains.annotations.NotNull;

import com.botdiril.framework.sql.connection.WriteDBConnection;
import com.botdiril.gamedata.achievement.Achievement;
import com.botdiril.gamedata.card.Card;
import com.botdiril.gamedata.card.EnumCardModifier;
import com.botdiril.gamedata.item.Item;
import com.botdiril.gamedata.item.ItemCurrency;
import com.botdiril.gamedata.player.properties.PlayerProperties;
import com.botdiril.gamedata.player.properties.ReadOnlyPlayerProperties;
import com.botdiril.gamedata.timers.EnumTimer;
import com.botdiril.gamedata.timers.TimerUtil;

public class Inventory extends ReadOnlyInventory
{
    protected transient final WriteDBConnection db;
    protected transient final PlayerProperties properties;

    public Inventory(WriteDBConnection db, int fkID)
    {
        super(db, fkID);
        this.db = db;
        this.properties = new PlayerProperties(this.db, this.fkid);
    }


    ///
    /// ACHIEVEMENTS
    ///

    public boolean fireAchievement(@NotNull Achievement achievement)
    {
        return this.db.query("SELECT * FROM achievements WHERE fk_us_id=? AND fk_il_id=?", rs ->
        {
            if (!rs.next())
            {
                this.db.simpleUpdate("INSERT INTO achievements (fk_us_id, fk_il_id) VALUES (?, ?)", this.fkid, achievement.getID());
                return true;
            }
            return false;
        }, this.fkid, achievement.getID());
    }


    ///
    /// TIMERS
    ///

    public long checkTimer(@NotNull EnumTimer timer)
    {
        var tt = this.getTimer(timer);
        long currentTime = System.currentTimeMillis();

        if (currentTime > tt)
        {
            return TimerUtil.TIMER_OFF_COOLDOWN;
        }

        return tt - currentTime;
    }

    public void resetTimer(@NotNull EnumTimer timer)
    {
        this.setTimer(timer, 0);
    }

    public void setTimer(@NotNull EnumTimer timer, long timestamp)
    {
        this.db.query("SELECT tm_time FROM timers WHERE fk_us_id=? AND fk_il_id=?", res ->
        {
            if (res.next())
            {
                this.db.simpleUpdate("UPDATE timers SET tm_time=? WHERE fk_us_id=? AND fk_il_id=?", timestamp, this.fkid, timer.getID());
            }
            else
            {
                this.db.simpleUpdate("INSERT INTO timers (fk_us_id, fk_il_id, tm_time)  VALUES (?, ?, ?)", this.fkid, timer.getID(), timestamp);
            }

            return null;
        }, this.fkid, timer.getID());
    }

    public long useTimer(@NotNull EnumTimer timer)
    {
        var tt = this.getTimer(timer);
        long currentTime = System.currentTimeMillis();

        if (currentTime > tt)
        {
            this.setTimer(timer, currentTime + timer.getTimeOffset());
            return TimerUtil.TIMER_OFF_COOLDOWN;
        }
        return tt - currentTime;
    }

    public long useTimerModified(@NotNull EnumTimer timer, double multiplier)
    {
        var tt = this.getTimer(timer);
        long currentTime = System.currentTimeMillis();

        if (currentTime > tt)
        {
            var offset = timer.getTimeOffset();
            offset *= multiplier;

            this.setTimer(timer, currentTime + offset);
            return TimerUtil.TIMER_OFF_COOLDOWN;
        }

        return tt - currentTime;
    }

    // This differentiates in the fact that this overrides the time even when
    // waiting
    public long useTimerOverride(@NotNull EnumTimer timer)
    {
        var tt = this.getTimer(timer);
        long currentTime = System.currentTimeMillis();

        if (currentTime > tt)
        {
            this.setTimer(timer, currentTime + timer.getTimeOffset());
            return TimerUtil.TIMER_OFF_COOLDOWN;
        }

        this.setTimer(timer, currentTime + timer.getTimeOffset());
        return tt - currentTime;
    }


    ///
    /// ITEM SETS
    ///

    public void setCard(@NotNull Card item, long amount)
    {
        this.db.query("SELECT cr_amount FROM cards WHERE fk_us_id=? AND fk_il_id=?", res ->
        {
            if (res.next())
            {
                this.db.simpleUpdate("UPDATE cards SET cr_amount=? WHERE fk_us_id=? AND fk_il_id=?", amount, this.fkid, item.getID());
            }
            else
            {
                this.db.simpleUpdate("INSERT INTO cards (fk_us_id, fk_il_id, cr_amount)  VALUES (?, ?, ?)", this.fkid, item.getID(), amount);
            }

            return null;
        }, this.fkid, item.getID());
    }

    public void setItem(@NotNull Item item, long amount)
    {
        if (item instanceof ItemCurrency curr)
        {
            switch (curr.getCurrency())
            {
                case COINS -> this.setCoins(amount);
                case DUST -> this.setDust(amount);
                case KEKS -> this.setKeks(amount);
                case KEYS -> this.setKeys(amount);
                case TOKENS -> this.setKekTokens(amount);
                case XP -> this.setXP(amount);
            }

            return;
        }

        this.db.exec("SELECT it_amount FROM inventory WHERE fk_us_id=? AND fk_il_id=?", stat ->
        {
            var res = stat.executeQuery();

            if (res.next())
            {
                this.db.simpleUpdate("UPDATE inventory SET it_amount=? WHERE fk_us_id=? AND fk_il_id=?", amount, this.fkid, item.getID());
            }
            else
            {
                this.db.simpleUpdate("INSERT INTO inventory (fk_us_id, fk_il_id, it_amount) VALUES (?, ?, ?)", this.fkid, item.getID(), amount);
            }

            return null;
        }, this.fkid, item.getID());
    }

    public void setCoins(long coins)
    {
        this.db.simpleUpdate("UPDATE users SET us_coins=? WHERE us_id=?", coins, this.fkid);
    }

    public void setDust(long dust)
    {
        this.db.simpleUpdate("UPDATE users SET us_dust=? WHERE us_id=?", dust, this.fkid);
    }

    public void setKeks(long keks)
    {
        this.db.simpleUpdate("UPDATE users SET us_keks=? WHERE us_id=?", keks, this.fkid);
    }

    public void setKekTokens(long tokens)
    {
        this.db.simpleUpdate("UPDATE users SET us_tokens=? WHERE us_id=?", tokens, this.fkid);
    }

    public void setKeys(long keys)
    {
        this.db.simpleUpdate("UPDATE users SET us_keys=? WHERE us_id=?", keys, this.fkid);
    }

    public void setLevel(int level)
    {
        this.db.simpleUpdate("UPDATE users SET us_level=? WHERE us_id=?", level, this.fkid);
    }

    public void setXP(long xp)
    {
        this.db.simpleUpdate("UPDATE users SET us_xp=? WHERE us_id=?", xp, this.fkid);
    }

    ///
    /// ITEM ADDS
    ///

    public void addCard(@NotNull Card item)
    {
        this.addCard(item, 1);
    }

    public void addCard(@NotNull Card item, long amount)
    {
        this.db.exec("SELECT cr_amount FROM cards WHERE fk_us_id=? AND fk_il_id=?", stat ->
        {
            var res = stat.executeQuery();

            if (res.next())
            {
                this.db.simpleUpdate("UPDATE cards SET cr_amount=cr_amount+? WHERE fk_us_id=? AND fk_il_id=?", amount, this.fkid, item.getID());
            }
            else
            {
                this.db.simpleUpdate("INSERT INTO cards (fk_us_id, fk_il_id, cr_amount) VALUES (?, ?, ?)", this.fkid, item.getID(), amount);
            }

            return null;
        }, this.fkid, item.getID());
    }

    public void addCardXP(@NotNull Card card, long xp)
    {
        var preAddXP = this.getCardXP(card);
        var currentXP = preAddXP + xp;
        var lvl = this.getCardLevel(card);

        var maxTier = EnumCardModifier.getMaxLevel();
        var maxLevel = maxTier.getLevel();

        var newLevel = lvl;

        var tier = EnumCardModifier.getByLevel(newLevel);
        assert tier != null;
        var xpSum = tier.getXPForLevelUp();
        var consumedXP = 0L;

        while (xpSum <= currentXP)
        {
            consumedXP = xpSum;

            if (++newLevel >= maxLevel)
            {
                newLevel = maxLevel;
                break;
            }

            tier = EnumCardModifier.getByLevel(newLevel);
            assert tier != null;
            xpSum += tier.getXPForLevelUp();
        }

        var newXP = currentXP - consumedXP;

        this.db.simpleUpdate("UPDATE cards SET cr_xp=?, cr_level=? WHERE fk_us_id=? AND fk_il_id=?", newXP, newLevel, this.fkid, card.getID());
    }

    public void addItem(@NotNull Item item)
    {
        this.addItem(item, 1);
    }

    public void addItem(@NotNull Item item, long amount)
    {
        if (item instanceof ItemCurrency curr)
        {
            switch (curr.getCurrency()) {
                case COINS -> this.addCoins(amount);
                case DUST -> this.addDust(amount);
                case KEKS -> this.addKeks(amount);
                case KEYS -> this.addKeys(amount);
                case TOKENS -> this.addKekTokens(amount);
                case XP -> this.addXP(amount);
            }

            return;
        }

        this.db.exec("SELECT it_amount FROM inventory WHERE fk_us_id=? AND fk_il_id=?", stat ->
        {
            var res = stat.executeQuery();

            if (res.next())
            {
                db.simpleUpdate("UPDATE inventory SET it_amount=it_amount+? WHERE fk_us_id=? AND fk_il_id=?", amount, this.fkid, item.getID());
            }
            else
            {
                db.simpleUpdate("INSERT INTO inventory (fk_us_id, fk_il_id, it_amount)  VALUES (?, ?, ?)", this.fkid, item.getID(), amount);
            }

            return null;
        }, this.fkid, item.getID());
    }

    public void addCoins(long coins)
    {
        this.db.simpleUpdate("UPDATE users SET us_coins=us_coins+? WHERE us_id=?", coins, this.fkid);
    }

    public void addDust(long dust)
    {
        this.db.simpleUpdate("UPDATE users SET us_dust=us_dust+? WHERE us_id=?", dust, this.fkid);
    }

    public void addKeks(long keks)
    {
        this.db.simpleUpdate("UPDATE users SET us_keks=us_keks+? WHERE us_id=?", keks, this.fkid);
    }

    public void addKekTokens(long tokens)
    {
        this.db.simpleUpdate("UPDATE users SET us_tokens=us_tokens+? WHERE us_id=?", tokens, this.fkid);
    }

    public void addKeys(long keys)
    {
        this.db.simpleUpdate("UPDATE users SET us_keys=us_keys+? WHERE us_id=?", keys, this.fkid);
    }

    public void addLevel(int level)
    {
        this.db.simpleUpdate("UPDATE users SET us_level=us_level+? WHERE us_id=?", level, this.fkid);
    }

    public void addXP(long xp)
    {
        this.db.simpleUpdate("UPDATE users SET us_xp=us_xp+? WHERE us_id=?", xp, this.fkid);

        // TODO!
        throw new UnsupportedOperationException();
    }

    //
    // Properties
    //

    public PlayerProperties getProperties()
    {
        return this.properties;
    }
}
