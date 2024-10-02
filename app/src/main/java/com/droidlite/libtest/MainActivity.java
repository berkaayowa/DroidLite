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

        DroidLiteSetup.dataBase(this, "tets.db",
                new Class[]{
                        User.class
                }, true);

        User user = new User();
        user.Id = 0;
        user.Name = "ferre";
        //user.Dob = new Date();
        user.Salary = 4;
        user.HourWorked = 6;

        if(user.save()) {
            Helper.log("Saved");
        }
        else {
            Helper.log("Could not be save");
        }

//        ArrayList<IEntity> list = User.getAll();
//
//        if(!list.isEmpty()) {
//
//            User testuser = (User) list.get(0);
//
//            Helper.log("Data size:" + String.valueOf(list.size()));
//            Helper.log("Has data:" + String.valueOf(testuser.Id));
//
//            testuser.Name = "omba";
//            testuser.Dob = new Date();
//
//            if(testuser.save())
//                Helper.log("Updated");
//
//            if(list.size() > 1) {
//
//                testuser = (User) list.get(1);
//
//                if(testuser.delete()) {
//                    Helper.log("Deleted");
//                }
//            }
//        }
//        else {
//            Helper.log("Could not be save");
       // }

        ArrayList<IEntity> list = User.getAll();
        User users = new User(1);

    }
}