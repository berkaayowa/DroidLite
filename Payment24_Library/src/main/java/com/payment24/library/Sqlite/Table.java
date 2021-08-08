package com.payment24.library.Sqlite;

import java.util.ArrayList;

public class Table {

    public Table() {
        this.Columns = new ArrayList<>();
        this.Queries = new ArrayList<>();
    }
    public String Name;
    public ArrayList<TableColumn> Columns;
    public ArrayList<TableQuery> Queries;
   // public map<String, String
}
