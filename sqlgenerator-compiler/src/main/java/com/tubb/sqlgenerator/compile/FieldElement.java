package com.tubb.sqlgenerator.compile;

import java.util.Set;

import javax.lang.model.element.Modifier;

/**
 * Created by tubingbing on 16/6/1.
 */
public class FieldElement extends CommonElement {

    String columnName = "";
    boolean unique = false;
    boolean notNULL = false;
    boolean ignore = false;
    String defaultValue;
    String check;
    ForeignKey foreignKey;
    Serializer serializer;
    String mappingColumnName;
    PrimaryKey primaryKey;

    public FieldElement(Set<Modifier> modifiers, String type, String name) {
        super(modifiers, type, name);
    }

    public String getColumnName() {
        if (TextUtils.isEmpty(columnName))
            return getName();
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public boolean isNotNULL() {
        return notNULL;
    }

    public void setNotNULL(boolean notNULL) {
        this.notNULL = notNULL;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setCheck(String check) {
        this.check = check;
    }

    public String getCheck() {
        return check;
    }

    public void setForeignKey(ForeignKey foreignKey) {
        this.foreignKey = foreignKey;
    }

    public ForeignKey getForeignKey() {
        return foreignKey;
    }

    public void setMappingColumnName(String mappingColumnName) {
        this.mappingColumnName = mappingColumnName;
    }

    public String getMappingColumnName() {
        return mappingColumnName;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public void setPrimaryKey(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public static class ForeignKey {

        private String referenceTableName;
        private String referenceColumnName = "_id";
        private String action;

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getReferenceColumnName() {
            return referenceColumnName;
        }

        public void setReferenceColumnName(String referenceColumnName) {
            this.referenceColumnName = referenceColumnName;
        }

        public String getReferenceTableName() {
            return referenceTableName;
        }

        public void setReferenceTableName(String referenceTableName) {
            this.referenceTableName = referenceTableName;
        }
    }

    public static class Serializer {
        private String serializerCanonicalName;
        private String serializedTypeCanonicalName;

        public void setSerializedTypeCanonicalName(String serializedTypeCanonicalName) {
            this.serializedTypeCanonicalName = serializedTypeCanonicalName;
        }

        public String getSerializedTypeCanonicalName() {
            return serializedTypeCanonicalName;
        }

        public void setSerializerCanonicalName(String serializerCanonicalName) {
            this.serializerCanonicalName = serializerCanonicalName;
        }

        public String getSerializerCanonicalName() {
            return serializerCanonicalName;
        }
    }

    public static class PrimaryKey {
        private boolean autoincrement = true;

        public void setAutoincrement(boolean autoincrement) {
            this.autoincrement = autoincrement;
        }

        public boolean isAutoincrement() {
            return autoincrement;
        }
    }
}
