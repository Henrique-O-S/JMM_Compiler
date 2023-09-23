package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.*;

public class ClassDescriptor {
    private Map<String, FieldDescriptor> fieldDescriptor;
    private Map<String, MethodDescriptor> methodDescriptor;
    private String className;
    private String extendedClassName;

    public ClassDescriptor(JmmNode node) {
        fieldDescriptor = new HashMap<>();
        methodDescriptor = new HashMap<>();
        className = node.get("name");
        extendedClassName = node.hasAttribute("superclass") ? node.get("superclass") : null;
    }

    public String getClassName() {
        return className;
    }

    public String getExtendedClassName(){
        return extendedClassName;
    }

    public Map<String, FieldDescriptor> getFieldDescriptor(){
        return fieldDescriptor;
    }

    public Map<String, MethodDescriptor> getMethodDescriptor(){
        return methodDescriptor;
    }

    public void setExtendedClassName(String name){
        this.extendedClassName = name;
    }
}
