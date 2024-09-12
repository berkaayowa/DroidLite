package com.droidlite.sqlite.interfaces;

import com.droidlite.sqlite.Table;

public interface IEntity {
    
    public Boolean Save();
    public Table OnQueryGenerated(Table table);
}
