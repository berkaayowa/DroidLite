package com.droidlite.sqlite;

import com.droidlite.sqlite.common.Database;
import com.droidlite.sqlite.common.Helper;
import com.droidlite.sqlite.enums.Query;
import com.droidlite.sqlite.interfaces.IEntity;
import java.util.Objects;

public class Entity implements IEntity {

    @Override
    public Boolean Save() {

        Table table =  Helper.GetEntityTable(this);
        table = OnQueryGenerated(table);

        TableQuery CreateQuery =  table.Queries.get(Query.CreateTable);
        TableQuery UpdateQuery =  table.Queries.get(Query.Update);

        Database.GetInstance().Run(CreateQuery);

        for (int i = 0; i < table.Queries.size(); i++) {



        }


        return false;
    }

    @Override
    public Table  OnQueryGenerated(Table table) {
        return table;
    }




}
