package com.droidlite.sqlite.interfaces;

import com.droidlite.sqlite.Table;
import com.droidlite.sqlite.TableQuery;
import com.droidlite.sqlite.enums.Query;

public interface IEntity {
    
    public Boolean save();
    public TableQuery onQueryGenerated(Query queryType, TableQuery tableQuery);
    public Table onGetEntityTable(Table table);

}
