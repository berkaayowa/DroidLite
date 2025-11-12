package com.droidlite.sqlite;

import com.droidlite.sqlite.attributes.AlterColumn;
import com.droidlite.sqlite.enums.ColumnType;
import com.droidlite.sqlite.enums.ComparisonOperator;

public class TableColumn {
    public String Name;
    public String Type;
    public Object Value;
    public AlterColumn Alter;
    public ComparisonOperator Operator;

    public com.droidlite.sqlite.enums.Constraint [] Constraints;

    public TableColumn() {
    }

    public TableColumn(String name, Object value) {
        Value = value;
        Name = name;
        Operator = ComparisonOperator.Default;
    }

    public TableColumn(String name, ComparisonOperator operator, Object value) {
        Value = value;
        Name = name;
        Operator = operator;
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
            case "boolean":
                return false;
            default:
                return null;

        }

    }

    public static ColumnType getColumnType(Object value, String typeCode) {

        if(value == null && typeCode == null)
            return ColumnType.Unknown;

        if(value != null && typeCode == null)
            typeCode = value.getClass().getName().toLowerCase();

        switch (typeCode) {
            case "float":
            case "java.lang.float":
                return ColumnType.FloatType;
            case "double":
            case "java.lang.double":
                return ColumnType.DoubleType;
            case "int":
            case "java.lang.int":
            case "java.lang.integer":
            case "integer":
                return ColumnType.IntegerType;
            case "java.lang.string":
            case "string":
                return ColumnType.TextType;
            case "java.util.date":
            case "date":
                return ColumnType.DateType;
            case "boolean":
            case "java.lang.boolean":
                return ColumnType.Boolean;
            default:
                return ColumnType.Unknown;

        }

    }

    public static ColumnType getColumnTypeByValue(Object value)
    {
        return getColumnType(value, null);
    }

    public static ColumnType getColumnTypeCode(String typCode)
    {
        return getColumnType(null, typCode);
    }

}
