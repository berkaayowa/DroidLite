package com.droidlite.libtest;

import com.droidlite.sqlite.TableColumn;
import com.droidlite.sqlite.attributes.Column;
import com.droidlite.sqlite.enums.Constraint;
import com.droidlite.sqlite.Entity;
import com.droidlite.sqlite.attributes.Table;
import com.droidlite.sqlite.interfaces.IEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Table(Name = "User")
public class User extends Entity {

    public User() {

    }

    public User(int id) {
       getById(id);
    }

    public User(String name) {
        getByName(name);
    }

    @Column(Name = "Id", Constraint = {Constraint.PrimaryKey})
    public Integer Id;

    @Column(Constraint = {Constraint.Null})
    public String Name;

    @Column(Constraint = {Constraint.NotNull})
    public Date Dob;

    @Column
    public float Salary;

    @Column
    public double HourWorked;

    public ArrayList<IEntity> getById(int id) {
        return get(new TableColumn[]{new TableColumn("Id", id)});
    }

    public ArrayList<IEntity> getByName(String name) {
        //new TableColumn("Name", name)
        return get(new TableColumn[]{});
    }

}
