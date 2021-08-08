package com.payment24.libtest;

import com.payment24.library.Sqlite.Attributes.Column;
import com.payment24.library.Sqlite.Entity;
import com.payment24.library.Sqlite.Enum.Constraint;
import com.payment24.library.Sqlite.Attributes.Table;
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
