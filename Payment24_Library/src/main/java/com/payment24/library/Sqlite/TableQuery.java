package com.payment24.library.Sqlite;

import com.payment24.library.Sqlite.Enum.Query;

public class TableQuery {

    public TableQuery(Query query, String statement) {
        Query = query;
        Statement = statement;
    }

    public com.payment24.library.Sqlite.Enum.Query Query;
    public String Statement;
    public boolean Success;
}
