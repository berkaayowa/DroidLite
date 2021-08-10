package com.payment24.library.Sqlite;

import com.payment24.library.Sqlite.Enum.Query;

import java.util.ArrayList;
import java.util.HashMap;

public class Table {

    public Table() {
        this.Columns = new ArrayList<>();
    }
    public String Name;
    public ArrayList<TableColumn> Columns;
    public HashMap<Query,TableQuery> Queries;
   // public map<String, String
}
