package com.tubb.sqlgenerator.compile;

import java.util.Set;

import javax.lang.model.element.Modifier;

/**
 * Created by tubingbing on 16/6/1.
 */
public class CommonElement {

    protected Set<Modifier> modifiers;
    protected String type;
    protected String name;

    public CommonElement(Set<Modifier> modifiers, String type, String name) {
        this.modifiers = modifiers;
        this.type = type;
        this.name = name;
    }

    public Set<Modifier> getModifiers() {
        return modifiers;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
