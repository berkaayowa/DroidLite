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
        table = onGetEntityTable(table);

        TableColumn primaryKeyColumn = table.getPrimaryKey();

        TableQuery query = null;

        //This is new record then generate insert query
        if(primaryKeyColumn.Value == primaryKeyColumn.getDefaultValue()){
            query = Helper.generateInsertQuery(table);
        }
        else {
            //new TableColumn[]{new TableColumn("Name", name)}
            query = Helper.generateUpdateQuery(table);
        }

        query = Database.getInstance().run(query);

        return query.Success;
    }

    @Override
    public ArrayList<IEntity> get(Class<?> target, TableColumn[] columns) {

        ArrayList<IEntity> records = new ArrayList<>();

        Table table = Helper.convertEntityClassToTable(target, null);

        if(table != null) {

            TableQuery query =  Helper.generateSelectQuery(table, columns);

            records = Helper.mapResultSet(target, Database.getInstance().runSelectQuery(query.Statement, Helper.tableColumnToBindingParameter(columns)));
        }

        return records;

    }

    public ArrayList<IEntity> get(TableColumn[] columns) {

        return get(this.getClass(), columns);
    }

    @Override
    public TableQuery onQueryGenerated(Query queryType, TableQuery query) {
        return query;
    }

    @Override
    public Table onGetEntityTable(Table table) {
        return table;
    }


}
