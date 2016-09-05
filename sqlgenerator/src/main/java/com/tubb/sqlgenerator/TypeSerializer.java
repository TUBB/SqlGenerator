package com.tubb.sqlgenerator;

public interface TypeSerializer {

    Object serialize(Object data);

    Object deserialize(Object data);
}