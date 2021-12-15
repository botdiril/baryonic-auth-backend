package com.botdiril.framework.sql.connection;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mysql.cj.jdbc.Driver;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

import com.botdiril.BotMain;
import com.botdiril.framework.sql.DBException;
import com.botdiril.framework.sql.SqlFoundation;
import com.botdiril.framework.sql.connection.ReadDBConnection;
import com.botdiril.framework.sql.connection.WriteDBConnection;

public class SqlConnectionManager
{
    private static final int IDLE_CONNECTION_TEST_PERIOD = 60;
    private static final int MAX_CONNECTION_AGE = 60 * 60;

    private final ComboPooledDataSource dataSource;

    public SqlConnectionManager() throws PropertyVetoException
    {
        this.dataSource = new ComboPooledDataSource();
        this.dataSource.setDriverClass(Driver.class.getName());
        this.dataSource.setIdleConnectionTestPeriod(IDLE_CONNECTION_TEST_PERIOD);
        this.dataSource.setTestConnectionOnCheckin(true);
        this.dataSource.setMaxConnectionAge(MAX_CONNECTION_AGE);
        this.dataSource.setJdbcUrl("jdbc:mysql:// " + BotMain.config.getSqlHost() + "/" + SqlFoundation.SCHEMA + "?useUnicode=true&autoReconnect=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC");
        this.dataSource.setUser(BotMain.config.getSqlUser());
        this.dataSource.setPassword(BotMain.config.getSqlPass());
        this.dataSource.setAutoCommitOnClose(false);
    }

    public WriteDBConnection get()
    {
        return get(false);
    }

    public WriteDBConnection get(boolean autocommit)
    {
        var c = get(autocommit, false);
        return new WriteDBConnection(c, autocommit);
    }

    public ReadDBConnection getReadOnly()
    {
        var c = get(true, true);
        return new ReadDBConnection(c, true);
    }

    private Connection get(boolean autocommit, boolean readOnly)
    {
        try
        {
            var c = this.dataSource.getConnection();
            c.setAutoCommit(autocommit);
            c.setReadOnly(readOnly);
            return c;
        }
        catch (SQLException e)
        {
            throw new DBException(e);
        }
    }
}