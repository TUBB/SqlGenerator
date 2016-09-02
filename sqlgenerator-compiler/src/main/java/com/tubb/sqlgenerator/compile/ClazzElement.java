package com.tubb.sqlgenerator.compile;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;

/**
 * Created by tubingbing on 16/6/3.
 */
public class ClazzElement extends CommonElement {

    private String packageName;
    private String tableName;
    private List<FieldElement> fieldElements;
    private List<Index> indices;

    public ClazzElement(Set<Modifier> modifiers, String type, String name) {
        super(modifiers, type, name);
    }

    public List<FieldElement> getFieldElements() {
        return fieldElements;
    }

    public void setFieldElements(List<FieldElement> fieldElements) {
        this.fieldElements = fieldElements;
    }

    public List<Index> getIndices() {
        return indices;
    }

    public void setIndices(List<Index> indices) {
        this.indices = indices;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public static class Index {

        private String name;
        private List<String> columns;
        private boolean unique;

        public List<String> getColumns() {
            return columns;
        }

        public void setColumns(List<String> columns) {
            this.columns = columns;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setUnique(boolean unique) {
            this.unique = unique;
        }

        public boolean isUnique() {
            return unique;
        }
    }
}
