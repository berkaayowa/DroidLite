package com.droidlite.sqlite;

import com.droidlite.sqlite.enums.Constraint;

import java.util.ArrayList;
import java.util.Arrays;

public class Table {

    public Table() {
        this.Columns = new ArrayList<>();
    }
    public String Name;
    public ArrayList<TableColumn> Columns;

    public TableColumn getPrimaryKey() {

        for (TableColumn column: Columns) {

            for(Constraint constraint: column.Constraints){

                if(constraint == Constraint.PrimaryKey)
                    return column;
            }

        }

        return null;
    }

}
