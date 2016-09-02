package com.tubb.sqlgenerator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by tubingbing on 16/5/31.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface Column {

    /**
     * column name
     *
     * @return column name
     */
    String name() default "";

    /**
     * UNIQUE CONSTRAINT
     *
     * @return unique
     */
    boolean unique() default false;

    /**
     * NOT NULL CONSTRAINT
     *
     * @return not null
     */
    boolean notNULL() default false;

    /**
     * DEFAULT CONSTRAINT
     *
     * @return default value
     */
    String defaultValue() default "";

    /**
     * CHECK CONSTRAINT
     *
     * @return check
     */
    String check() default "";

    /**
     * ignore the column
     *
     * @return ignore
     */
    boolean ignore() default false;

}