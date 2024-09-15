package com.droidlite.sqlite;

import com.droidlite.sqlite.enums.Query;

public class TableQuery {


    public TableQuery(Query query, String statement) {
        Query = query;
        Statement = statement;
    }

    public com.droidlite.sqlite.enums.Query Query;
    public String Statement;
    public boolean Success;


}
