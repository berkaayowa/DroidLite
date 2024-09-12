package com.droidlite.sqlite;

import com.droidlite.sqlite.common.Database;
import com.droidlite.sqlite.common.Helper;
import com.droidlite.sqlite.enums.Query;
import com.droidlite.sqlite.interfaces.IEntity;

public class Entity implements IEntity {

    @Override
    public Boolean save() {

        Table table =  Helper.getEntityTable(this);
        table = onGetEntityTable(table);

        TableQuery createQuery =  onQueryGenerated(Query.CreateTable, table.queries.get(Query.CreateTable));
        TableQuery updateQuery =  onQueryGenerated(Query.Update, table.queries.get(Query.Update));

        createQuery = Database.getInstance().run(createQuery);
        updateQuery = Database.getInstance().run(updateQuery);

        return updateQuery.Success;
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
