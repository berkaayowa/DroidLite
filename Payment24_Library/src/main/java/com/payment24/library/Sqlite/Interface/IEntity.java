package com.payment24.library.Sqlite.Interface;

import com.payment24.library.Sqlite.Table;

public interface IEntity {
    
    public Boolean Save();
    public Table OnQueryGenerated(Table table);
}
