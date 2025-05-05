package com.droidlite.sqlite;

import android.content.Context;

import com.droidlite.sqlite.common.Database;
import com.droidlite.sqlite.common.Helper;
import com.droidlite.sqlite.common.QueryHelper;

public class DroidLiteSetup {

    public static void dataBase(Context context, String name, boolean isDebugMode){
        Database.setup(context, name, isDebugMode);
    }

    public static void dataBase(Context context, String name, Class<?> [] entities, boolean isDebugMode){
        Database.setup(context, name, isDebugMode);
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
