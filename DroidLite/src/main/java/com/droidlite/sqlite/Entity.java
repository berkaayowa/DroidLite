package com.droidlite.sqlite;

import com.droidlite.sqlite.common.Database;
import com.droidlite.sqlite.common.Helper;
import com.droidlite.sqlite.enums.Query;
import com.droidlite.sqlite.interfaces.IEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Entity implements IEntity {

    @Override
    public Boolean save() {

        Table table = Helper.getEntityTable(this.getClass(), this);
        table = onGetEntityTable(table);

        TableQuery createQuery =  onQueryGenerated(Query.CreateTable, table.queries.get(Query.CreateTable));
        TableQuery updateQuery =  onQueryGenerated(Query.Update, table.queries.get(Query.Update));

        createQuery = Database.getInstance().run(createQuery);
        updateQuery = Database.getInstance().run(updateQuery);

        return updateQuery.Success;
    }

    @Override
    public ArrayList<IEntity> get(Class<?> target, TableColumn[] columns) {

        Table table = Helper.getEntityTable(target, null);
        TableQuery query =  table.queries.get(Query.Select);
        query.Statement = query.Statement + Helper.getWhereClose(columns);

        return Helper.mapResultSet(target, Database.getInstance().runSelectQuery(query.Statement, Helper.tableColumnToBindingParameter(columns)));

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
