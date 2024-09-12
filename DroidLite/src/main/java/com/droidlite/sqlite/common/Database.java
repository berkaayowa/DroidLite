package com.droidlite.sqlite.common;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.droidlite.sqlite.enums.Query;
import com.droidlite.sqlite.TableQuery;

public class Database extends SQLiteOpenHelper {

    private static Database CurrentDbObject;

    private  Database(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static Database getInstance() {

        return CurrentDbObject;
    }

    public static Database Setup(@Nullable Context context, @Nullable String name, int version) {

        if(CurrentDbObject == null)
            CurrentDbObject = new Database(context, name, null, version);

        return CurrentDbObject;
    }

    public void onCreate(SQLiteDatabase db) {

    }

    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

    }

    public boolean Update(Query query) {

        return true;
    }

    public TableQuery run(TableQuery query) {

        try {

            this.getWritableDatabase().execSQL(query.Statement);
            query.Success = true;
        }
        catch (Exception ex)
        {
            query.Success = false;
        }

        return query;
    }
}