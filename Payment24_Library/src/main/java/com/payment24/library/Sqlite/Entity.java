package com.payment24.library.Sqlite;

import com.payment24.library.Sqlite.Common.Helper;
import com.payment24.library.Sqlite.Interface.IEntity;
import java.util.Objects;

public class Entity implements IEntity {

    @Override
    public Boolean Save() {

        Table table =  Helper.GetEntityTable(this);
        table = OnQueryGenerated(table);

        return false;
    }

    @Override
    public Table  OnQueryGenerated(Table table) {
        return table;
    }




}
