package com.droidlite.libtest;

import com.droidlite.sqlite.enums.Constraint;
import com.droidlite.sqlite.Entity;
import com.droidlite.sqlite.attributes.Column;
import com.droidlite.sqlite.attributes.Table;

import java.util.Date;

@Table(Name = "User")
public class User extends Entity {

    @Column(Name = "Id", Constraint = {Constraint.PrimaryKey})
    public int Id;

    @Column(Constraint = {Constraint.Null})
    public String Name;

    @Column(Constraint = {Constraint.NotNull})
    public Date Dob;

    @Column
    public float Salary;

    @Column
    public double HourWorked;

}
