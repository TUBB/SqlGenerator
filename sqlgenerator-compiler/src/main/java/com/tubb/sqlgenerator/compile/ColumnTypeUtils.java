package com.tubb.sqlgenerator.compile;

import java.util.HashMap;

/**
 * Created by tubingbing on 16/6/3.
 */
public class ColumnTypeUtils {

    public static final String INTEGER_TYPE = "INTEGER";
    public static final String REAL_TYPE = "REAL";
    public static final String TEXT_TYPE = "TEXT";
    public static final String BLOB_TYPE = "BLOB";

    private ColumnTypeUtils() {

    }

    private static final HashMap<String, String> SQLITE_TYPE_METHOD_MAP =
            new HashMap<String, String>() {
                {
                    put("byte", "getInt");
                    put("short", "getShort");
                    put("int", "getInt");
                    put("long", "getLong");
                    put("float", "getFloat");
                    put("double", "getDouble");
                    put("boolean", "getInt");
                    put("char", "getString");
                    put("byte[]", "getBlob");
                    put("java.lang.Byte", "getInt");
                    put("java.lang.Short", "getShort");
                    put("java.lang.Integer", "getInt");
                    put("java.lang.Long", "getLong");
                    put("java.lang.Float", "getFloat");
                    put("java.lang.Double", "getDouble");
                    put("java.lang.Boolean", "getInt");
                    put("java.lang.Character", "getString");
                    put("java.lang.String", "getString");
                    put("java.lang.Byte[]", "getBlob");
                }
            };

    private static final HashMap<String, String> BASE_TYPE_MAP = new HashMap<String, String>() {
        {
            put("byte", INTEGER_TYPE);
            put("short", INTEGER_TYPE);
            put("int", INTEGER_TYPE);
            put("long", INTEGER_TYPE);
            put("float", REAL_TYPE);
            put("double", REAL_TYPE);
            put("boolean", INTEGER_TYPE);
            put("char", TEXT_TYPE);
            put("byte[]", BLOB_TYPE);
            put("java.lang.Byte", INTEGER_TYPE);
            put("java.lang.Short", INTEGER_TYPE);
            put("java.lang.Integer", INTEGER_TYPE);
            put("java.lang.Long", INTEGER_TYPE);
            put("java.lang.Float", REAL_TYPE);
            put("java.lang.Double", REAL_TYPE);
            put("java.lang.Boolean", INTEGER_TYPE);
            put("java.lang.Character", TEXT_TYPE);
            put("java.lang.String", TEXT_TYPE);
            put("java.lang.Byte[]", BLOB_TYPE);
        }
    };

    public static boolean isBoolean(String javaType) {
        return "boolean".equals(javaType) || "java.lang.Boolean".equals(javaType);
    }

    public static boolean isChar(String javaType) {
        return "char".equals(javaType) || "java.lang.Character".equals(javaType);
    }

    public static boolean isByte(String javaType) {
        return "byte".equals(javaType) || "java.lang.Byte".equals(javaType);
    }

    public static String getSQLiteColumnType(String javaType) {
        return BASE_TYPE_MAP.get(javaType);
    }

    public static String getSQLiteTypeMethod(String javaType) {
        return SQLITE_TYPE_METHOD_MAP.get(javaType);
    }

}
