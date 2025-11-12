package com.droidlite.libtest;

import com.droidlite.sqlite.TableColumn;
import com.droidlite.sqlite.attributes.AlterColumn;
import com.droidlite.sqlite.attributes.Column;
import com.droidlite.sqlite.enums.AlterType;
import com.droidlite.sqlite.enums.Constraint;
import com.droidlite.sqlite.Entity;
import com.droidlite.sqlite.attributes.Table;
import com.droidlite.sqlite.interfaces.IEntity;

import java.util.ArrayList;
import java.util.Date;

@Table(Name = "tUser")
public class User extends Entity {

    public User() {

    }

    public User(int id) {
        bind(getById(id));
    }

    @Column(Name = "Id", Constraint = {Constraint.PrimaryKey})
    public int Id;

    @Column(Constraint = {Constraint.Null})
    public String Name;

    @Column
    public float Salary;

    @Column
    public double HourWorked;

    @Column(Constraint = {Constraint.Null})
    public boolean IsDeleted;

    public IEntity getById(int id) {
        return getFirstOrNull(new TableColumn[]{new TableColumn("Id", id)});
    }

    public ArrayList<IEntity> getByName(String name) {
        return get(new TableColumn[]{new TableColumn("Name", name)});
    }

    public static ArrayList<IEntity> getAll() {
        return Entity.getAll(User.class, new TableColumn[]{});
    }


}
