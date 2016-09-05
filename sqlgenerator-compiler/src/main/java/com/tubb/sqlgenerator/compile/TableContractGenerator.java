package com.tubb.sqlgenerator.compile;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.lang.model.element.Modifier;

/**
 * Created by tubingbing on 16/6/18.
 */
public class TableContractGenerator extends ClazzGenerator {

    HashMap<String, ClazzElement> clazzElements;

    public TableContractGenerator(HashMap<String, ClazzElement> clazzElements) {
        this.clazzElements = clazzElements;
    }

    public JavaFile generateTableContractClass(ClazzElement clazzElement) {
        String clazzName = clazzElement.getName() + TABLE_CONTRACT_SUFFIX;

        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(clazzName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        // table name field
        FieldSpec tableNameSpec = FieldSpec.builder(String.class, getTableNameFieldContractName())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", clazzElement.getTableName())
                .build();
        typeSpecBuilder.addField(tableNameSpec);

        // column field
        for (FieldElement fieldElement : clazzElement.getFieldElements()) {
            FieldSpec fieldSpec = FieldSpec.builder(String.class,
                    getFieldContractName(fieldElement.getColumnName()))
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$S", fieldElement.getColumnName())
                    .build();
            typeSpecBuilder.addField(fieldSpec);
        }

        // create table sql field
        FieldSpec createTableSqlSpec = FieldSpec.builder(String.class, getCreateTableContractName())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", createTableSql(clazzElement))
                .build();
        typeSpecBuilder.addField(createTableSqlSpec);

        List<ClazzElement.Index> indices = clazzElement.getIndices();
        for (ClazzElement.Index index : indices) {
            FieldSpec fieldSpec = FieldSpec.builder(String.class,
                    getIndexFieldName(index.getName()))
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$S", createIndexSql(clazzElement.getTableName(), index))
                    .build();
            typeSpecBuilder.addField(fieldSpec);
        }

        typeSpecBuilder.addMethod(generateCreateTableMethod(clazzElement));
        typeSpecBuilder.addMethod(generateCreateIndexMethod(clazzElement));
        typeSpecBuilder.addMethod(generateDropTableMethod(clazzElement));

        return JavaFile.builder(clazzElement.getPackageName(), typeSpecBuilder.build()).build();
    }

    private String createTableSql(ClazzElement clazzElement) {
        List<String> columnSqls = new ArrayList<>();
        for (FieldElement columnElement : clazzElement.getFieldElements()) {
            StringBuilder columnSB = new StringBuilder();
            String columnName = columnElement.getColumnName();
            String columnType = columnElement.getType();
            String baseColumnType = ColumnTypeUtils.getSQLiteColumnType(columnType);

            if (TextUtils.isEmpty(baseColumnType)) { // not the base type
                if (clazzElements.containsKey(columnElement.getType())) { // one to one
                    ClazzElement mapClazzElement = clazzElements.get(columnElement.getType());
                    List<FieldElement> fieldElements = mapClazzElement.getFieldElements();
                    for (FieldElement fieldElement :
                            fieldElements) {
                        if (fieldElement.getPrimaryKey() != null) {
                            columnType = ColumnTypeUtils.getSQLiteColumnType(fieldElement.getType());
                        }
                    }
                } else if (columnElement.getSerializer() != null) { // serializer
                    columnType = ColumnTypeUtils.getSQLiteColumnType(
                            columnElement.getSerializer().getSerializedTypeCanonicalName());
                } else if (columnType.startsWith("java.util.List")
                        || columnType.startsWith("java.util.ArrayList")) {
                    if ("java.util.List".equals(columnType)
                            || "java.util.ArrayList".equals(columnType)) {
                        throw new IllegalArgumentException("Must use generic <Type>");
                    } else {
                        int start = columnType.indexOf("<");
                        int end = columnType.indexOf(">");
                        String generic = columnType.substring(start + 1, end);
                        if (clazzElements.containsKey(generic)) {
                            for (FieldElement fe : clazzElements.get(generic).getFieldElements()) {
                                if (fe.getColumnName()
                                        .equals(columnElement.getMappingColumnName())) {
                                    columnType = ColumnTypeUtils.getSQLiteColumnType(fe.getType());
                                    break;
                                }
                            }
                        } else {
                            throw new IllegalArgumentException(generic + " Model should be apply @Table annotation");
                        }
                    }
                } else {
                    throw new IllegalArgumentException(
                            String.format("Not support type (%s) yet!!!", columnType));
                }
            } else { // the base type
                columnType = baseColumnType;
            }
            FieldElement.PrimaryKey primaryKey = columnElement.getPrimaryKey();
            if (primaryKey == null) {
                columnSB.append(columnName)
                        .append(" ")
                        .append(columnType)
                        .append(getNotNULLConstraint(columnElement))
                        .append(getDefaultConstraint(columnElement))
                        .append(getCheckConstraint(columnElement))
                        .append(getUniqueConstraint(columnElement));
            } else {
                columnSB.append(columnName)
                        .append(" ")
                        .append(columnType)
                        .append(" PRIMARY KEY")
                        .append(primaryKey.isAutoincrement()?" AUTOINCREMENT":"");
            }
            columnSqls.add(columnSB.toString());
        }

        for (FieldElement columnElement : clazzElement.getFieldElements()) {
            FieldElement.ForeignKey foreignKey = columnElement.getForeignKey();
            if (foreignKey == null) continue;
            StringBuilder columnSB = new StringBuilder();
            columnSB.append("FOREIGN KEY(")
                    .append(columnElement.getColumnName())
                    .append(")")
                    .append(" REFERENCES ")
                    .append(foreignKey.getReferenceTableName())
                    .append("(")
                    .append(foreignKey.getReferenceColumnName())
                    .append(") ")
                    .append(foreignKey.getAction());
            columnSqls.add(columnSB.toString());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ")
                .append(clazzElement.getTableName());
        sb.append("(");
        sb.append(TextUtils.join(",", columnSqls));
        sb.append(")");
        return sb.toString();
    }

    public String createIndexSql(String tableName, ClazzElement.Index index) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE INDEX ")
                .append(index.getName())
                .append(" ON ")
                .append(tableName)
                .append("(")
                .append(TextUtils.join(",", index.getColumns()))
                .append(")");
        return sb.toString();
    }

    private MethodSpec generateDropTableMethod(ClazzElement clazzElement) {
        MethodSpec.Builder dropTableBuilder = MethodSpec.methodBuilder("dropTable")
                .addModifiers(Modifier.PUBLIC).addModifiers(Modifier.STATIC)
                .returns(void.class)
                .addParameter(ClassName.get("android.database.sqlite", "SQLiteDatabase"), "db");
        String dropTableSql = "DROP TABLE IF EXISTS " + clazzElement.getTableName();
//        ClassName log = ClassName.get("autodao", "AutoDaoLog");
//        dropTableBuilder
//                .beginControlFlow("if($T.isDebug())", log)
//                .addStatement("$T.d($S)", log, dropTableSql)
//                .endControlFlow();
        dropTableBuilder.addStatement(
                "db.execSQL($S)",
                dropTableSql);
        return dropTableBuilder.build();
    }

    private MethodSpec generateCreateTableMethod(ClazzElement clazzElement) {
        MethodSpec.Builder createTableBuilder = MethodSpec.methodBuilder("createTable")
                .addModifiers(Modifier.PUBLIC).addModifiers(Modifier.STATIC)
                .returns(void.class)
                .addParameter(ClassName.get("android.database.sqlite", "SQLiteDatabase"), "db");
        ClassName contract = ClassName.get(clazzElement.getPackageName(),
                clazzElement.getName() + TABLE_CONTRACT_SUFFIX);
        String createTableSqlField = getCreateTableContractName();
//        ClassName log = ClassName.get("autodao", "AutoDaoLog");
//        createTableBuilder
//                .beginControlFlow("if($T.isDebug())", log)
//                .addStatement("$T.d($T.$L)", log, contract, createTableSqlField)
//                .endControlFlow();
        createTableBuilder.addStatement(
                "db.execSQL($T.$L)",
                contract,
                createTableSqlField);
        return createTableBuilder.build();
    }

    private MethodSpec generateCreateIndexMethod(ClazzElement clazzElement) {
        MethodSpec.Builder createIndexBuilder = MethodSpec.methodBuilder("createIndex")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(ClassName.get("android.database.sqlite", "SQLiteDatabase"), "db");
        if (clazzElement.getIndices() != null && clazzElement.getIndices().size() > 0) {
            for (ClazzElement.Index index : clazzElement.getIndices()) {
                ClassName indexClassName = ClassName.get(clazzElement.getPackageName(),
                        clazzElement.getName() + TABLE_CONTRACT_SUFFIX);
                String createIndexSqlField = getIndexFieldName(index.getName());
//                ClassName log = ClassName.get("autodao", "AutoDaoLog");
//                createIndexBuilder
//                        .beginControlFlow("if($T.isDebug())", log)
//                        .addStatement("$T.d($T.$L)", log, indexClassName, createIndexSqlField)
//                        .endControlFlow();
                createIndexBuilder.addStatement(
                        "db.execSQL($T.$L)",
                        indexClassName,
                        createIndexSqlField);
            }
        }
        return createIndexBuilder.build();
    }

    private String getUniqueConstraint(FieldElement columnElement) {
        if (columnElement.isUnique()) {
            return " UNIQUE";
        } else {
            return "";
        }
    }

    private String getCheckConstraint(FieldElement columnElement) {
        if (!TextUtils.isEmpty(columnElement.getCheck())) {
            return " CHECK(" + columnElement.getCheck() + ")";
        } else {
            return "";
        }
    }

    private String getDefaultConstraint(FieldElement columnElement) {
        if (!TextUtils.isEmpty(columnElement.getDefaultValue())) {
            return " default " + columnElement.getDefaultValue();
        } else {
            return "";
        }
    }

    private String getNotNULLConstraint(FieldElement columnElement) {
        if (columnElement.isNotNULL()) {
            return " NOT NULL";
        } else {
            return "";
        }
    }

}
