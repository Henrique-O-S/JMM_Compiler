package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.Objects;

public class Optimization implements JmmOptimization {
    private StringBuilder ollirCode = new StringBuilder();

    private JmmSemanticsResult jmmSemanticsResult;

    private int tempNumber = 0;
    private int whileNumber = 0;
    private int ifNumber = 0;


    @Override
    public OllirResult toOllir(JmmSemanticsResult jmmSemanticsResult) {
        this.jmmSemanticsResult = jmmSemanticsResult;
        new OllirGenerator(this).visit(jmmSemanticsResult.getRootNode());
        System.out.print(ollirCode.toString());
        return new OllirResult(ollirCode.toString(), jmmSemanticsResult.getConfig());
    }

    public void appendToOllir(String code) {
        ollirCode.append(code);
    }

    public int getTempNumber() {
        return tempNumber++;
    }

    public int getWhileNumber() {
        return whileNumber++;
    }

    public int getIfNumber() {
        return ifNumber++;
    }


    public void decreaseTempNumber() {
        tempNumber--;
    }

    public String intToOllir(JmmNode integer) {
        return integer.get("value") + typeToOllir(new Type("int", false));
    }

    public String initObjectDeclaration(JmmNode declaration) {
        StringBuilder ret = new StringBuilder();
        String objClass = declaration.get("objClass");
        String type = typeToOllir(new Type(objClass, false));
        int tempNumber = this.getTempNumber();
        ollirCode.append("temp_").append(tempNumber).append(type).append(" :=").append(type).append(" new(").append(objClass).append(")").append(type);
        ollirCode.append(";\n");
        ollirCode.append("invokespecial(temp_").append(tempNumber).append(type).append(",\"<init>\").V");
        ollirCode.append(";\n");
        ret.append("temp_").append(tempNumber).append(type);
        return ret.toString();
    }

    private String typeToOllir(Type type) {
        StringBuilder ret = new StringBuilder();
        if (type.isArray())
            ret.append(".array");
        String typeName = type.getName();
        switch (typeName) {
            case "int" -> ret.append(".i32");
            case "void" -> ret.append(".V");
            case "boolean" -> ret.append(".bool");
            default -> ret.append(".").append(typeName);
        }

        return ret.toString();
    }

    public String getMethodRetType(JmmNode instance) {
        String name = Objects.equals(instance.getKind(), "InstanceDeclaration") ? instance.get("instance") : "main";
        return typeToOllir(jmmSemanticsResult.getSymbolTable().getReturnType(name));
    }


    public void addImport(JmmNode node) {
        String library = node.get("library");
        library = library.substring(1, library.length() - 1);
        ollirCode.append("import ");
        for (String item : library.split(", ")) {
            ollirCode.append(item).append(".");

        }
        ollirCode.deleteCharAt(ollirCode.length() - 1);
        ollirCode.append(";\n");
    }

    public void addClass() {
        if (!ollirCode.isEmpty())
            ollirCode.append("\n");
        String className = jmmSemanticsResult.getSymbolTable().getClassName();
        String extend = Objects.equals(jmmSemanticsResult.getSymbolTable().getSuper(), null) ? "" : " extends " + jmmSemanticsResult.getSymbolTable().getSuper();
        ollirCode.append(className).append(extend).append(" {\n");

    }

    public void addConstructor() {
        String className = jmmSemanticsResult.getSymbolTable().getClassName();
        ollirCode.append("\n").append(
                ".construct ").append(className).append("().V {\n").append(
                "invokespecial(this.").append(className).append(", \"<init>\").V;\n").append(
                "}\n\n");
    }


    public void addMethod(JmmNode jmmNode) {
        String name;
        String accessModifier = "public";
        JmmNode instance = jmmNode.getJmmChild(0);
        name = Objects.equals(instance.getKind(), "InstanceDeclaration") ? instance.get("instance") : "main";
        accessModifier = Objects.equals(instance.getKind(), "InstanceDeclaration") ? instance.getJmmChild(0).get("value") : "public static";
        ollirCode.append(".method ").append(accessModifier).append(" ").append(name).append("(");
        for (Symbol parameter : jmmSemanticsResult.getSymbolTable().getParameters(name)) {
            ollirCode.append(parameter.getName()).append(typeToOllir(parameter.getType())).append(", ");
        }
        if (!jmmSemanticsResult.getSymbolTable().getParameters(name).isEmpty()) {
            ollirCode.delete(ollirCode.length() - 2, ollirCode.length());
        }
        ollirCode.append(")");
        ollirCode.append(typeToOllir(jmmSemanticsResult.getSymbolTable().getReturnType(name))).append(" {\n");
    }

