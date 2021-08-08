package com.payment24.library.Sqlite;

import Payment24.Sqlite.Attributes.Column;
import Payment24.Sqlite.Enum.Constraint;
import Payment24.Sqlite.Attributes.Table;
import Payment24.Sqlite.Entity;

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
