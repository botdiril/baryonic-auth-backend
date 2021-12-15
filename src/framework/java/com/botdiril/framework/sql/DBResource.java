package com.botdiril.framework.sql;

public interface DBResource extends AutoCloseable
{
    @Override
    void close() throws DBException;
}
