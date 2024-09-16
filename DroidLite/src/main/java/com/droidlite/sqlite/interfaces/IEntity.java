package com.droidlite.sqlite.interfaces;

import com.droidlite.sqlite.Table;
import com.droidlite.sqlite.TableColumn;
import com.droidlite.sqlite.TableQuery;
import com.droidlite.sqlite.enums.Query;

import java.util.ArrayList;
import java.util.List;

public interface IEntity {
    
    public Boolean save();
    public ArrayList<IEntity> get(Class<?> target, TableColumn [] columns);

}
