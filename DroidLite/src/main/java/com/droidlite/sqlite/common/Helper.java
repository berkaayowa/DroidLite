package com.droidlite.sqlite.common;

import static com.droidlite.sqlite.enums.ColumnType.*;

import android.os.Build;
import android.util.Log;

import com.droidlite.sqlite.Entity;
import com.droidlite.sqlite.Table;
import com.droidlite.sqlite.TableColumn;
import com.droidlite.sqlite.TableQuery;
import com.droidlite.sqlite.attributes.Column;
import com.droidlite.sqlite.enums.ColumnType;
import com.droidlite.sqlite.enums.Constraint;
import com.droidlite.sqlite.enums.Query;
import com.droidlite.sqlite.interfaces.IEntity;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
                    Helper.log("convertEntityClassToTable|error|0|" + ex.getMessage());
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

            columns = columns + table.Columns.get(i).Name;

            if(primaryKey.Name == table.Columns.get(i).Name) {
                columnsValue = columnsValue + " NULL ";
            }
            else
                columnsValue = columnsValue + getSqliteColumnValue(table.Columns.get(i).Value);

            if(i != table.Columns.size() - 1) {
                columns = columns + ", ";
                columnsValue = columnsValue + ", ";
            }

        }

        insertQuery = insertQuery.replace("{{columns}}", columns);
        insertQuery = insertQuery.replace("{{columnValue}}", columnsValue);

        return new TableQuery(Query.Insert, insertQuery);
    }

    public static TableQuery generateDeleteQuery(Table table, TableColumn[] whereCondition) {

        String deleteQuery = "DELETE FROM " + table.Name + " WHERE {{whereCondition}}";
        String deleteWhereCondition = " ";

        if(whereCondition.length > 0) {

            for (int i = 0; i < whereCondition.length; i++) {

                deleteWhereCondition = deleteWhereCondition + whereCondition[i].Name + " = " + getSqliteColumnValue(whereCondition[i].Value);

                if (i != whereCondition.length - 1)
                    deleteWhereCondition = deleteWhereCondition + ", ";

            }

        }

        deleteQuery = deleteQuery.replace("{{whereCondition}}", deleteWhereCondition);

        return new TableQuery(Query.Delete, deleteQuery);
    }

    private static String getSqliteColumnType(TableColumn tableColumn)
    {
        String type = "";

        ColumnType columnType = TableColumn.getColumnTypeCode(tableColumn.Type.toLowerCase());

        if(ColumnType.TextType == columnType)
            type = "TEXT";
        else if(ColumnType.IntegerType == columnType)
            type = "INTEGER";
        else if(ColumnType.DoubleType == columnType || ColumnType.FloatType == columnType)
            type = "REAL";
        else if(ColumnType.DateType == columnType)
            type = "NUMERIC";
        else
            type = "TEXT";

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

    private static String getSqliteColumnValue(Object data)
    {
        String value = "";

        if(data != null) {

            value = data.toString();
            ColumnType columnType = TableColumn.getColumnTypeCode(data.getClass().getName().toLowerCase());

            if(ColumnType.TextType == columnType || ColumnType.DateType == columnType)
                value = "'" + value + "'";

        }
        else {
            value = "NULL";
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

                            ColumnType columnType = TableColumn.getColumnTypeCode(fields[i].getType().getName().toLowerCase());
                            String value = hashMap.get(fields[i].getName());

                            if(ColumnType.TextType == columnType)
                                fields[i].set(entity, value);
                            else if(ColumnType.DoubleType == columnType)
                                fields[i].set(entity, Double.valueOf(value));
                            else if(ColumnType.FloatType == columnType)
                                fields[i].set(entity, Float.valueOf(value));
                            else if(ColumnType.IntegerType == columnType)
                                fields[i].set(entity, Integer.valueOf(value));
                            else if(ColumnType.DateType == columnType) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    fields[i].set(entity, LocalDateTime.parse(value));
                                }
                            }

                        }

                    } catch (Exception ex) {
                        Helper.log("hashMapToEntity|error|0|" + ex.getMessage());
                    }
                }

                return entity;

            } catch (Exception ex) {
                Helper.log("hashMapToEntity|error|1|" + ex.getMessage());
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

    public static void log(String message) {
        Log.e("DroidLite~Log",message);
    }
}
