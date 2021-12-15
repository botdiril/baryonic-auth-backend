package com.botdiril;

import java.util.Locale;

import com.botdiril.framework.sql.connection.SqlConnectionManager;
import com.botdiril.framework.sql.SqlFoundation;
import com.botdiril.internal.BotdirilConfig;
import com.botdiril.gamedata.player.inventory.InventoryTables;
import com.botdiril.gamedata.ItemLookup;
import com.botdiril.gamedata.achievement.Achievements;
import com.botdiril.gamedata.card.Cards;
import com.botdiril.gamedata.items.Items;
import com.botdiril.gamedata.metrics.UserMetrics;
import com.botdiril.gamedata.player.properties.PlayerProperties;
import com.botdiril.gamedata.timers.EnumTimer;
import com.botdiril.util.BotdirilLog;

public class BotMain
{
    public static BotdirilConfig config;
    public static Botdiril botdiril;

    public static SqlConnectionManager SQL_MANAGER;

    public static void main(String[] args)
    {
        BotdirilLog.init();

        BotdirilLog.logger.info("=====================================");
        BotdirilLog.logger.info("####     BOTDIRIL MESON 500      ####");
        BotdirilLog.logger.info("=====================================");

        Locale.setDefault(Locale.US);


        try
        {
            config = BotdirilConfig.load();
            botdiril = new Botdiril(config);

            SqlFoundation.build();

            try (var db = BotMain.SQL_MANAGER.get())
            {
                ItemLookup.prepare(db);

                InventoryTables.initTables(db);

                UserMetrics.initTable(db);

                PlayerProperties.init(db);

                Items.load();

                Cards.load();

                Class.forName(EnumTimer.class.getName());

                Class.forName(Achievements.class.getName());

                ItemLookup.save(db);

                db.commit();
            }

            botdiril.start();
        }
        catch (MajorFailureException e)
        {
            BotdirilLog.logger.fatal("An unrecoverable failure has occurred while initializing the bot.", e);
        }
        catch (Exception e)
        {
            BotdirilLog.logger.fatal("A general unrecoverable failure has occurred while initializing the bot.", e);
        }
    }
}
