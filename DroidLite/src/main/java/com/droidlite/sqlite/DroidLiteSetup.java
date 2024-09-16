package com.droidlite.sqlite;

import android.content.Context;

import com.droidlite.sqlite.common.Database;
import com.droidlite.sqlite.common.Helper;

public class DroidLiteSetup {

    public static void dataBase(Context context, String name, int version){
        Database.setup(context, name, version);
    }

    public static void tables(Class<?> [] entities) {

        if(entities.length > 0) {

            for (Class<?> entity : entities) {

                try {

                    Table table = Helper.convertEntityClassToTable(entity, null);
                    TableQuery query = Helper.generateCreateTableQuery(table);
                    Database.getInstance().run(query.Statement);

                } catch (Exception ex) {
                    Helper.log("DroidLiteSetup|tables|" + ex.getMessage());
                }

            }
        }

    }

}
