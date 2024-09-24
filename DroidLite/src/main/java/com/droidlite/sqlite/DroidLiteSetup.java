package com.droidlite.sqlite;

import android.content.Context;

import com.droidlite.sqlite.common.Database;
import com.droidlite.sqlite.common.Helper;
import com.droidlite.sqlite.common.QueryHelper;

public class DroidLiteSetup {

    public static void dataBase(Context context, String name, int dataBaseVersion){
        Database.setup(context, name, dataBaseVersion);
    }

    public static void dataBase(Context context, String name, int dataBaseVersion, Class<?> [] entities){
        Database.setup(context, name, dataBaseVersion);
        tables(entities);

    }

    public static void tables(Class<?> [] entities) {

        if(entities.length > 0) {

            for (Class<?> entity : entities) {

                try {

                    Table table = Helper.convertEntityClassToTable(entity, null);
                    TableQuery query = QueryHelper.generateCreateTableQuery(table);
                    Database.getInstance().run(query.Query);

                } catch (Exception ex) {
                    Helper.log("DroidLiteSetup|tables|" + ex.getMessage());
                }

            }
        }

    }

}
