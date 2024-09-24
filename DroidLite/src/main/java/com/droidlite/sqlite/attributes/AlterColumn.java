package com.droidlite.sqlite.attributes;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
//@Target(ElementType.FIELD)

public @interface AlterColumn {
    public int DataBaseVersion() default 0;
//    public Constraint[] Constraint() default {};
}