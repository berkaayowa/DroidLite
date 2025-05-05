package com.droidlite.sqlite.common;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.Nullable;

import com.droidlite.sqlite.enums.Query;
import com.droidlite.sqlite.TableQuery;

import java.util.ArrayList;
import java.util.HashMap;

public class Database extends SQLiteOpenHelper {

    private static Database CurrentDbObject;
    public int Version;
    public boolean IsDebugMode;

    private  Database(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.Version = version;
    }

    public static Database getInstance() {

        return CurrentDbObject;
    }

    public static Database setup(@Nullable Context context, @Nullable String name, boolean isDebugMode) {

        if(CurrentDbObject == null)
            CurrentDbObject = new Database(context, name, null, 1);

        CurrentDbObject.IsDebugMode = isDebugMode;

        return CurrentDbObject;
    }

    public void onCreate(SQLiteDatabase db) {

    }

    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

    }

    public boolean Update(Query query) {

        return true;
    }

    public boolean run(String query) {

        try {

            Helper.log("Database|run|query|" + query);

            this.getWritableDatabase().execSQL(query);
            return true;
        }
        catch (Exception ex)
        {
            Helper.log("Database|run|error|0|" + ex.getMessage());
        }

        return false;
    }

    public ArrayList<HashMap<String, String>> runSelectQuery(String sql){
        return runSelectQuery(sql, new String[]{});
    }

    public ArrayList<HashMap<String, String>> runSelectQuery(String sql, String[] bindingParameter){

        ArrayList<HashMap<String, String>> result = new ArrayList<>();
        //String sql = "SELECT * from Visiteur WHERE vis_login = ? and vis_mdp = ?";
        //SQLiteStatement statement =  this.getWritableDatabase().compileStatement(sql);

        Helper.log("Database|runSelectQuery|query|" + sql);

        Cursor cursor = this.getWritableDatabase().rawQuery(sql, bindingParameter);

        while(cursor.moveToNext()){

            int columnCount = cursor.getColumnCount();
            HashMap<String, String> row = new HashMap<>();

            for (int i = 0; i < columnCount; i++)
                row.put(cursor.getColumnName(i), cursor.getString(i));

            result.add(row);

        }

        cursor.close();

        return result;

    }
}