    public void addMethodRetType(JmmNode instance, JmmNode retStmt) {
        String name = Objects.equals(instance.getKind(), "InstanceDeclaration") ? instance.get("instance") : "main";
        String retType = typeToOllir(jmmSemanticsResult.getSymbolTable().getReturnType(name));

        ollirCode.append("ret").append(retType);

        if (!retType.equals(".V")) {
            for (Symbol localVar : jmmSemanticsResult.getSymbolTable().getLocalVariables(name)) {
                if (Objects.equals(localVar.getName(), retStmt.getJmmChild(0).get("value"))) {
                    ollirCode.append(" ").append(localVar.getName()).append(typeToOllir(localVar.getType()));
                    ollirCode.append(";\n");
                    return;
                }
            }
            int i = 1;
            for (Symbol parameter : jmmSemanticsResult.getSymbolTable().getParameters(name)) {
                if (Objects.equals(parameter.getName(), retStmt.getJmmChild(0).get("value"))) {
                    ollirCode.append(" ").append("$").append(i).append(".").append(parameter.getName()).append(typeToOllir(parameter.getType()));
                    ollirCode.append(";\n");
                    return;
                }
                i++;
            }
            if (isNumeric(retStmt.getJmmChild(0).get("value"))) {
                ollirCode.append(" ").append(retStmt.getJmmChild(0).get("value")).append(typeToOllir(new Type("int", false)));
            }
            if (Objects.equals(retStmt.getJmmChild(0).get("value"), "true") || Objects.equals(retStmt.getJmmChild(0).get("value"), "false")) {
                ollirCode.append(" ").append(retStmt.getJmmChild(0).get("value")).append(typeToOllir(new Type("bool", false)));
            }

        }
        ollirCode.append(";\n");

    }

    public void addField(JmmNode field) {
        String accessModifier = field.getNumChildren() == 2 ? field.getJmmChild(0).get("value") : "private";
        ollirCode.append(".field ").append(accessModifier);

        for (Symbol f : jmmSemanticsResult.getSymbolTable().getFields()) {
            if (Objects.equals(f.getName(), field.get("var"))) {
                ollirCode.append(" ").append(f.getName()).append(typeToOllir(f.getType())).append(";\n");
                break;
            }
        }
    }

