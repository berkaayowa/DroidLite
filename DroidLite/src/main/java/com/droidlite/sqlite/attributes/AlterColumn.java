package com.droidlite.sqlite.attributes;


import com.droidlite.sqlite.enums.AlterType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
//@Target(ElementType.FIELD)

public @interface  AlterColumn {
    public int DataBaseVersion() default 0;
    public AlterType AlterType() default AlterType.Add;
}