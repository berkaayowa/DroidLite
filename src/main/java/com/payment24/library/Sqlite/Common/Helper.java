package com.payment24.library.Sqlite.Common;

import com.payment24.library.Sqlite.Attributes.Column;
import com.payment24.library.Sqlite.Entity;
import com.payment24.library.Sqlite.Enum.Constraint;
import com.payment24.library.Sqlite.Enum.Query;
import com.payment24.library.Sqlite.Table;
import com.payment24.library.Sqlite.TableColumn;
import com.payment24.library.Sqlite.TableQuery;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class Helper {

    public static Table GetEntityTable(Entity entity) {

        Table table = new Table();

        if (entity.getClass().isAnnotationPresent(com.payment24.library.Sqlite.Attributes.Table.class)) {

            com.payment24.library.Sqlite.Attributes.Table attrTable = entity.getClass().getAnnotation(com.payment24.library.Sqlite.Attributes.Table.class);
            table.Name = attrTable.Name();

            Field[] fields = entity.getClass().getFields();

            for (int i = 0; i < fields.length; i++) {

                if(!fields[i].isAnnotationPresent(Column.class))
                    continue;

                try
                {
                    TableColumn tableColumn = new TableColumn();
                    Column col = fields[i].getAnnotation(Column.class);

                    tableColumn.Name = col.Name();
                    tableColumn.Constraint = col.Constraint();
                    tableColumn.Value = fields[i].get(entity);
                    tableColumn.Type = fields[i].getType().getName().toLowerCase();

                    if(tableColumn.Name.isEmpty())
                        tableColumn.Name = fields[i].getName();

                    table.Columns.add(tableColumn);

                }catch (Exception ex) {
                    System.out.println("error: " + ex.getMessage());
                }

            }
        }

        table.Queries = Helper.GenerateQuery(table);

        return table;
    }

    public static ArrayList<TableQuery> GenerateQuery(Table table) {

        ArrayList<TableQuery> queries = new ArrayList<>();

        String selectQry = "SELECT {{columnQry}} FROM " + table.Name;
        String createQry = "CREATE TABLE IF NOT EXISTS `"+table.Name+"` ({{columns}})";
        String createColumnQry = "";
        String insertQry = "INSERT INTO " + table.Name + " ({{columnQry}}) VALUES ({{columnValue}})";
        String columnQry = "";
        String columnValue = "";

        for(int i = 0; i < table.Columns.size(); i++) {

            createColumnQry = createColumnQry + table.Columns.get(i).Name;

            columnQry = columnQry + table.Columns.get(i).Name;
            columnValue = columnValue + table.Columns.get(i).Value;
            createColumnQry = createColumnQry + " " + GetSqliteColumnType(table.Columns.get(i));

            if(i != table.Columns.size() - 1) {
                createColumnQry = createColumnQry + ", ";
                columnQry = columnQry + ", ";
                columnValue = columnValue + ", ";
            }

        }

        insertQry = insertQry.replace("{{columnQry}}", columnQry);
        insertQry = insertQry.replace("{{columnValue}}", columnValue);
        selectQry = selectQry.replace("{{columnQry}}", columnQry);
        createQry = createQry.replace("{{columns}}", createColumnQry);

        queries.add(new TableQuery(Query.Update, insertQry));
        queries.add(new TableQuery(Query.Select, selectQry));
        queries.add(new TableQuery(Query.CreateTable, createQry));

        return queries;
    }

    private static String GetSqliteColumnType(TableColumn tableColumn)
    {
        String type = "";

        switch (tableColumn.Type.toLowerCase()) {
            case "java.lang.string":
            case "string":
                type = "TEXT";
                break;
            case "int":
            case "integer":
                type = "INTEGER";
                break;
            case "float":
                type = "REAL";
                break;
            case "double":
                type = "REAL";
                break;
            case "java.util.date":
            case "date":
                type = "NUMERIC";
                break;
            default:
                type = tableColumn.Type.toLowerCase();
                break;

        }

        for(int i = 0; i < tableColumn.Constraint.length; i++)
        {
            if(tableColumn.Constraint[i] == Constraint.PrimaryKey)
                type = type + " NOT NULL PRIMARY KEY AUTOINCREMENT";
            if(tableColumn.Constraint[i] == Constraint.Unique)
                type = type + " UNIQUE";
            if(tableColumn.Constraint[i] == Constraint.Null)
                type = type + " NULL";
            if(tableColumn.Constraint[i] == Constraint.NotNull)
                type = type + " NOT NULL";
        }

        return  type;
    }
}