    public String getVarOrType(JmmNode node, JmmNode instance, String condition) {
        StringBuilder retString = new StringBuilder();

        String name = Objects.equals(instance.getKind(), "InstanceDeclaration") ? instance.get("instance") : "main";
        //System.out.println(name);
        String var = Objects.equals(node.getKind(), "Assignment") || Objects.equals(node.getKind(), "ArrayAssignment") ? node.get("var") : node.get("value");
        if (isNumeric(var)) {
            return this.intToOllir(node);
        }
        for (Symbol localVar : jmmSemanticsResult.getSymbolTable().getLocalVariables(name)) {
            if (Objects.equals(localVar.getName(), var)) {
                if (Objects.equals(condition, "var"))
                    retString.append(var);
                retString.append(typeToOllir(localVar.getType()));
                return retString.toString();
            }
        }
        int i = 1;
        for (Symbol parameter : jmmSemanticsResult.getSymbolTable().getParameters(name)) {
            if (Objects.equals(parameter.getName(), var)) {
                if (Objects.equals(condition, "var"))
                    retString.append("$").append(i).append(".").append(var);
                retString.append(typeToOllir(parameter.getType()));
                return retString.toString();
            }
            i++;
        }
        for (Symbol localVar : jmmSemanticsResult.getSymbolTable().getFields()) {
            if (Objects.equals(localVar.getName(), var)) {
                if (Objects.equals(condition, "var")) {
                    retString.append(var);
                }
                retString.append(typeToOllir(localVar.getType()));
                return retString.toString();
            }
        }
        return node.get("value");
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public String getInvoke(JmmNode dotOp, JmmNode instance) {
        JmmNode left = dotOp.getJmmChild(0);
        while (!left.hasAttribute("value") && !left.hasAttribute("objClass")) {
            left = left.getJmmChild(0);
        }
        if (left.hasAttribute("value")) {
            if (Objects.equals(left.get("value"), "this")) {
                return "invokevirtual";
            }
            String name = Objects.equals(instance.getKind(), "InstanceDeclaration") ? instance.get("instance") : "main";
            for (Symbol localVar : jmmSemanticsResult.getSymbolTable().getLocalVariables(name)) {
                if (Objects.equals(localVar.getName(), left.get("value"))) {
                    return "invokevirtual";
                }
            }
            for (Symbol localVar : jmmSemanticsResult.getSymbolTable().getFields()) {
                if (Objects.equals(localVar.getName(), left.get("value"))) {
                    return "invokevirtual";
                }
            }
            for (Symbol parameter : jmmSemanticsResult.getSymbolTable().getParameters(name)) {
                if (Objects.equals(parameter.getName(), left.get("value"))) {
                    return "invokevirtual";
                }
            }
        } else {
            return "invokevirtual";
        }


        return "invokestatic";
    }

    public void checkVoidMethod(JmmNode instance) {
        String name = Objects.equals(instance.getKind(), "InstanceDeclaration") ? instance.get("instance") : "main";

        if (Objects.equals(jmmSemanticsResult.getSymbolTable().getReturnType(name).getName(), "void"))
            ollirCode.append("ret.V;\n");
    }

    public boolean isField(JmmNode node, JmmNode instance) {
        String name = Objects.equals(instance.getKind(), "InstanceDeclaration") ? instance.get("instance") : "main";
        String var = Objects.equals(node.getKind(), "Assignment") ? node.get("var") : node.get("value");

        for (Symbol field : jmmSemanticsResult.getSymbolTable().getLocalVariables(name)) {
            if (Objects.equals(field.getName(), var)) {
                return false;
            }
        }

        for (Symbol field : jmmSemanticsResult.getSymbolTable().getParameters(name)) {
            if (Objects.equals(field.getName(), var)) {
                return false;
            }
        }

        for (Symbol field : jmmSemanticsResult.getSymbolTable().getFields()) {
            if (Objects.equals(field.getName(), var)) {
                return true;
            }
        }
        return false;
    }

    public int addGetField(JmmNode node, String s) {
        String value = node.get("value");
        int tempNumber = this.getTempNumber();
        ollirCode.append("temp_").append(tempNumber).append(s).append(" :=").append(s).append(" getfield(this.").append(jmmSemanticsResult.getSymbolTable().getClassName()).append(", ").append(value).append(s).append(")").append(s);
        ollirCode.append(";\n");
        return tempNumber;
    }

    public String getDotOpType(JmmNode dotOp, JmmNode instance) {
        JmmNode left = dotOp.getJmmChild(0);
        while (!left.hasAttribute("value") && !left.hasAttribute("objClass")) {
            left = left.getJmmChild(0);
        }

        if (!left.hasAttribute("value")) {
            if (left.get("objClass").equals(jmmSemanticsResult.getSymbolTable().getClassName()))
                left.put("value", "this");
        }
        if (Objects.equals(left.get("value"), "this")) {
            return typeToOllir(jmmSemanticsResult.getSymbolTable().getReturnType(dotOp.get("method")));
        }
        String name = Objects.equals(instance.getKind(), "InstanceDeclaration") ? instance.get("instance") : "main";
        for (Symbol localVar : jmmSemanticsResult.getSymbolTable().getLocalVariables(name)) {
            if (Objects.equals(localVar.getName(), left.get("value"))) {
                if (Objects.equals(localVar.getType().getName(), jmmSemanticsResult.getSymbolTable().getClassName())) {
                    return typeToOllir(jmmSemanticsResult.getSymbolTable().getReturnType(dotOp.get("method")));
                }
            }
        }
        for (Symbol localVar : jmmSemanticsResult.getSymbolTable().getFields()) {
            if (Objects.equals(localVar.getName(), left.get("value"))) {
                if (Objects.equals(localVar.getType().getName(), jmmSemanticsResult.getSymbolTable().getClassName())) {
                    return typeToOllir(jmmSemanticsResult.getSymbolTable().getReturnType(dotOp.get("method")));
                }
            }
        }
        for (Symbol parameter : jmmSemanticsResult.getSymbolTable().getParameters(name)) {
            if (Objects.equals(parameter.getName(), left.get("value"))) {
                if (Objects.equals(parameter.getType().getName(), jmmSemanticsResult.getSymbolTable().getClassName())) {
                    return typeToOllir(jmmSemanticsResult.getSymbolTable().getReturnType(dotOp.get("method")));
                }
            }
        }
        return ".V";
    }


    public String getSubstringAfterSecondDot(String str) {
        int firstDotIndex = str.indexOf(".");
        if (firstDotIndex == -1 || firstDotIndex == str.length() - 1) {
            return "";
        }
        int secondDotIndex = str.indexOf(".", firstDotIndex + 1);
        if (secondDotIndex == -1 || secondDotIndex == str.length() - 1) {
            return "";
        } else {
            return str.substring(secondDotIndex);
        }
    }

    public String getSubstringAfterFirstDot(String str) {
        int firstDotIndex = str.indexOf(".");
        if (firstDotIndex == -1 || firstDotIndex == str.length() - 1) {
            return "";
        }
        return str.substring(firstDotIndex);
    }

    public String getArrayString(JmmNode subscriptOp, JmmNode instance) {
        StringBuilder retString = new StringBuilder();
        String name = Objects.equals(instance.getKind(), "InstanceDeclaration") ? instance.get("instance") : "main";
        String var;
        if(Objects.equals(subscriptOp.getKind(), "SubscriptOp")){
            var = subscriptOp.getJmmChild(0).get("value");
            System.out.println("VAR IS " + var);
        }
        else{
            var = subscriptOp.get("var");
        }
        for (Symbol localVar : jmmSemanticsResult.getSymbolTable().getLocalVariables(name)) {

            System.out.println("Var name|" + localVar.getName() + "|");
            System.out.println("Curr var|" + var + "|");

            if (Objects.equals(localVar.getName(), var)) {
                System.out.println("sim");
                retString.append(var);
                return retString.toString();
            }
        }
        int i = 1;
        for (Symbol parameter : jmmSemanticsResult.getSymbolTable().getParameters(name)) {
            //System.out.println(parameter.getName());
            if (Objects.equals(parameter.getName(), var)) {
                retString.append("$").append(i).append(".").append(var);
                return retString.toString();
            }
            i++;
        }
        for (Symbol localVar : jmmSemanticsResult.getSymbolTable().getFields()) {
            if (Objects.equals(localVar.getName(), var)) {
                retString.append(var);
                return retString.toString();
            }
        }
        System.out.println("ardeu");
        return "error";
    }

    public String getOp(JmmNode node) {
        String op = node.get("op");
        if (Objects.equals(op, "+") || Objects.equals(op, "-") || Objects.equals(op, "/") || Objects.equals(op, "*")) {
            op += ".i32";
        } else {
            op += ".bool";
        }
        return op;
    }

    public String getCondition(String insideIf) {
        StringBuilder ret = new StringBuilder();
        ret.append("if (").append(insideIf).append(") ");
        return ret.toString();
    }

    public String getPutField() {
        return "putfield(this." + jmmSemanticsResult.getSymbolTable().getClassName() + ", ";
    }

    public String getReservedExpr(JmmNode node) {
        String value = node.get("value");
        if (Objects.equals(value, "true") || Objects.equals(value, "false")) {
            return value + ".bool";
        }
        if (Objects.equals(value, "this")) {
            return value + "." + jmmSemanticsResult.getSymbolTable().getClassName();
        }
        return value;
    }

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticResults) {


        //System.out.println("se está empty: " + semanticsResult.getConfig().isEmpty());

        var optimizeFlag = semanticResults.getConfig().getOrDefault("optimize", "false");


        if (!optimizeFlag.equals("true")) {
            return semanticResults;
        }


        //flag está dentro do semanticsResult

        boolean hasChange = true;
        while (hasChange) {
            hasChange = false;
            var prop = new OptimizationAnalyser();
            prop.visit(semanticResults.getRootNode(), (MySymbolTable) semanticResults.getSymbolTable());
            System.out.println("propdHasChanged depois de tudo: " + prop.propHasChanged);

            var fold = new FoldingAnalyser();
            fold.visit(semanticResults.getRootNode(), (MySymbolTable) semanticResults.getSymbolTable());
            System.out.println("foldHasChanged depois de tudo: " + fold.foldHasChanged);
            hasChange = (fold.foldHasChanged || prop.propHasChanged);
            prop.propHasChanged = false;
            fold.foldHasChanged = false;
        }

        System.out.println(semanticResults.getRootNode().toTree());

        //reports.addAll(semanticAnalysis);
        //System.out.println("os reports finais na otimização: " + reports);


        return semanticResults;
    }


}
