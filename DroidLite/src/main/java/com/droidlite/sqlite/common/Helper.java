package com.droidlite.sqlite.common;

import android.os.Build;

import com.droidlite.sqlite.Entity;
import com.droidlite.sqlite.Table;
import com.droidlite.sqlite.TableColumn;
import com.droidlite.sqlite.TableQuery;
import com.droidlite.sqlite.attributes.Column;
import com.droidlite.sqlite.enums.Constraint;
import com.droidlite.sqlite.enums.Query;
import com.droidlite.sqlite.interfaces.IEntity;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Helper {

    public static Table convertEntityClassToTable(Class<?> target, Entity entity) {

        if (target.isAnnotationPresent(com.droidlite.sqlite.attributes.Table.class)) {

            Table table = new Table();

            com.droidlite.sqlite.attributes.Table attrTable = target.getAnnotation(com.droidlite.sqlite.attributes.Table.class);
            table.Name = attrTable.Name();

            Field[] fields = target.getFields();

            for (int i = 0; i < fields.length; i++) {

                try
                {
                    //Below check is to make sure we only process data table field
                    if(!fields[i].isAnnotationPresent(com.droidlite.sqlite.attributes.Column.class))
                        continue;

                    TableColumn tableColumn = new TableColumn();
                    com.droidlite.sqlite.attributes.Column col = fields[i].getAnnotation(com.droidlite.sqlite.attributes.Column.class);

                    tableColumn.Name = col.Name();
                    tableColumn.Constraints = col.Constraint();

                    if(entity != null)
                        tableColumn.Value = fields[i].get(entity);

                    tableColumn.Type = fields[i].getType().getName().toLowerCase();

                    if(tableColumn.Name.isEmpty())
                        tableColumn.Name = fields[i].getName();

                    table.Columns.add(tableColumn);

                }catch (Exception ex) {
                    System.out.println("error: " + ex.getMessage());
                }

            }

            return table;
        }

        return null;
    }

    public static TableQuery generateSelectQuery(Table table, TableColumn[] whereCondition) {

        String selectQuery = "SELECT {{columns}} FROM " + table.Name;
        String selectColumns = "";
        String selectWhereCondition = "";

        for(int i = 0; i < table.Columns.size(); i++) {

            selectColumns = selectColumns + table.Columns.get(i).Name;

            if(i != table.Columns.size() - 1)
                selectColumns = selectColumns + ", ";

        }

        if(whereCondition.length > 0) {

            selectWhereCondition = " WHERE ";

            for (int i = 0; i < whereCondition.length; i++) {

                selectWhereCondition = selectWhereCondition + whereCondition[i].Name + " = ? " ;
                //+ getSqliteColumnValue(whereCondition[i].Value)
                if (i != whereCondition.length - 1)
                    selectColumns = selectColumns + " AND ";

            }

        }

        selectQuery = selectQuery.replace("{{columns}}", selectColumns);

        if(!selectWhereCondition.isEmpty())
            selectQuery = selectQuery + " " + selectWhereCondition;

        return new TableQuery(Query.Select, selectQuery);
    }

    public static TableQuery generateCreateTableQuery(Table table) {

        HashMap<Query,TableQuery> queryTableQueryMap = new HashMap<>();
        ArrayList<TableQuery> queries = new ArrayList<>();

        String createTableQuery = "CREATE TABLE IF NOT EXISTS `"+table.Name+"` ({{columns}})";
        String createTableColumns = "";

        for(int i = 0; i < table.Columns.size(); i++) {

            createTableColumns = createTableColumns + " " + table.Columns.get(i).Name + " " + getSqliteColumnType(table.Columns.get(i));

            if(i != table.Columns.size() - 1)
                createTableColumns = createTableColumns  + ", ";

        }

        createTableQuery = createTableQuery.replace("{{columns}}", createTableColumns );

        return new TableQuery(Query.CreateTable, createTableQuery);
    }

    public static TableQuery generateUpdateQuery(Table table) {

        String updateQuery = "UPDATE " + table.Name + " SET {{column}} {{whereCondition}}";
        String updateQueryColumns = "";
        String updateQueryWhereCondition = "";

        TableColumn primaryKey = table.getPrimaryKey();

        for(int i = 0; i < table.Columns.size(); i++) {

            if(primaryKey.Name == table.Columns.get(i).Name) {

                if(updateQueryWhereCondition.isEmpty())
                    updateQueryWhereCondition = " WHERE " + table.Columns.get(i).Name + " = " + getSqliteColumnValue(table.Columns.get(i).Value);
            }
            else {

                updateQueryColumns = updateQueryColumns + table.Columns.get(i).Name + " = " + getSqliteColumnValue(table.Columns.get(i).Value);

                if(i != table.Columns.size() - 1)
                    updateQueryColumns = updateQueryColumns + ", ";

            }

        }

        updateQuery = updateQuery.replace("{{column}}", updateQueryColumns);
        updateQuery = updateQuery.replace("{{whereCondition}}", updateQueryWhereCondition);

        return new TableQuery(Query.Update, updateQuery);
    }

    public static TableQuery generateInsertQuery(Table table) {

        String insertQuery = "INSERT INTO " + table.Name + " ({{columns}}) VALUES ({{columnValue}})";
        String columns = "";
        String columnsValue = "";

        TableColumn primaryKey = table.getPrimaryKey();

        for(int i = 0; i < table.Columns.size(); i++) {

            if(primaryKey.Name != table.Columns.get(i).Name) {

                columns = columns + table.Columns.get(i).Name;
                columnsValue = columnsValue + getSqliteColumnValue(table.Columns.get(i).Value);

                if(i != table.Columns.size() - 1) {
                    columns = columns + ", ";
                    columnsValue = columnsValue + ", ";
                }

            }

        }

        insertQuery = insertQuery.replace("{{columns}}", columns);
        insertQuery = insertQuery.replace("{{columnValue}}", columnsValue);

        return new TableQuery(Query.Insert, insertQuery);
    }

    private static String getSqliteColumnType(TableColumn tableColumn)
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
            case "double":
                type = "REAL";
                break;
            case "java.util.date":
            case "date":
                type = "NUMERIC";
                break;
            default:
                type = "TEXT";
                break;

        }


        for(int i = 0; i < tableColumn.Constraints.length; i++)
        {
            if(tableColumn.Constraints[i] == Constraint.PrimaryKey)
                type = type + " PRIMARY KEY AUTOINCREMENT";
            if(tableColumn.Constraints[i] == Constraint.Unique)
                type = type + " UNIQUE";
            if(tableColumn.Constraints[i] == Constraint.Null)
                type = type + " NULL";
            if(tableColumn.Constraints[i] == Constraint.NotNull)
                type = type + " NOT NULL";
        }

        return  type;
    }

    private static String getSqliteColumnValue(Query query, TableColumn tableColumn)
    {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            if(query == Query.Insert && Arrays.stream(tableColumn.Constraints).anyMatch(x -> x == Constraint.PrimaryKey)) {

                return "NULL";

            }
        }
        return  getSqliteColumnValue(tableColumn.Value);
    }

    private static String getSqliteColumnValue(Object data)
    {
        String value = "";

        if(data != null)
            value = data.toString();

        switch (data.getClass().getName().toLowerCase()) {
            case "java.lang.string":
            case "string":
            case "java.util.date":
            case "date":
                value = "'" + value + "'";
                break;
            default:
                break;

        }

        return  value;
    }

    public static ArrayList<IEntity> mapResultSet(Class<?> entityClass, ArrayList<HashMap<String, String>> resultSet) {

        ArrayList<IEntity> list = new ArrayList<>();

        for (int i = 0; i < resultSet.size(); i++) {

            IEntity iEntity = hashMapToEntity(entityClass, resultSet.get(i));

            if(iEntity != null)
                list.add(iEntity);

        }

        return list;
    }

    public static Entity hashMapToEntity(Class<?> entityClass, HashMap<String, String> hashMap) {

        if(!hashMap.isEmpty()) {

            try {

                Entity entity = (Entity) entityClass.newInstance();

                Field[] fields = entityClass.getFields();

                for (int i = 0; i < fields.length; i++) {

                    try {

                        if (!fields[i].isAnnotationPresent(Column.class))
                            continue;

                        if (hashMap.containsKey(fields[i].getName())){

                            String typeCode = fields[i].getType().getName().toLowerCase();
                            String value = hashMap.get(fields[i].getName());

                            switch (typeCode) {
                                case "double":
                                    fields[i].set(entity, Double.valueOf(value));
                                case "int":
                                case "integer":
                                    fields[i].set(entity, Integer.valueOf(value));
                                case "float":
                                    fields[i].set(entity, Float.valueOf(value));
                                case "java.lang.string":
                                case "string":
                                    fields[i].set(entity, value);
                                case "java.util.date":
                                case "date":
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        fields[i].set(entity, LocalDateTime.parse(value));
                                    }
                                    break;
                                default:
                                    break;

                            }


                        }

                    } catch (Exception ex) {

                    }
                }

                return entity;

            } catch (Exception e) {

            }
        }

        return null;
    }

    public static String [] tableColumnToBindingParameter(TableColumn[] columns) {

        String [] bindingParams = new String[columns.length];

        for (int i = 0; i < columns.length; i++) {
            bindingParams[i] = getSqliteColumnValue(columns[i].Value);
        }

        return bindingParams;
    }

    public static String getWhereClose(TableColumn[] columns) {

       String where = " ";

       if(columns.length > 0) {

           where = " WHERE ";

           for (int i = 0; i < columns.length; i++) {

               if(i == columns.length - 1)
                   where = where + columns[i].Name + " = ? ";
               else
                   where = where + columns[i].Name + " = ?, ";
           }
       }

        return where;
    }
}
