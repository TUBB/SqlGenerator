package com.tubb.sqlgenerator.compile;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;

/**
 * Created by tubingbing on 16/6/1.
 */
public class MethodElement extends CommonElement {

    List<CommonElement> parameters;

    public MethodElement(Set<Modifier> modifiers,
                         String type,
                         String name,
                         List<CommonElement> parameters) {
        super(modifiers, type, name);
        this.parameters = parameters;
    }

    public List<CommonElement> getParameters() {
        return parameters;
    }

}
