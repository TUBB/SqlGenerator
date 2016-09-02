package com.tubb.sqlgenerator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by tubingbing on 16/6/20.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface Serializer {

    /**
     * Serializer packageName + className
     *
     * @return serializer canonical name
     */
    Class serializerCanonicalName();

    /**
     * serialized database supported type (packageName + className)
     *
     * @return serialized type canonical name
     */
    Class serializedTypeCanonicalName();

}
