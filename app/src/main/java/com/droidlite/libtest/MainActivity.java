package com.droidlite.libtest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.droidlite.sqlite.common.Database;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Database.Setup(this, "tets.db", 1);

        User user = new User();
        user.Id = 3;
        user.Dob = new Date();
        user.Salary = 25655;
        user.HourWorked = 852.595;

        if(user.save()) {
            Log.e("DebugN","Saved");
        }
        else {
            Log.e("DebugN","Could not be save");
        }
    }
}