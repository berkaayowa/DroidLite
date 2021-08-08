package com.payment24.library.Sqlite.Interface;

import Payment24.Sqlite.Table;

public interface IEntity {
    
    public Boolean Save();
    public Table OnQueryGenerated(Table table);
}
