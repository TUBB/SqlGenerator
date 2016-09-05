package com.tubb.sqlgenerator.compile;

import com.google.auto.service.AutoService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;

import com.tubb.sqlgenerator.annotation.Column;
import com.tubb.sqlgenerator.annotation.ForeignKey;
import com.tubb.sqlgenerator.annotation.Index;
import com.tubb.sqlgenerator.annotation.Mapping;
import com.tubb.sqlgenerator.annotation.PrimaryKey;
import com.tubb.sqlgenerator.annotation.Serializer;
import com.tubb.sqlgenerator.annotation.Table;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;
import static javax.tools.Diagnostic.Kind.WARNING;

/**
 * Created by tubingbing on 16/5/31.
 */
@AutoService(Processor.class)
public class AutoDaoProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Filer filer;
    private boolean primaryKeyFlag = false;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        elementUtils = env.getElementUtils();
        filer = env.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(Table.class.getCanonicalName());
        types.add(Column.class.getCanonicalName());
        types.add(ForeignKey.class.getCanonicalName());
        types.add(Index.class.getCanonicalName());
        types.add(Mapping.class.getCanonicalName());
        types.add(Serializer.class.getCanonicalName());
        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        HashMap<String, ClazzElement> clazzElements = new HashMap<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(Table.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                if (!element.getModifiers().contains(Modifier.ABSTRACT)) {
                    primaryKeyFlag = false;
                    TypeElement typeElement = (TypeElement) element;
                    List<? extends Element> memberElements =
                            elementUtils.getAllMembers(typeElement);
                    List<FieldElement> fieldElements = new ArrayList<>();
                    List<MethodElement> methodElements = new ArrayList<>();
                    for (Element member : memberElements) {
                        ElementKind kind = member.getKind();
                        if (kind == ElementKind.FIELD) {
                            FieldElement fieldElement = generateFieldElement(member);
                            fieldElements.add(fieldElement);
                        } else if (kind == ElementKind.METHOD) {
                            methodElements.add(generateMethodElement(member));
                        } else {
                            info("Not support ElementKind (like contructor)");
                        }
                    }
                    if (!primaryKeyFlag) {
                        error("One table must have a primary key");
                    }
                    parseColumnAnnotationsAndPut(clazzElements,
                            typeElement,
                            fieldElements,
                            methodElements);
                } else {
                    error(element, "@Table Annotation can't apply on Abstract Class");
                }
            } else {
                error(element, "@Table Annotation can only apply on Class");
            }
        }

        if (clazzElements.size() > 0) { // process method maybe call multi times

            TableContractGenerator tableContractGenerator = new TableContractGenerator(clazzElements);
            try {
                for (Map.Entry<String, ClazzElement> clazzElementEntry : clazzElements.entrySet()) {
                    ClazzElement clazzElement = clazzElementEntry.getValue();
                    tableContractGenerator.generateTableContractClass(clazzElement).writeTo(filer);
                }
            } catch (Exception e) {
                e.printStackTrace();
                error("Generate Table Contract failure", e);
            }

        }

        return false;
    }

    private void parseColumnAnnotationsAndPut(HashMap<String, ClazzElement> clazzElements,
                                              TypeElement typeElement,
                                              List<FieldElement> fieldElements,
                                              List<MethodElement> methodElements) {
        List<FieldElement> columnElements = filterFieldElements(fieldElements, methodElements);
        if (columnElements.size() > 0) {
            String clazzName = typeElement.getSimpleName().toString();
            String packageName = getPackageName(typeElement);
            ClazzElement clazzElement = new ClazzElement(typeElement.getModifiers(),
                    typeElement.asType().toString(),
                    clazzName);
            clazzElement.setFieldElements(columnElements);
            clazzElement.setPackageName(packageName);
            List<? extends AnnotationMirror> typeAnnotationMirrors =
                    elementUtils.getAllAnnotationMirrors(typeElement);
            String tableName = clazzName;
            List<ClazzElement.Index> indexs = new ArrayList<>();
            for (AnnotationMirror annotationMirror : typeAnnotationMirrors) {
                DeclaredType declaredType = annotationMirror.getAnnotationType();
                String annotationType = declaredType.asElement().asType().toString();
                if (Table.class.getCanonicalName().equals(annotationType)) {
                    Map<? extends ExecutableElement, ? extends AnnotationValue> tableElementValues =
                            annotationMirror.getElementValues();
                    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
                            : tableElementValues.entrySet()) {
                        String key = entry.getKey().getSimpleName().toString();
                        Object value = entry.getValue().getValue();
                        if ("name".equals(key)) {
                            tableName = String.valueOf(value);
                        }
                    }
                } else if (Index.class.getCanonicalName().equals(annotationType)) {
                    ClazzElement.Index index = new ClazzElement.Index();
                    Map<? extends ExecutableElement, ? extends AnnotationValue> indexElementValues =
                            annotationMirror.getElementValues();
                    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
                            : indexElementValues.entrySet()) {
                        String key = entry.getKey().getSimpleName().toString();
                        Object value = entry.getValue().getValue();
                        if ("name".equals(key)) {
                            String indexName = String.valueOf(value);
                            index.setName(indexName);
                        } else if ("columns".equals(key)) {
                            String columns = entry
                                    .getValue()
                                    .getValue()
                                    .toString()
                                    .replace("\"", "");
                            index.setColumns(Arrays.asList(columns.split(",")));
                        } else if ("unique".equals(key)) {
                            boolean unique = (boolean) value;
                            index.setUnique(unique);
                        }
                    }
                    indexs.add(index);
                }
            }
            clazzElement.setTableName(tableName);
            clazzElement.setIndices(indexs);
            clazzElements.put(typeElement.asType().toString(), clazzElement);
        } else {
            error(typeElement, "No columns");
        }
    }

    /**
     * fielter field elements
     *
     * @param fieldElements
     * @param methodElements
     * @return
     */
    private List<FieldElement> filterFieldElements(List<FieldElement> fieldElements,
                                                   List<MethodElement> methodElements) {

        List<FieldElement> targetFieldElements = new ArrayList<>();

        for (FieldElement fieldElement : fieldElements) {

            if (fieldElement.isIgnore()) continue;
            if (fieldElement.getModifiers().contains(Modifier.STATIC)
                    || fieldElement.getModifiers().contains(Modifier.FINAL)) {
                warning("Invalid field " + fieldElement.toString());
                continue;
            }
            if (fieldElement.getModifiers().contains(Modifier.PUBLIC)) {
                targetFieldElements.add(fieldElement);
                continue;
            }

            String fieldName = fieldElement.getName();
            String fieldType = fieldElement.getType();

            String fieldSetName = ClazzGenerator.buildAccessorName("set", fieldName);
            String fieldGetName;
            if ("boolean".equals(fieldType)) {
                fieldGetName = ClazzGenerator.buildAccessorName("is", fieldName);
            } else {
                fieldGetName = ClazzGenerator.buildAccessorName("get", fieldName);
            }

            boolean matchSetMethod = false;
            boolean matchGetMethod = false;
            List<CommonElement> parameters;
            for (MethodElement methodElement : methodElements) {
                if (!methodElement.getModifiers().contains(Modifier.PUBLIC)
                        || methodElement.getModifiers().contains(Modifier.STATIC)
                        || methodElement.getModifiers().contains(Modifier.FINAL))
                    continue;

                if (fieldType.equals(methodElement.getType())
                        && fieldGetName.equals(methodElement.getName()))
                    matchGetMethod = true;
                parameters = methodElement.getParameters();
                if (fieldSetName.equals(methodElement.getName())
                        && (parameters != null
                        && parameters.size() == 1
                        && parameters.get(0).getType().equals(fieldType)))
                    matchSetMethod = true;
            }

            if (matchSetMethod && matchGetMethod)
                targetFieldElements.add(fieldElement);
            else {
                if (!matchSetMethod) warning("field " + fieldName + " not find setter");
                if (!matchGetMethod) warning("field " + fieldName + " not find getter");
            }
        }

        return targetFieldElements;
    }

    private MethodElement generateMethodElement(Element member) {
        ExecutableElement executableElement = (ExecutableElement) member;
        Set<Modifier> methodModifers = executableElement.getModifiers();
        String returnType = executableElement.getReturnType().toString();
        String methodName = executableElement.getSimpleName().toString();
        List<CommonElement> methodParameters = new ArrayList<>();
        for (VariableElement variableElement : executableElement.getParameters()) {
            String variableType = variableElement.asType().toString();
            String variableName = variableElement.getSimpleName().toString();
            methodParameters.add(new CommonElement(null, variableType, variableName));
        }
        return new MethodElement(methodModifers, returnType, methodName, methodParameters);
    }

    private FieldElement generateFieldElement(Element member) {

        String fieldName = member.getSimpleName().toString();
        String fieldType = member.asType().toString();

        Set<Modifier> fieldModifers = member.getModifiers();
        FieldElement fieldElement = new FieldElement(fieldModifers, fieldType, fieldName);
        List<? extends AnnotationMirror> fieldAnnotationMirrors =
                elementUtils.getAllAnnotationMirrors(member);
        for (AnnotationMirror annotationMirror : fieldAnnotationMirrors) {
            DeclaredType declaredType = annotationMirror.getAnnotationType();
            String annotationType = declaredType.asElement().asType().toString();
            if (Column.class.getCanonicalName().equals(annotationType)) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> columnElementValues =
                        annotationMirror.getElementValues();
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
                        : columnElementValues.entrySet()) {
                    String key = entry.getKey().getSimpleName().toString();
                    Object value = entry.getValue().getValue();
                    if ("name".equals(key)) {
                        String columnName = String.valueOf(value);
                        fieldElement.setColumnName(columnName);
                    } else if ("unique".equals(key)) {
                        boolean unique = (boolean) value;
                        fieldElement.setUnique(unique);
                    } else if ("notNULL".equals(key)) {
                        boolean notNULL = (boolean) value;
                        fieldElement.setNotNULL(notNULL);
                    } else if ("ignore".equals(key)) {
                        boolean ignore = (boolean) value;
                        fieldElement.setIgnore(ignore);
                    } else if ("defaultValue".equals(key)) {
                        String defaultValue = String.valueOf(value);
                        fieldElement.setDefaultValue(defaultValue);
                    } else if ("check".equals(key)) {
                        String check = String.valueOf(value);
                        fieldElement.setCheck(check);
                    } else {
                        warning(member, key + " annotation not support yet");
                    }
                }
            } else if (Mapping.class.getCanonicalName().equals(annotationType)) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> columnElementValues =
                        annotationMirror.getElementValues();
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
                        : columnElementValues.entrySet()) {
                    String key = entry.getKey().getSimpleName().toString();
                    Object value = entry.getValue().getValue();
                    if ("name".equals(key)) {
                        String mappingColumnName = String.valueOf(value);
                        fieldElement.setMappingColumnName(mappingColumnName);
                    }
                }
            } else if (ForeignKey.class.getCanonicalName().equals(annotationType)) {
                FieldElement.ForeignKey foreignKey = new FieldElement.ForeignKey();
                Map<? extends ExecutableElement, ? extends AnnotationValue> foreignElementValues =
                        annotationMirror.getElementValues();
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
                        : foreignElementValues.entrySet()) {
                    String key = entry.getKey().getSimpleName().toString();
                    Object value = entry.getValue().getValue();
                    if ("referenceTableName".equals(key)) {
                        String referenceTableName = String.valueOf(value);
                        foreignKey.setReferenceTableName(referenceTableName);
                    } else if ("referenceColumnName".equals(key)) {
                        String referenceColumnName = String.valueOf(value);
                        foreignKey.setReferenceColumnName(referenceColumnName);
                    } else if ("action".equals(key)) {
                        String action = String.valueOf(value);
                        foreignKey.setAction(action);
                    } else {
                        warning(member, key + " annotation not support yet");
                    }
                }
                fieldElement.setForeignKey(foreignKey);
            } else if (Serializer.class.getCanonicalName().equals(annotationType)) {
                FieldElement.Serializer serializer = new FieldElement.Serializer();
                Map<? extends ExecutableElement, ? extends AnnotationValue> foreignElementValues =
                        annotationMirror.getElementValues();
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
                        : foreignElementValues.entrySet()) {
                    String key = entry.getKey().getSimpleName().toString();
                    Object value = entry.getValue().getValue();
                    if ("serializerCanonicalName".equals(key)) {
                        String serializerCanonicalName = String.valueOf(value);
                        serializer.setSerializerCanonicalName(serializerCanonicalName);
                    } else if ("serializedTypeCanonicalName".equals(key)) {
                        String serializedTypeCanonicalName = String.valueOf(value);
                        String mCT = ColumnTypeUtils.getSQLiteColumnType(serializedTypeCanonicalName);
                        if (mCT == null)
                            throw new IllegalArgumentException("Invalid serialized type");
                        serializer.setSerializedTypeCanonicalName(serializedTypeCanonicalName);
                    } else {
                        warning(member, key + " annotation not support yet");
                    }
                }
                fieldElement.setSerializer(serializer);
            } else if (PrimaryKey.class.getCanonicalName().equals(annotationType)) {
                if (primaryKeyFlag) {
                    error("One table can only have a primary key");
                }
                primaryKeyFlag = true;
                FieldElement.PrimaryKey primaryKey = new FieldElement.PrimaryKey();
                Map<? extends ExecutableElement, ? extends AnnotationValue> foreignElementValues =
                        annotationMirror.getElementValues();
                String columnType = ColumnTypeUtils.getSQLiteColumnType(fieldElement.getType());
                if (foreignElementValues.size() == 0) {
                    if (!columnType.equals(ColumnTypeUtils.INTEGER_TYPE)) {
                        primaryKey.setAutoincrement(false);
                    }
                } else {
                    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
                            : foreignElementValues.entrySet()) {
                        String key = entry.getKey().getSimpleName().toString();
                        Object value = entry.getValue().getValue();
                        if ("autoincrement".equals(key)) {
                            boolean autoincrement = (boolean) value;
                            if (autoincrement && !columnType.equals(ColumnTypeUtils.INTEGER_TYPE)) {
                                error("Column with autoincrement must be INTEGER type");
                            }
                            primaryKey.setAutoincrement(autoincrement);
                        } else {
                            warning(member, key + " annotation not support yet");
                        }
                    }
                }

                fieldElement.setPrimaryKey(primaryKey);
            } else {
                warning(member, annotationType + " annotation not support yet");
            }
        }
        return fieldElement;
    }

    private String getPackageName(TypeElement type) {
        return elementUtils.getPackageOf(type).getQualifiedName().toString();
    }

    private void info(String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(NOTE, message);
    }

    private void warning(String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(WARNING, message);
    }

    private void warning(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(WARNING, message, element);
    }

    private void error(String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(ERROR, message);
    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(ERROR, message, element);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
