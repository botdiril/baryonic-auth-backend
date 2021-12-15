package com.botdiril.framework.sql.connection;

import com.mchange.v2.c3p0.impl.NewProxyPreparedStatement;
import com.mysql.cj.jdbc.ClientPreparedStatement;
import org.checkerframework.checker.units.qual.K;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.*;
import java.util.*;

import com.botdiril.framework.sql.DBException;
import com.botdiril.framework.sql.SqlStatementCallback;
import com.botdiril.util.BotdirilLog;

public class ReadDBConnection extends AbstractDBConnection
{
    ReadDBConnection(Connection connection, boolean autocommit)
    {
        super(connection, autocommit, true);
    }

    protected ReadDBConnection(Connection connection, boolean autocommit, boolean readOnly)
    {
        super(connection, autocommit, readOnly);
    }


    private Optional<byte[]> retrieveBlob(ResultSet resultSet, String columnName) throws SQLException, IOException
    {
        var blob = resultSet.getBlob(columnName);

        if (resultSet.wasNull())
        {

            blob.free();
            return Optional.empty();
        }

        try (var is = blob.getBinaryStream())
        {
            var bytes = new byte[(int) blob.length()];

            if (is.read(bytes) != bytes.length)
                throw new DBException("Blob read size mismatch.");

            return Optional.of(bytes);
        }
        finally
        {
            blob.free();
        }
    }

    protected  <R> Optional<R> retrieveValue(ResultSet resultSet, String columnName, Class<R> valueType) throws SQLException, IOException
    {
        if (valueType == byte[].class)
        {
            return this.retrieveBlob(resultSet, columnName).map(valueType::cast);
        }

        Object val;

        if (valueType == Integer.class)
        {
            val = resultSet.getInt(columnName);
        }
        else if (valueType == Long.class)
        {
            val = resultSet.getLong(columnName);
        }
        else if (valueType == Float.class)
        {
            val = resultSet.getFloat(columnName);
        }
        else if (valueType == Double.class)
        {
            val = resultSet.getDouble(columnName);
        }
        else if (valueType == String.class)
        {
            val = resultSet.getString(columnName);
        }
        else
        {
            throw new UnsupportedOperationException(String.format("Unsupported type %s.", valueType.getName()));
        }

        return resultSet.wasNull() ? Optional.empty() : Optional.of(valueType.cast(val));
    }

    public <R> @NotNull Optional<R> getValue(@Language("MySQL") String statement, String columnName, Class<R> valueType, Object... params)
    {
        try
        {
            try (var stat = connection.prepareStatement(statement))
            {
                this.setParams(stat, params);

                BotdirilLog.logger.debug("Executing SQL: " + ((ClientPreparedStatement) ((NewProxyPreparedStatement) stat).unwrap(ClientPreparedStatement.class)).asSql());

                try (var rs = stat.executeQuery())
                {
                    if (!rs.next())
                        return Optional.empty();

                    return this.retrieveValue(rs, columnName, valueType);
                }
            }
        }
        catch (Exception e)
        {
            throw new DBException(e);
        }
    }

    public <R> R getValueOr(@Language("MySQL") String statement, String columnName, Class<R> valueType, R fallbackValue, Object... params)
    {
        try
        {
            try (var stat = connection.prepareStatement(statement))
            {
                this.setParams(stat, params);

                BotdirilLog.logger.debug("Executing SQL: " + ((ClientPreparedStatement) ((NewProxyPreparedStatement) stat).unwrap(ClientPreparedStatement.class)).asSql());

                try (var rs = stat.executeQuery())
                {
                    if (!rs.next())
                        return fallbackValue;

                    return this.retrieveValue(rs, columnName, valueType).orElse(fallbackValue);
                }
            }
        }
        catch (Exception e)
        {
            throw new DBException(e);
        }
    }

    public <R> List<R> getList(@Language("MySQL") String statement, String columnName, Class<R> valueType, Object... params)
    {
        try
        {
            try (var stat = connection.prepareStatement(statement))
            {
                this.setParams(stat, params);

                BotdirilLog.logger.debug("Executing SQL: " + ((ClientPreparedStatement) ((NewProxyPreparedStatement) stat).unwrap(ClientPreparedStatement.class)).asSql());

                try (var rs = stat.executeQuery())
                {
                    var resultList = new ArrayList<R>();
                    Optional<R> val;

                    while (rs.next())
                    {
                        val = this.retrieveValue(rs, columnName, valueType);

                        if (val.isEmpty())
                            continue;

                        resultList.add(val.get());
                    }

                    return resultList;
                }
            }
        }
        catch (Exception e)
        {
            throw new DBException(e);
        }
    }

    public <KT, VT> Map<KT, VT> getMap(@Language("MySQL") String statement, String keyColumn, Class<KT> keyType, String valueColumn, Class<VT> valueType, Object... params)
    {
        try
        {
            try (var stat = connection.prepareStatement(statement))
            {
                this.setParams(stat, params);

                BotdirilLog.logger.debug("Executing SQL: " + ((ClientPreparedStatement) ((NewProxyPreparedStatement) stat).unwrap(ClientPreparedStatement.class)).asSql());

                try (var rs = stat.executeQuery())
                {
                    var resultList = new HashMap<KT, VT>();
                    Optional<KT> key;
                    Optional<VT> val;

                    while (rs.next())
                    {
                        key = this.retrieveValue(rs, keyColumn, keyType);
                        val = this.retrieveValue(rs, valueColumn, valueType);

                        if (key.isEmpty() || val.isEmpty())
                            continue;

                        resultList.put(key.get(), val.get());
                    }

                    return resultList;
                }
            }
        }
        catch (Exception e)
        {
            throw new DBException(e);
        }
    }

    public <R> R query(@Language("MySQL") String statement, SqlStatementCallback<ResultSet, R> callback, Object... params)
    {
        try
        {
            try (var stat = connection.prepareStatement(statement))
            {
                this.setParams(stat, params);

                BotdirilLog.logger.debug("Executing SQL: " + ((ClientPreparedStatement) ((NewProxyPreparedStatement) stat).unwrap(ClientPreparedStatement.class)).asSql());

                try (var rs = stat.executeQuery())
                {
                    return callback.exec(rs);
                }
            }
        }
        catch (Exception e)
        {
            throw new DBException(e);
        }
    }

    public boolean hasRow(@Language("MySQL") String statement, Object... params)
    {
        return this.query(statement, ResultSet::next, params);
    }
}
