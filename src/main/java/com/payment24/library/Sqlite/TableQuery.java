package com.payment24.library.Sqlite;
import Payment24.Sqlite.Enum.Query;

public class TableQuery {

    public TableQuery(Payment24.Sqlite.Enum.Query query, String statement) {
        Query = query;
        Statement = statement;
    }

    public Query Query;
    public String Statement;
    public boolean Success;
}
