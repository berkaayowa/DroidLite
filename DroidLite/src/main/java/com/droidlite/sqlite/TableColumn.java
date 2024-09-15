package com.droidlite.sqlite;

public class TableColumn {
    public String Name;
    public String Type;
    public Object Value;

    public com.droidlite.sqlite.enums.Constraint [] Constraints;

    public TableColumn() {
    }

    public TableColumn(String name, Object value) {
        Value = value;
        Name = name;
    }

    public Object getDefaultValue() {

        String typeCode = Value.getClass().getName().toLowerCase();

        switch (typeCode) {
            case "double":
            case "int":
            case "java.lang.int":
            case "java.lang.integer":
            case "integer":
                return 0;
            case "java.lang.string":
            case "string":
                return "";
            default:
                return null;

        }

    }
}
