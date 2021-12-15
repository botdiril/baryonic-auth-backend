package com.botdiril.framework.sql.connection;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.botdiril.framework.sql.DBException;
import com.botdiril.framework.sql.DBResource;
import com.botdiril.framework.sql.ParamNull;

public abstract class AbstractDBConnection implements DBResource
{
    protected final Connection connection;
    protected final boolean autocommit;
    protected boolean readOnly;

    protected AbstractDBConnection(Connection connection, boolean autocommit, boolean readOnly)
    {
        this.connection = connection;
        this.autocommit = autocommit;
        this.readOnly = readOnly;
    }

    protected void setParams(PreparedStatement statement, Object... params) throws Exception
    {
        for (int i = 0; i < params.length; i++)
        {
            var param = params[i];

            if (param == null)
            {
                throw new IllegalStateException("Parameter can't be raw null!");
            }

            int paramIdx = i + 1;

            if (param instanceof ParamNull paramNull)
            {
                statement.setNull(paramIdx, paramNull.type().getJdbcType());
            }
            else if (param instanceof Integer intVal)
            {
                statement.setInt(paramIdx, intVal);
            }
            else if (param instanceof Long longVal)
            {
                statement.setLong(paramIdx, longVal);
            }
            else if (param instanceof String str)
            {
                statement.setString(paramIdx, str);
            }
            else if (param instanceof byte[] bytes)
            {
                ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
                statement.setBinaryStream(paramIdx, stream);
            }
            else
            {
                throw new UnsupportedOperationException("Unsupported DB data type.");
            }
        }
    }

    @Override
    public void close()
    {
        try
        {
            this.rollback();

            this.connection.close();
        }
        catch (SQLException e)
        {
            throw new DBException(e);
        }
    }

    public void commit()
    {
        if (this.autocommit)
            return;

        try
        {
            this.connection.commit();
        }
        catch (SQLException e)
        {
            throw new DBException(e);
        }
    }

    public void rollback()
    {
        if (this.autocommit)
            return;

        try
        {
            this.connection.rollback();
        }
        catch (SQLException e)
        {
            throw new DBException(e);
        }
    }

    public boolean isAutoCommiting()
    {
        return autocommit;
    }

    public boolean isReadOnly()
    {
        return this.readOnly;
    }
}
