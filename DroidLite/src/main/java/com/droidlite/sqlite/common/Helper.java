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
import java.util.HashMap;

public class Helper {

    public static Table getEntityTable(Class<?> target, Entity entity) {

        Table table = new Table();

        if (target.isAnnotationPresent(com.droidlite.sqlite.attributes.Table.class)) {

            com.droidlite.sqlite.attributes.Table attrTable = target.getAnnotation(com.droidlite.sqlite.attributes.Table.class);
            table.Name = attrTable.Name();

            Field[] fields = target.getFields();

            for (int i = 0; i < fields.length; i++) {

                try
                {
                    if(!fields[i].isAnnotationPresent(com.droidlite.sqlite.attributes.Column.class))
                        continue;

                    TableColumn tableColumn = new TableColumn();
                    com.droidlite.sqlite.attributes.Column col = fields[i].getAnnotation(com.droidlite.sqlite.attributes.Column.class);

                    tableColumn.Name = col.Name();
                    tableColumn.Constraint = col.Constraint();

                    if(entity != null)
                        tableColumn.Value = fields[i].get(entity);

                    tableColumn.Type = fields[i].getType().getName().toLowerCase();

                    if(tableColumn.Name.isEmpty())
                        tableColumn.Name = fields[i].getName();

                    table.tableColumns.add(tableColumn);

                }catch (Exception ex) {
                    System.out.println("error: " + ex.getMessage());
                }

            }
        }

        table.queries = Helper.generateQuery(table);

        return table;
    }

    public static HashMap<Query,TableQuery> generateQuery(Table table) {

        HashMap<Query,TableQuery> queryTableQueryMap = new HashMap<>();
        ArrayList<TableQuery> queries = new ArrayList<>();

        String selectQuery = "SELECT {{columnQry}} FROM " + table.Name;
        String createQuery = "CREATE TABLE IF NOT EXISTS `"+table.Name+"` ({{columns}})";
        String createColumnQry = "";
        String insertQry = "INSERT INTO " + table.Name + " ({{columnQry}}) VALUES ({{columnValue}})";
        String updateQuery = "UPDATE " + table.Name + " SET ({{column}}) WHERE ({{whereCondition}})";
        String updateQueryColumns = "";
        String columnQry = "";
        String columnValue = "";

        for(int i = 0; i < table.tableColumns.size(); i++) {

            createColumnQry = createColumnQry + table.tableColumns.get(i).Name;
            columnQry = columnQry + table.tableColumns.get(i).Name;
            columnValue = columnValue + getSqliteColumnValue(Query.Update, table.tableColumns.get(i));
            createColumnQry = createColumnQry + " " + getSqliteColumnType(table.tableColumns.get(i));

            if(i != table.tableColumns.size() - 1) {
                createColumnQry = createColumnQry + ", ";
                columnQry = columnQry + ", ";
                columnValue = columnValue + ", ";

                updateQueryColumns = updateQueryColumns + " AND " + table.tableColumns.get(i).Name  + "=" + getSqliteColumnValue(table.tableColumns.get(i).Value);

            }
            else {
                updateQueryColumns = updateQueryColumns + " " + table.tableColumns.get(i).Name  + "=" + getSqliteColumnValue(table.tableColumns.get(i).Value);
            }

        }

        insertQry = insertQry.replace("{{columnQry}}", columnQry);
        insertQry = insertQry.replace("{{columnValue}}", columnValue);
        selectQuery = selectQuery.replace("{{columnQry}}", columnQry);
        createQuery = createQuery.replace("{{columns}}", createColumnQry);

        updateQuery = updateQuery.replace("{{columns}}", updateQueryColumns );

        queryTableQueryMap.put(Query.CreateTable, new TableQuery(Query.CreateTable, createQuery));
        queryTableQueryMap.put(Query.Insert, new TableQuery(Query.Insert, insertQry));
        queryTableQueryMap.put(Query.Select, new TableQuery(Query.Select, selectQuery));

        return queryTableQueryMap;
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

    private static String getSqliteColumnValue(Query query, TableColumn tableColumn)
    {
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
