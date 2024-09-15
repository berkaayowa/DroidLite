package com.droidlite.libtest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.droidlite.sqlite.Entity;
import com.droidlite.sqlite.common.Database;

import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Database.setup(this, "tets.db", 1);

//        User user = new User();
//        user.Id = 0;
//        user.Name = "ayowa";
//        user.Dob = new Date();
//        user.Salary = 56;
//        user.HourWorked = 30.5;
//
//        if(user.save()) {
//            Log.e("DebugN","Saved");
//        }
//        else {
//            Log.e("DebugN","Could not be save");
//        }

        new User("ayowa");
    }
}