package com.botdiril.framework.sql.connection;

import com.mchange.v2.c3p0.impl.NewProxyPreparedStatement;
import com.mysql.cj.jdbc.ClientPreparedStatement;
import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Optional;

import com.botdiril.framework.sql.DBException;
import com.botdiril.framework.sql.DBResource;
import com.botdiril.framework.sql.ParamNull;
import com.botdiril.framework.sql.SqlStatementCallback;
import com.botdiril.util.BotdirilLog;

public final class WriteDBConnection extends ReadDBConnection
{
    WriteDBConnection(Connection connection, boolean autocommit)
    {
        super(connection, autocommit, false);
    }

    public int simpleUpdate(@Language("MySQL") String statement, Object... params)
    {
        try
        {
            try (var stat = connection.prepareStatement(statement))
            {
                this.setParams(stat, params);

                BotdirilLog.logger.debug("Executing SQL: " + ((ClientPreparedStatement) ((NewProxyPreparedStatement) stat).unwrap(ClientPreparedStatement.class)).asSql());

                return stat.executeUpdate();
            }
        }
        catch (Exception e)
        {
            throw new DBException(e);
        }
    }

    public boolean simpleExecute(@Language("MySQL") String statement, Object... params)
    {
        try
        {
            try (var stat = connection.prepareStatement(statement))
            {
                this.setParams(stat, params);

                BotdirilLog.logger.debug("Executing SQL: " + ((ClientPreparedStatement) ((NewProxyPreparedStatement) stat).unwrap(ClientPreparedStatement.class)).asSql());

                return stat.execute();
            }
        }
        catch (Exception e)
        {
            throw new DBException(e);
        }
    }

    public <R> R exec(@Language("MySQL") String statement, SqlStatementCallback<PreparedStatement, R> callback, Object... params)
    {
        try
        {
            try (var stat = connection.prepareStatement(statement))
            {
                this.setParams(stat, params);

                BotdirilLog.logger.debug("Executing SQL: " + ((ClientPreparedStatement) ((NewProxyPreparedStatement) stat).unwrap(ClientPreparedStatement.class)).asSql());

                return callback.exec(stat);
            }
        }
        catch (Exception e)
        {
            throw new DBException(e);
        }
    }

    public <R> R exec(@Language("MySQL") String statement, @MagicConstant(intValues = {
        Statement.RETURN_GENERATED_KEYS,
        Statement.NO_GENERATED_KEYS
    }) int generateKeys, SqlStatementCallback<PreparedStatement, R> callback, Object... params)
    {
        try
        {
            try (var stat = connection.prepareStatement(statement, generateKeys))
            {
                this.setParams(stat, params);

                BotdirilLog.logger.debug("Executing SQL: " + ((ClientPreparedStatement) ((NewProxyPreparedStatement) stat).unwrap(ClientPreparedStatement.class)).asSql());

                return callback.exec(stat);
            }
        }
        catch (Exception e)
        {
            throw new DBException(e);
        }
    }
}
