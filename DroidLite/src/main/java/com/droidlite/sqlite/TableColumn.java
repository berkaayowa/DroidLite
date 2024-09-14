package com.droidlite.sqlite;

public class TableColumn {
    public String Name;
    public String Type;
    public Object Value;
    public com.droidlite.sqlite.enums.Constraint [] Constraint;

    public TableColumn() {
    }

    public TableColumn(String name, Object value) {
        Value = value;
        Name = name;
    }
}
