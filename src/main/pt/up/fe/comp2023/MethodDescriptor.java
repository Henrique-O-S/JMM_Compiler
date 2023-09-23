package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.*;

public class MethodDescriptor {
    private List<Symbol> parameters;
    private Type returnType = new Type("void", false);
    private List<Symbol> localVariables;

    private List<String> localVarsName;

    private int argCounter = 0;

    private int voidFunc;
    public MethodDescriptor(JmmNode root){
        parameters = new ArrayList<>();
        localVariables = new ArrayList<>();
        voidFunc = 0;
        initLocalVarsName(root);


    }

    private void initLocalVarsName(JmmNode node){
        String argString = node.get("parameter");
        if(argString.charAt(0) == '[' && argString.endsWith("]")){
            argString = argString.substring(1, argString.length() - 1);
        }
        if(!argString.equals("")){
            localVarsName = List.of(argString.split(", "));
            System.out.println(localVarsName.size());
        }
    }

    public void setReturnType(JmmNode node){
        voidFunc = 1;

        String type = node.get("value");
        boolean isArray = type.endsWith("[]");
        if(isArray){
            type = type.substring(0, type.length() - 2);
        }
        returnType = new Type(type, isArray);





    }


    public void addArg(JmmNode node){
        String type = node.getJmmChild(0).get("value");
        boolean isArray = type.endsWith("[]");
        if(isArray){
            type = type.substring(0, type.length() - 2);
        }
        System.out.println(localVarsName.get(argCounter));
        parameters.add(new Symbol(new Type(type, isArray), localVarsName.get(argCounter)));
        argCounter++;

    }

    public void addVar(JmmNode var){
        String type;
        boolean isArray;
        if(Objects.equals(var.getKind(), "VarDeclarationStmt")){
            type = var.getJmmChild(0).get("value");
            isArray = type.endsWith("[]");
            if(isArray){
                type = type.substring(0, type.length() - 2);
            }
            localVariables.add(new Symbol(new Type(type, isArray), var.get("var")));
        }
    }


    public Type getReturnType() {
        return returnType;
    }

    public List<Symbol> getParameters() {
        return parameters;
    }

    public List<Symbol> getLocalVariables() {
        return localVariables;
    }
}
