package com.droidlite.sqlite.common;

import com.droidlite.sqlite.Table;
import com.droidlite.sqlite.TableColumn;
import com.droidlite.sqlite.TableQuery;
import com.droidlite.sqlite.enums.ColumnType;
import com.droidlite.sqlite.enums.Constraint;
import com.droidlite.sqlite.enums.Query;

import java.util.ArrayList;
import java.util.HashMap;

public class QueryHelper {

    public static TableQuery generateSelectQuery(Table table, TableColumn[] whereCondition) {

         ArrayList<TableQuery> alterColumnQueries = new ArrayList<>();

        String selectQuery = "SELECT {{columns}} FROM " + table.Name;
        String selectColumns = "";
        String selectWhereCondition = "";

        for(int i = 0; i < table.Columns.size(); i++) {

            selectColumns = selectColumns + table.Columns.get(i).Name;

            if(i != table.Columns.size() - 1)
                selectColumns = selectColumns + ", ";

            //BA: 24092023
            //Checking if alter column indicator is present, if yes then generate alter query
            TableQuery alterColumnQuery = getAlterQueryIfPresent(table, table.Columns.get(i));

            if(alterColumnQuery != null)
                alterColumnQueries.add(alterColumnQuery);

        }

        if(whereCondition.length > 0) {

            selectWhereCondition = " WHERE ";

            for (int i = 0; i < whereCondition.length; i++) {

                selectWhereCondition = selectWhereCondition + whereCondition[i].Name + " = " + getSqliteColumnValue(whereCondition[i].Value);

                if (i != whereCondition.length - 1)
                    selectColumns = selectColumns + " AND ";

            }

        }

        selectQuery = selectQuery.replace("{{columns}}", selectColumns);

        if(!selectWhereCondition.isEmpty())
            selectQuery = selectQuery + " " + selectWhereCondition;

        TableQuery tableQuery = new TableQuery(Query.Select, selectQuery);
        tableQuery.TableQueries = alterColumnQueries;

        return tableQuery;
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

        ArrayList<TableQuery> alterColumnQueries = new ArrayList<>();

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

            //BA: 24092023
            //Checking if alter column indicator is present, if yes then generate alter query
            TableQuery alterColumnQuery = getAlterQueryIfPresent(table, table.Columns.get(i));

            if(alterColumnQuery != null)
                alterColumnQueries.add(alterColumnQuery);

        }

        updateQuery = updateQuery.replace("{{column}}", updateQueryColumns);
        updateQuery = updateQuery.replace("{{whereCondition}}", updateQueryWhereCondition);

        TableQuery tableQuery = new TableQuery(Query.Update, updateQuery);
        tableQuery.TableQueries = alterColumnQueries;

        return tableQuery;
    }

    public static TableQuery generateInsertQuery(Table table) {

        ArrayList<TableQuery> alterColumnQueries = new ArrayList<>();

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

            //BA: 24092023
            //Checking if alter column indicator is present, if yes then generate alter query
            TableQuery alterColumnQuery = getAlterQueryIfPresent(table, table.Columns.get(i));

            if(alterColumnQuery != null)
                alterColumnQueries.add(alterColumnQuery);

        }

        insertQuery = insertQuery.replace("{{columns}}", columns);
        insertQuery = insertQuery.replace("{{columnValue}}", columnsValue);

        TableQuery tableQuery = new TableQuery(Query.Insert, insertQuery);
        tableQuery.TableQueries = alterColumnQueries;

        return tableQuery;
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
        else if(ColumnType.Boolean == columnType)
            type = "INTEGER";
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

            if(ColumnType.Boolean == columnType) {

                if(Boolean.valueOf(value)) {
                    value = "1";
                }
                else {
                    value = "0";
                }
            }

        }
        else {
            value = "NULL";
        }

        return  value;
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

    public static TableQuery getAlterQueryIfPresent(Table table, TableColumn tableColumn) {

        if(tableColumn.Alter != null) {

            try {

                //BA: 24092023
                //Alter.DataBaseVersion default value is 0 means that the alter query should be run on all version
                //IF the version is greater than zero means alter query should be run only on a specific target db version
                boolean dataBaseVersionMatches = tableColumn.Alter.DataBaseVersion() == 0;

                if (!dataBaseVersionMatches)
                    dataBaseVersionMatches = Database.getInstance().Version == tableColumn.Alter.DataBaseVersion();

                if (dataBaseVersionMatches) {

                    String alterQuery = " ALTER TABLE " + table.Name + " ADD " + tableColumn.Name + " " + getSqliteColumnType(tableColumn);

                    return new TableQuery(Query.Alter, alterQuery);
                }

            }
            catch (Exception ex) {
                Helper.log("getAlterQueryIfPresent|error|0|" + ex.getMessage());
            }

        }

        return null;

    }

}
