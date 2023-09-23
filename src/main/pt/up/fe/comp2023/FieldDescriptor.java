package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.Objects;

public class FieldDescriptor {

    private Symbol symbol;

    private String accessModifier = "";

    public FieldDescriptor(JmmNode root){
            symbol = processNode(root);
    }

    private Symbol processNode(JmmNode root){
        String var = root.get("var");
        for (JmmNode node : root.getChildren()){
            if(Objects.equals(node.getKind(), "AccessModifier")){
                accessModifier = node.get("value");
            }
            else if(Objects.equals(node.getKind(), "Type")){
                String type = node.get("value");
                boolean isArray = type.endsWith("[]");
                if(isArray){
                    type = type.substring(0, type.length() - 2);
                }
                return new Symbol(new Type(type, isArray), var);
            }
        }
        return new Symbol(new Type("int", false), "gato");
    }

    public Symbol getSymbol() {
        return symbol;
    }


}
