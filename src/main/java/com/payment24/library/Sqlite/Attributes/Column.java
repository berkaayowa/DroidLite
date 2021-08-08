package com.payment24.library.Sqlite.Attributes;

import Payment24.Sqlite.Enum.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)

public @interface Column {
    public String Name() default "";
//    public Constraint Constraint() default Constraint.None;
    public Constraint [] Constraint() default {};
}