package com.tubb.sqlgenerator.sample;

import com.tubb.sqlgenerator.TypeSerializer;

import java.io.File;

/**
 * Created by tubingbing on 16/9/5.
 */

public class FileSerializer implements TypeSerializer{
    @Override
    public Object serialize(Object data) {
        if (data instanceof File) return ((File) data).getAbsolutePath();
        return null;
    }

    @Override
    public Object deserialize(Object data) {
        if (data instanceof String) return new File(String.valueOf(data));
        return null;
    }
}
