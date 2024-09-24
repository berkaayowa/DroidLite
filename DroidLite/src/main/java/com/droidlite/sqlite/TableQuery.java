package com.droidlite.sqlite;

import com.droidlite.sqlite.enums.Query;

import java.util.ArrayList;

public class TableQuery {


    public TableQuery(Query queryType, String query) {
        QueryType = queryType;
        Query = query;
    }

    public com.droidlite.sqlite.enums.Query QueryType;
    public String Query;
    public boolean Success;
    public ArrayList<TableQuery> TableQueries;


}
