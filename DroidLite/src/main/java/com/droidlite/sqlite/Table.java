package com.droidlite.sqlite;

import com.droidlite.sqlite.enums.Query;

import java.util.ArrayList;
import java.util.HashMap;

public class Table {

    public Table() {
        this.tableColumns = new ArrayList<>();
    }
    public String Name;
    public ArrayList<TableColumn> tableColumns;
    public HashMap<Query,TableQuery> queries;
   // public map<String, String
}
