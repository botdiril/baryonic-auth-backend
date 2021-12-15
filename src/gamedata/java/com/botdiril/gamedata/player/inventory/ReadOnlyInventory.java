package com.botdiril.gamedata.player.inventory;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import com.botdiril.api.data.response.InventoryOverview;
import com.botdiril.framework.sql.connection.ReadDBConnection;
import com.botdiril.gamedata.achievement.Achievement;
import com.botdiril.gamedata.card.Card;
import com.botdiril.gamedata.item.Item;
import com.botdiril.gamedata.item.ItemCurrency;
import com.botdiril.gamedata.player.properties.ReadOnlyPlayerProperties;
import com.botdiril.gamedata.timers.EnumTimer;
import com.botdiril.gamedata.timers.TimerUtil;

public class ReadOnlyInventory
{
    protected transient final ReadDBConnection db;
    protected transient final ReadOnlyPlayerProperties properties;

    protected final int fkid;

    public ReadOnlyInventory(ReadDBConnection db, int fkid)
    {
        this.db = db;
        this.fkid = fkid;
        this.properties = new ReadOnlyPlayerProperties(this.db, this.fkid);
    }

    public int getFID()
    {
        return this.fkid;
    }

    public InventoryOverview getOverview()
    {
        return this.db.query("SELECT * FROM users WHERE us_id=?", rs ->
        {
            rs.next();

            return new InventoryOverview(
                rs.getInt("us_level"),
                rs.getLong("us_xp"),
                rs.getLong("us_coins"),
                rs.getLong("us_keks"),
                rs.getLong("us_dust"),
                rs.getLong("us_keys"),
                rs.getLong("us_tokens"),
                this.getCards());
        }, this.fkid);
    }

    ///
    /// ACHIEVEMENTS
    ///

    public boolean hasAchievement(@NotNull Achievement achievement)
    {
        return this.db.hasRow("SELECT * FROM achievements WHERE fk_us_id=? AND fk_il_id=?", this.fkid, achievement.getID());
    }

    ///
    /// TIMERS
    ///

    public long getTimer(@NotNull EnumTimer timer)
    {
        return this.db.getValueOr("SELECT tm_time FROM timers WHERE fk_us_id=? AND fk_il_id=?",
            "tm_time", Long.class, TimerUtil.TIMER_OFF_COOLDOWN, this.fkid, timer.getID());
    }

    ///
    /// ITEM GETS
    ///

    public long howManyOf(@NotNull Card card)
    {
        return this.db.getValueOr("SELECT cr_amount FROM cards WHERE fk_us_id=? AND fk_il_id=?",
            "cr_amount", Long.class, 0L, this.fkid, card.getID());
    }

    public long howManyOf(@NotNull Item item)
    {
        if (item instanceof ItemCurrency curr)
        {
            return switch (curr.getCurrency()) {
                case COINS -> this.getCoins();
                case DUST -> this.getDust();
                case KEKS -> this.getKeks();
                case KEYS -> this.getKeys();
                case TOKENS -> this.getKekTokens();
                case XP -> this.getXP();
            };
        }

        return this.db.getValueOr("SELECT it_amount FROM inventory WHERE fk_us_id=? AND fk_il_id=?",
            "it_amount", Long.class, 0L, this.fkid, item.getID());
    }

    public long getCards()
    {
        return this.db.getValueOr("SELECT SUM(cr_amount) as cardcount FROM cards WHERE fk_us_id=?",
            "cardcount", Long.class, 0L, this.fkid);
    }

    public int getCardLevel(Card card)
    {
        return this.db.getValueOr("SELECT cr_level FROM cards WHERE fk_us_id=? AND fk_il_id=?",
            "cr_level", Integer.class, 0, this.fkid, card.getID());
    }

    public long getCardXP(Card card)
    {
        return this.db.getValueOr("SELECT cr_xp FROM cards WHERE fk_us_id=? AND fk_il_id=?",
            "cr_xp", Long.class, 0L, this.fkid, card.getID());
    }

    public long getCoins()
    {
        return this.db.getValueOr("SELECT us_coins FROM users WHERE us_id=?",
            "us_coins", Long.class, 0L, this.fkid);
    }

    public long getDust()
    {
        return this.db.getValueOr("SELECT us_dust FROM users WHERE us_id=?",
            "us_dust", Long.class, 0L, this.fkid);
    }

    public long getKeks()
    {
        return this.db.getValueOr("SELECT us_keks FROM users WHERE us_id=?",
            "us_keks", Long.class, 0L, this.fkid);
    }

    public long getKekTokens()
    {
        return this.db.getValueOr("SELECT us_tokens FROM users WHERE us_id=?",
            "us_tokens", Long.class, 0L, this.fkid);
    }

    public long getKeys()
    {
        return this.db.getValueOr("SELECT us_keys FROM users WHERE us_id=?",
            "us_keys", Long.class, 0L, this.fkid);
    }

    public long getXP()
    {
        return this.db.getValueOr("SELECT us_xp FROM users WHERE us_id=?",
            "us_xp", Long.class, 0L, this.fkid);
    }

    public int getLevel()
    {
        return this.db.getValueOr("SELECT us_level FROM users WHERE us_id=?",
            "us_level", Integer.class, 0, this.fkid);
    }

    //
    // PROPERTIES
    //

    public ReadOnlyPlayerProperties getProperties()
    {
        return this.properties;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof ReadOnlyInventory inventory)
            return this.fkid == inventory.fkid;

        return false;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.fkid);
    }
}
