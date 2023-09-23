package pt.up.fe.comp2023;


import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

import java.util.*;

public class FoldingAnalyser extends PostorderJmmVisitor<MySymbolTable, String> {

    public List<Report> globalReports = new ArrayList<>();

    public Boolean foldHasChanged = false;

    public HashMap<String, String> variableHashmap = new HashMap<>();

    final List<String> _PERMITTED_TYPES = List.of(new String[]{"int", "boolean", "void", "String[]", "int[]", "String"});
    final List<String> _BOOLEAN_OPERATORS = List.of(new String[]{"&&", "||", ">", "<", ">=", "<=", "==", "!="});

    //receber a symbolTable
    //reports (depois têm de ser concatenados)
    @Override
    protected void buildVisitor() {

        System.out.println("foldHasChanged: " + foldHasChanged);
        addVisit("Identifier", this::checkDeclaration);
        addVisit("BinaryOp", this::checkBinaryOpOptimization);
        addVisit("Integer", this::checkInteger);


        this.setDefaultVisit(this::defaultVisitor);
    }


    private String defaultVisitor(JmmNode jmmNode, MySymbolTable mySymbolTable) {
        return "";
    }


    private String checkDeclaration(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        System.out.println("checkDeclaration ");
        System.out.println(jmmNode.getAttributes());
        System.out.println("node: " + jmmNode);
        List<JmmNode> children = jmmNode.getChildren();

        System.out.println("children: " + children);

        boolean isParameter = false;
        boolean isField = false;

        Optional<JmmNode> loopAncestor = jmmNode.getAncestor("LoopStmt");
        Optional<JmmNode> condicionalAncestor = jmmNode.getAncestor("CondicionalStmt");
        if (loopAncestor.isPresent()) {
            System.out.println("ancestors na checkDeclaration: " + loopAncestor);
        }
        if (condicionalAncestor.isPresent()) {
            System.out.println("condicionalAncestors na checkDeclaration: " + condicionalAncestor);
        }

        //Se o pai for um condicional statement, significa que este node é o node da condição, no qual deve ser feito constant propagation
        JmmNode parent = jmmNode.getJmmParent().getJmmParent();

        if (jmmNode.getJmmParent().getKind().equals("CondicionalStmt")) {
            //se o node for logo a condição
            parent = jmmNode.getJmmParent();
        }

        System.out.println("parent: " + parent);


        String methodNode = null;
        Optional<JmmNode> instanceDeclaration = jmmNode.getAncestor("InstanceDeclaration");

        System.out.println("instanceDeclaration: " + instanceDeclaration);

        if (instanceDeclaration.isPresent()) {
            methodNode = instanceDeclaration.get().get("instance");
        } else {
            methodNode = "main";
        }

        //Se fôr parâmetro

        List<Symbol> parameters = mySymbolTable.getParameters(methodNode);

        //Se for um type que não é pârametro
        for (int i = 0; i < parameters.size(); i++) {
            System.out.println("getParameters");
            System.out.println(parameters.get(i));
            if (Objects.equals(parameters.get(i).getName(), jmmNode.get("value"))) {
                //se for um parâmetro
                isParameter = true;
            }
        }

        List<Symbol> fields = mySymbolTable.getFields();

        //Se for uma variável da classe
        for (int i = 0; i < fields.size(); i++) {
            System.out.println("getFields");
            System.out.println(fields.get(i));
            if (Objects.equals(fields.get(i).getName(), jmmNode.get("value"))) {
                isField = true;

            }
        }


        System.out.println("condicional: " + condicionalAncestor.isEmpty());
        System.out.println("parental: " + parent.getKind().equals("CondicionalStmt"));
        System.out.println("variableHasmMap: " + variableHashmap);
        System.out.println("tem a key: " + variableHashmap.containsKey(jmmNode.get("value")));
        System.out.println("!isField: " + !isField);
        System.out.println("!isParameter: " + !isParameter);
        System.out.println("loopAncestor.isEmpty(): " + loopAncestor.isEmpty());
        System.out.println("(condicionalAncestor.isEmpty() || parent.getKind().equals(\"CondicionalStmt\")): " + (condicionalAncestor.isEmpty() || parent.getKind().equals("CondicionalStmt")));
        System.out.println("variableHashmap.get(jmmNode.get(\"value\")) != null: " + (variableHashmap.get(jmmNode.get("value")) != null));

        if (variableHashmap.containsKey(jmmNode.get("value")) && !isField && !isParameter && loopAncestor.isEmpty() && (condicionalAncestor.isEmpty() || parent.getKind().equals("CondicionalStmt")) && variableHashmap.get(jmmNode.get("value")) != null) {
            System.out.println("vai ser criado um novo node");
            System.out.println("variableHasmap: " + variableHashmap);
            JmmNode substituteNode = new JmmNodeImpl("Integer");
            substituteNode.put("value", variableHashmap.get(jmmNode.get("value")));
            System.out.println("substituteNode: " + substituteNode);
            jmmNode.replace(substituteNode);
        }

        return "";

    }


    private String checkBinaryOpOptimization(JmmNode jmmNode, MySymbolTable mySymbolTable) {
        System.out.println("checkBinaryOp");

        System.out.println("node: " + jmmNode);


        //se um dos filhos não for um integer, então não vou poder ter um value no binaryOp node
        for (int i = 0; i < jmmNode.getChildren().size(); i++) {
            if (!jmmNode.getChildren().get(i).getKind().equals("Integer")) {
                return "";
            }
        }

        String op = jmmNode.get("op");
        Integer result;
        Integer firstChild = Integer.valueOf(jmmNode.getChildren().get(0).get("value"));
        Integer secondChild = Integer.valueOf(jmmNode.getChildren().get(1).get("value"));

        System.out.println("firstChild: " + firstChild);
        System.out.println("secondChild: " + secondChild);

        if (op.equals("+")) {
            result = firstChild + secondChild;
        } else if (op.equals("-")) {
            result = firstChild - secondChild;
        } else if (op.equals("*")) {
            result = firstChild * secondChild;
        } else if (op.equals("/")) {
            result = firstChild / secondChild;
        } else {
            result = null;
        }

        System.out.println("result depois do cálculo: " + result);

        if (result != null) {
            JmmNode substituteNode = new JmmNodeImpl("Integer");
            substituteNode.put("value", String.valueOf(result));
            System.out.println("substituteNode: " + substituteNode);
            jmmNode.replace(substituteNode);
            foldHasChanged = true;
        }

        return "";
    }


    private String checkInteger(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        System.out.println("checkInteger");

        System.out.println("node: " + jmmNode);
        System.out.println("attributes: " + jmmNode.getAttributes());
        System.out.println("children: " + jmmNode.getChildren());


        return "";

    }


    private String checkAssignment(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        System.out.println("checkAssignment");


        return "";
    }


    public static boolean isStringOfIntegers(String str) {
        return str.matches("\\d+"); //matches one or more digits
    }
}
