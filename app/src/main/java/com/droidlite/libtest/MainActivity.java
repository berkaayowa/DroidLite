package com.droidlite.libtest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.droidlite.sqlite.DroidLiteSetup;
import com.droidlite.sqlite.common.Helper;
import com.droidlite.sqlite.interfaces.IEntity;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DroidLiteSetup.dataBase(this, "tets.db", 1);
        DroidLiteSetup.tables(new Class[]{
                User.class
        });

        User user = new User();
        user.Id = 0;
        user.Name = "ayowa";
        user.Dob = new Date();
        user.Salary = 56;
        user.HourWorked = 30.5;

        if(user.save()) {
            Helper.log("Saved");
        }
        else {
            Helper.log("Could not be save");
        }

        ArrayList<IEntity> list = User.getAll();

        if(list.size() > 0) {
            User testuser = (User) list.get(0);
            Helper.log("Data size:" + String.valueOf(list.size()));
            Helper.log("Has data:" + String.valueOf(testuser.Id));
        }
        else {
            Helper.log("Could not be save");
        }
        //new User(1);
    }
}