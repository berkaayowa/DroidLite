package com.payment24.library.Sqlite;
import Payment24.Sqlite.Common.Helper;
import Payment24.Sqlite.Interface.IEntity;
import java.util.Objects;


public class Entity implements IEntity {

    @Override
    public Boolean Save() {

        if (Objects.isNull(this)) {
            return false;
        }

        Table table =  Helper.GetEntityTable(this);
        table = OnQueryGenerated(table);

        return false;
    }

    @Override
    public Table  OnQueryGenerated(Table table) {
        return table;
    }




}
