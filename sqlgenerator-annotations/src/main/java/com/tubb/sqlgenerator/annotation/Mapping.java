package com.tubb.sqlgenerator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by tubingbing on 16/6/23.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface Mapping {

    /**
     * the mapping column name (another table's column name)
     *
     * @return the mapping column name
     */
    String name();
}
