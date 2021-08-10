package com.payment24.library.Sqlite;

import com.payment24.library.Sqlite.Common.Database;
import com.payment24.library.Sqlite.Common.Helper;
import com.payment24.library.Sqlite.Enum.Query;
import com.payment24.library.Sqlite.Interface.IEntity;
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
