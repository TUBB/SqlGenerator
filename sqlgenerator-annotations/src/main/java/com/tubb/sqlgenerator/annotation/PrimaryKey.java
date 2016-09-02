package com.tubb.sqlgenerator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by tubingbing on 16/9/2.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface PrimaryKey {
    boolean autoincrement() default true;
}
