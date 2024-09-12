package com.droidlite.libtest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.droidlite.sqlite.common.Database;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Database.SetUp(this, "tets.db", 1);

        User user = new User();
        user.Id = 2;
        user.Dob = new Date();
        user.Salary = 25655;
        user.HourWorked = 852.595;

        if(user.Save()) {
            System.out.println("Saved");
        }
        else {
            System.out.println("Could not be save");
        }
    }
}