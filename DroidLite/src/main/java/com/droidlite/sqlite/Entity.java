package com.droidlite.sqlite;

import com.droidlite.sqlite.common.Database;
import com.droidlite.sqlite.common.Helper;
import com.droidlite.sqlite.enums.Query;
import com.droidlite.sqlite.interfaces.IEntity;

import java.util.ArrayList;

public class Entity implements IEntity {

    @Override
    public Boolean save() {

        Table table = Helper.convertEntityClassToTable(this.getClass(), this);
        TableColumn primaryKeyColumn = table.getPrimaryKey();
        TableQuery query = null;

        //This is new record then generate insert query
        if(primaryKeyColumn.Value == primaryKeyColumn.getDefaultValue())
            query = Helper.generateInsertQuery(table);
        else
            query = Helper.generateUpdateQuery(table);

        return Database.getInstance().run(query.Statement);
    }

    @Override
    public ArrayList<IEntity> get(Class<?> target, TableColumn[] columns) {
        return getAll(target, columns);
    }

    public ArrayList<IEntity> get(TableColumn[] columns) {

        return get(this.getClass(), columns);
    }

    public static ArrayList<IEntity> getAll(Class<?> target, TableColumn[] columns) {

        ArrayList<IEntity> records = new ArrayList<>();
        Table table = Helper.convertEntityClassToTable(target, null);

        if(table != null) {

            TableQuery query =  Helper.generateSelectQuery(table, columns);
            records = Helper.mapResultSet(target, Database.getInstance().runSelectQuery(query.Statement, Helper.tableColumnToBindingParameter(columns)));

        }

        return records;

    }

    public static IEntity getFirstOrNull(Class<?> target, TableColumn[] columns) {

        ArrayList<IEntity> records = getAll(target, columns);

        if(!records.isEmpty())
            return records.get(0);

        return null;
    }

    public IEntity getFirstOrNull(TableColumn[] columns) {

        return getFirstOrNull(this.getClass(), columns);
    }

    @Override
    public Boolean delete() {

        Table table = Helper.convertEntityClassToTable(this.getClass(), this);
        TableColumn primaryKeyColumn = table.getPrimaryKey();

        return delete(this.getClass(), new TableColumn[]{primaryKeyColumn});
    }

    public static Boolean delete(Class<?> target, TableColumn[] columns) {

        Table table = Helper.convertEntityClassToTable(target, null);

        if(table != null) {

            TableQuery query =  Helper.generateDeleteQuery(table, columns);
            return Database.getInstance().run(query.Statement);

        }

        return false;

    }

    protected void populate(IEntity entity) {
        //ToDo
    }


}
