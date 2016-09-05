package com.tubb.sqlgenerator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by tubingbing on 16/6/3.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface ForeignKey {

    String referenceTableName();

    /**
     * reference column must unique
     *
     * @return reference column
     */
    String referenceColumnName();

    String action() default "";
}
