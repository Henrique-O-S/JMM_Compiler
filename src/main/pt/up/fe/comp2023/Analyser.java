package pt.up.fe.comp2023;


import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Analyser extends PostorderJmmVisitor<MySymbolTable, List<Report>> {

    public List<Report> globalReports = new ArrayList<>();

    final List<String> _PERMITTED_TYPES = List.of(new String[]{"int", "boolean", "void", "String[]", "int[]", "String"});
    final List<String> _BOOLEAN_OPERATORS = List.of(new String[]{"&&", "||", ">", "<", ">=", "<=", "==", "!="});

    //receber a symbolTable
    //reports (depois têm de ser concatenados)
    @Override
    protected void buildVisitor() {
        addVisit("FieldDeclaration", this::checkFieldDeclaration);
        addVisit("InstanceDeclaration", this::checkInstanceDeclaration);
        addVisit("VarDeclarationStmt", this::checkVarDeclarationStmt);
        addVisit("Identifier", this::checkDeclaration);
        addVisit("BinaryOp", this::checkBinaryOp);
        addVisit("LengthOp", this::checkLengthOp);
        addVisit("Type", this::checkType);
        addVisit("CondicionalStmt", this::checkConditionalStatement);
        addVisit("LoopStmt", this::checkLoopStatement);
        addVisit("Integer", this::checkInteger);
        addVisit("ReservedExpr", this::checkReservedExpr);
        addVisit("Stmt", this::checkWithStmt);
        addVisit("ReturnStmt", this::checkReturnStmt);
        addVisit("ParameterType", this::checkParameterType);
        addVisit("Assignment", this::checkAssignment);
        addVisit("ArrayAssignment", this::checkAssignment);
        addVisit("ArrayDeclaration", this::checkArrayDeclaration);
        addVisit("SubscriptOp", this::checkSubscriptOp);
        addVisit("ObjectDeclaration", this::checkObjectDeclaration);
        addVisit("DotOp", this::checkDotOp);
        addVisit("PrecedenceOp", this::checkPrecedenceOp);
        this.setDefaultVisit(this::defaultVisitor);
    }

    private List<Report> defaultVisitor(JmmNode jmmNode, MySymbolTable mySymbolTable) {
        return globalReports;
    }


    private List<Report> checkVarDeclarationStmt(JmmNode jmmNode, SymbolTable symbolTable) {

        System.out.println("checkVarDeclarationStatement");
        System.out.println(jmmNode.getAttributes());
        System.out.println("node: " + jmmNode);
        List<JmmNode> children = jmmNode.getChildren();

        System.out.println("children: " + children);

        if (!children.isEmpty()) {
            System.out.println("children não está empty");
            jmmNode.put("type", children.get(0).get("type"));
            jmmNode.put("isArray", children.get(0).get("isArray"));
        }
        System.out.println("attributes after the put: " + jmmNode.getAttributes());

        System.out.println("var: " + jmmNode.get("var"));
        System.out.println("type: " + jmmNode.get("type"));

        System.out.println("-.-.-.-.-.-.-.-.-");

        return globalReports;
    }

    private List<Report> checkDeclaration(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        System.out.println("checkDeclaration ");
        System.out.println(jmmNode.getAttributes());
        System.out.println("node: " + jmmNode);
        List<JmmNode> children = jmmNode.getChildren();

        System.out.println("children: " + children);

        /*System.out.println("child1: " + children.get(0).getAttributes());
        System.out.println("child1 type: " + children.get(0).get("type"));
        System.out.println("child1 value: " + children.get(0).get("value"));*/
        System.out.println("x.x.x.x.x.x.x.x");

        //eu verifico depois se está na SymbolTable
        //primeiro nas localVariables, depois nos parametros e depois nos fields

        //tem de ser ir ver à symbol table qual o valor efetivo da variável

        String methodNode = null;
        Optional<JmmNode> instanceDeclaration = jmmNode.getAncestor("InstanceDeclaration");

        System.out.println("instanceDeclaration: " + instanceDeclaration);

        if (instanceDeclaration.isPresent()) {
            methodNode = instanceDeclaration.get().get("instance");
        } else {
            methodNode = "main";
        }

        System.out.println("methodNode: " + methodNode);

        if (jmmNode.get("value").equals("this")) {
            System.out.println("é this");
            jmmNode.put("type", mySymbolTable.getClassName());
            jmmNode.put("isArray", "false");

            if (methodNode.equals("main")) {
                System.out.println("é main");
                globalReports.add(Reports.reportCheckDeclaration(jmmNode));
            }
            return globalReports;
        }


        List<Symbol> tipo = mySymbolTable.getLocalVariables(methodNode);


        String var = jmmNode.get("value");

        //Se for um type que não é pârametro
        for (int i = 0; i < tipo.size(); i++) {
            System.out.println("getLocalVariables");
            System.out.println(tipo.get(i));
            if (Objects.equals(tipo.get(i).getName(), var)) {
                jmmNode.put("type", tipo.get(i).getType().getName());
                jmmNode.put("isArray", String.valueOf(tipo.get(i).getType().isArray()));

                return globalReports;
            }
        }

        //Se fôr parâmetro

        List<Symbol> parameters = mySymbolTable.getParameters(methodNode);

        //Se for um type que não é pârametro
        for (int i = 0; i < parameters.size(); i++) {
            System.out.println("getParameters");
            System.out.println(parameters.get(i));
            if (Objects.equals(parameters.get(i).getName(), var)) {
                jmmNode.put("type", parameters.get(i).getType().getName());
                jmmNode.put("isArray", String.valueOf(parameters.get(i).getType().isArray()));

                return globalReports;
            }
        }

        List<Symbol> fields = mySymbolTable.getFields();

        //Se for uma variável da classe
        for (int i = 0; i < fields.size(); i++) {
            System.out.println("getFields");
            System.out.println(fields.get(i));
            if (Objects.equals(fields.get(i).getName(), var)) {
                System.out.println("Objects.equals(fields.get(i).getName(), var)");
                jmmNode.put("type", fields.get(i).getType().getName());
                jmmNode.put("isArray", String.valueOf(fields.get(i).getType().isArray()));

                if (methodNode.equals("main")) {
                    //trata do caso mesmo que seja static
                    globalReports.add(Reports.reportCheckDeclaration(jmmNode));
                }
                return globalReports;
            }
        }

        List<String> imports = mySymbolTable.getImports();

        for (int i = 0; i < imports.size(); i++) {
            System.out.println("imports");
            System.out.println(imports.get(i));
            if (Objects.equals(imports.get(i), var)) {
                System.out.println("Objects.equals(imports.get(i), var)");
                jmmNode.put("type", imports.get(i));
                jmmNode.put("isArray", "false");

                return globalReports;
            }
        }


        //Se não for nenhum dos casos
        jmmNode.put("type", "none");
        jmmNode.put("isArray", "false");


        System.out.println("node type: " + jmmNode.get("type"));

        globalReports.add(Reports.reportCheckDeclaration(jmmNode));
        return globalReports;
    }

    private List<Report> checkFieldDeclaration(JmmNode jmmNode, MySymbolTable mySymbolTable) {


        return globalReports;
    }


    private List<Report> checkType(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        //List<Report> errorReports = new ArrayList<>();

        System.out.println("checkType");
        System.out.println("node attributes: " + jmmNode.getAttributes());
        System.out.println("node: " + jmmNode);
        System.out.println("value: " + jmmNode.get("value"));

        String value = jmmNode.get("value");

        if (_PERMITTED_TYPES.contains(value)) {
            System.out.println("Antes do put");
            jmmNode.put("type", value);
            if (value.equals("String[]")
                    || value.equals("int[]")) {
                jmmNode.put("isArray", "true");
            } else {
                jmmNode.put("isArray", "false");
            }

            System.out.println("node attribute after the put:" + jmmNode.getAttributes());
            System.out.println("type:" + jmmNode.get("type"));

            return globalReports;
        }

        //Verificar se o tipo não foi importado

        List<String> imports = mySymbolTable.getImports();

        for (int i = 0; i < imports.size(); i++) {
            if (imports.get(i).equals(jmmNode.get("value"))) {
                jmmNode.put("type", imports.get(i));
                jmmNode.put("isArray", "false");
                return globalReports;
            }
        }

        String superClass = mySymbolTable.getSuper();

        if (superClass != null) {
            if (superClass.equals(jmmNode.get("value"))) {
                jmmNode.put("type", jmmNode.get("value"));
                jmmNode.put("isArray", "false");
                return globalReports;
            }
        }

        String className = mySymbolTable.getClassName();

        if (className.equals(jmmNode.get("value"))) {
            jmmNode.put("type", jmmNode.get("value"));
            jmmNode.put("isArray", "false");
            return globalReports;
        }


        jmmNode.put("type", "none");
        jmmNode.put("isArray", "false");
        globalReports.add(Reports.reportCheckType(jmmNode));
        System.out.println("Não entrou no if no checkType");
        return globalReports;

    }

    private List<Report> checkBinaryOp(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        System.out.println("checkBinaryOp");
        System.out.println("node: " + jmmNode);
        System.out.println("node attributes: " + jmmNode.getAttributes());
        System.out.println("children: " + jmmNode.getChildren());

        List<JmmNode> children = jmmNode.getChildren();
        String op = jmmNode.get("op");

        System.out.println("op: " + jmmNode.get("op"));

        if (jmmNode.get("op").equals("!") && children.get(0).get("type").equals("boolean")) {
            return globalReports;
        } else if (jmmNode.get("op").equals("!") && !children.get(0).get("type").equals("boolean")) {

            globalReports.add(Reports.reportCheckBinaryOp(jmmNode, "children nodes have different types"));
            System.out.println("globalReports: " + globalReports);
            return globalReports;
        }


        if (!Objects.equals(children.get(0).get("type"), children.get(1).get("type"))
                || children.get(0).get("isArray").equals("true")
                || children.get(1).get("isArray").equals("true")) {
            //significa que os dois nodes em que se está a fazer a operação são de tipos
            // diferentes, logo não dá
            //fazer um report


            jmmNode.put("type", "none");
            jmmNode.put("isArray", "false");

            globalReports.add(Reports.reportCheckBinaryOp(jmmNode, "children nodes have different types"));

            System.out.println("globalReports: " + globalReports);
            return globalReports;
        } else {

            //Se não for um operador que permita a utilização de booleanos
            //e a situação de aceder a elementos de arrays?
            if (_BOOLEAN_OPERATORS.contains(op)) {
                System.out.println("operador é boolean");
                jmmNode.put("type", "boolean");
                jmmNode.put("isArray", "false");
                System.out.println("globalReports : " + globalReports);
                return globalReports;
            } else {
                if (jmmNode.getChildren().get(0).get("type").equals("boolean")) {
                    //dar return a report
                    jmmNode.put("type", "int");
                    jmmNode.put("isArray", "false");

                    globalReports.add(Reports.reportCheckBinaryOp(jmmNode, "not boolean operation with boolean type children"));
                    System.out.println("globalReports 2: " + globalReports);
                    return globalReports;
                } else {
                    jmmNode.put("type", "int");
                    jmmNode.put("isArray", "false");
                    System.out.println("globalReports 3: " + globalReports);


                    return globalReports;
                }

            }


        }

        //return globalReports;
    }

    private List<Report> checkConditionalStatement(JmmNode jmmNode, MySymbolTable mySymbolTable) {


        System.out.println("checkConditionalStatement");
        System.out.println("node: " + jmmNode);
        System.out.println("attributes: " + jmmNode.getAttributes());
        System.out.println("children: " + jmmNode.getChildren());

        List<JmmNode> children = jmmNode.getChildren();
        System.out.println("child 0: " + children.get(0));
        System.out.println("child 0 attributes: " + children.get(0).getAttributes());
        System.out.println("child 1: " + children.get(1));
        System.out.println("child 1 attributes: " + children.get(1).getAttributes());
        System.out.println("child 2: " + children.get(2));
        System.out.println("child 2 attributes: " + children.get(2).getAttributes());


        if (jmmNode.getChildren().get(0).get("type").equals("boolean")) {
            return globalReports;
        }

        String op = children.get(0).get("op");

        if (!_BOOLEAN_OPERATORS.contains(op)) {
            globalReports.add(Reports.reportcheckConditionalStatement(jmmNode));
            System.out.println("globalReports checkCondition: " + globalReports);
            return globalReports;
        }


        return globalReports;

    }

    private List<Report> checkLoopStatement(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        System.out.println("checkLoopStatement");

        System.out.println("node: " + jmmNode);
        System.out.println("attributes: " + jmmNode.getAttributes());
        System.out.println("children: " + jmmNode.getChildren());

        if (jmmNode.getChildren().get(0).get("type").equals("none")) {
            System.out.println("primeiro if");
            jmmNode.put("type", "none");
            jmmNode.put("type", "false");
            return globalReports;

        } else if (!jmmNode.getChildren().get(0).get("type").equals("boolean") || jmmNode.getChildren().get(0).get("isArray").equals("true")) {

            System.out.println("segundo if");
            jmmNode.put("type", "none");
            jmmNode.put("type", "false");
            globalReports.add(Reports.reportCheckLoopStatement(jmmNode));
            return globalReports;
        }

        System.out.println("default");
        jmmNode.put("type", "none");
        jmmNode.put("type", "false");

        return globalReports;
    }

    private List<Report> checkWithStmt(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        System.out.println("checkWithStmt");

        System.out.println("node: " + jmmNode);
        System.out.println("attributes: " + jmmNode.getAttributes());
        System.out.println("children: " + jmmNode.getChildren());

        jmmNode.put("type", "none");
        jmmNode.put("type", "false");


        return globalReports;
    }


    private List<Report> checkInteger(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        System.out.println("checkInteger");

        System.out.println("node: " + jmmNode);
        System.out.println("attributes: " + jmmNode.getAttributes());
        System.out.println("children: " + jmmNode.getChildren());

        jmmNode.put("type", "int");
        jmmNode.put("isArray", "false");


        System.out.println("attributes after put: " + jmmNode.getAttributes());

        return globalReports;

    }


    private List<Report> checkAssignment(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        System.out.println("checkAssignment");

        System.out.println("node: " + jmmNode);
        System.out.println("node attributes: " + jmmNode.getAttributes());
        System.out.println("var: " + jmmNode.get("var"));

        List<JmmNode> children = jmmNode.getChildren();

        System.out.println("children: " + children);
        System.out.println("child 0 attributes: " + children.get(0).getAttributes());
        System.out.println("child 0 children: " + children.get(0).getChildren());

        System.out.println("assignment child 0: " + children.get(0).getAttributes());
        //System.out.println("child 0 type: " + children.get(0).get("type"));
        String methodNode = null;
        Optional<JmmNode> instanceDeclaration = jmmNode.getAncestor("InstanceDeclaration");

        if (instanceDeclaration.isPresent()) {
            methodNode = instanceDeclaration.get().get("instance");
        } else {
            methodNode = "main";
        }

        List<Symbol> tipo = mySymbolTable.getLocalVariables(methodNode);


        String var = jmmNode.get("var");

        Boolean checkFields = true;

        //Se for um type que não é pârametro
        for (int i = 0; i < tipo.size(); i++) {
            System.out.println(tipo.get(i));
            if (Objects.equals(tipo.get(i).getName(), var)) {
                System.out.println("if das localVariables");
                jmmNode.put("type", tipo.get(i).getType().getName());
                jmmNode.put("isArray", String.valueOf(tipo.get(i).getType().isArray()));
                System.out.println("ainda nas localVariables");
                System.out.println("type: " + jmmNode.get("type"));
                System.out.println("isArray: " + jmmNode.get("isArray"));
                checkFields = false;
                break;


            }
        }

        List<Symbol> parameters = mySymbolTable.getParameters(methodNode);


        //Se for um type que não é pârametro
        for (int i = 0; i < parameters.size(); i++) {
            System.out.println("parameters at " + i + " " + parameters.get(i));

            if (Objects.equals(parameters.get(i).getName(), var)) {
                System.out.println("parameters tem uma variável igual a var");
                jmmNode.put("type", parameters.get(i).getType().getName());
                jmmNode.put("isArray", String.valueOf(parameters.get(i).getType().isArray()));
                break;

            }
        }

        if (checkFields) {
            System.out.println("checkfields is true");
            List<Symbol> fields = mySymbolTable.getFields();
            //Se for um type que não é pârametro
            for (int i = 0; i < fields.size(); i++) {
                System.out.println(fields.get(i));
                if (Objects.equals(fields.get(i).getName(), var)) {
                    jmmNode.put("type", fields.get(i).getType().getName());
                    jmmNode.put("isArray", String.valueOf(fields.get(i).getType().isArray()));

                    if (methodNode.equals("main")) {
                        //trata do caso mesmo que seja static
                        System.out.println("é main");
                        globalReports.add(Reports.reportcheckAssignment(jmmNode));
                        return globalReports;
                    }
                    break;
                }
            }
        }


        List<String> imports = mySymbolTable.getImports();
        String extendedClassName = mySymbolTable.getSuper();
        String className = mySymbolTable.getClassName();


        //Primeiro é um int resultante de um array
        if (jmmNode.getChildren().size() > 1) {

            System.out.println("children number > 1");


            if (!jmmNode.getChildren().get(0).get("type").equals(jmmNode.getChildren().get(1).get("type"))) {

                System.out.println("children have different types 1");
                jmmNode.put("type", "none");
                jmmNode.put("isArray", "false");
                globalReports.add(Reports.reportcheckAssignment(jmmNode));
                return globalReports;
            } else {
                System.out.println("all good, children have the same type");
                return globalReports;

            }

        }

        if (jmmNode.hasAttribute("type")) {

            if (jmmNode.getChildren().get(0).hasAttribute("op")) {
                if (jmmNode.getChildren().get(0).get("op").equals("!")) {
                    if (jmmNode.get("type").equals(jmmNode.getChildren().get(0).getChildren().get(0).get("type"))
                            && jmmNode.get("type").equals("booelan")) {
                        return globalReports;
                    } else {
                        globalReports.add(Reports.reportcheckAssignment(jmmNode));
                        return globalReports;
                    }
                }
            }
            if (jmmNode.get("type").equals("none") && jmmNode.getChildren().get(0).get("type").equals("none")) {
                System.out.println("primeiro if");
                return globalReports;

            } else if (jmmNode.get("type").equals("none")) { //se o node tiver tipo none (não confundir com não estar declarado), passa a ter o type do child
                System.out.println("segundo if");

                jmmNode.put("type", jmmNode.getChildren().get(0).get("type"));
                jmmNode.put("isArray", jmmNode.getChildren().get(0).get("isArray"));
                return globalReports;

            } else if (jmmNode.getChildren().get(0).get("type").equals("none")) { //o type do filho é none, não acontece nada
                System.out.println("terceiro if");

                return globalReports;

            } else if (jmmNode.get("type").equals(extendedClassName) && jmmNode.getChildren().get(0).get("type").equals(className)) {

                System.out.println("quarto if");

                return globalReports;
            } else if (imports.contains(jmmNode.get("type")) && imports.contains(jmmNode.getChildren().get(0).get("type"))) { //se ambos forem um import
                System.out.println("quinto if");

                return globalReports;

            } else if (!jmmNode.get("type").equals(jmmNode.getChildren().get(0).get("type"))) {
                System.out.println("sexto if");

                System.out.println("node type: " + jmmNode.get("type"));
                System.out.println("child 0  type: " + jmmNode.getChildren().get(0).get("type"));
                globalReports.add(Reports.reportcheckAssignment(jmmNode));
                return globalReports;

            } else if (jmmNode.get("type").equals(jmmNode.getChildren().get(0).get("type"))) {

                System.out.println("sétimo if");
                return globalReports;
            }
        } else { //ou um ou outro ou ambos são unknown, nunca nenhum foi declarado
            System.out.println("oitavo if");

            jmmNode.put("type", "none");
            jmmNode.put("isArray", "false");
            globalReports.add(Reports.reportcheckAssignment(jmmNode));
            return globalReports;
        }


        System.out.println("não é nenhum dos ifs");


        jmmNode.put("type", "none");
        jmmNode.put("isArray", "false");

        globalReports.add(Reports.reportcheckAssignment(jmmNode));
        return globalReports;
    }

    private List<Report> checkArrayDeclaration(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        System.out.println("checkArrayDeclaration");

        System.out.println("node: " + jmmNode);
        System.out.println("node attributes: " + jmmNode.getAttributes());

        System.out.println("children: " + jmmNode.getChildren());
        System.out.println("child 0 attributes: " + jmmNode.getChildren().get(0).getAttributes());

        jmmNode.put("isArray", "true");
        jmmNode.put("type", jmmNode.getChildren().get(0).get("type"));

        System.out.println("isArray: " + jmmNode.get("isArray"));
        System.out.println("after, node attributes: " + jmmNode.getAttributes());

        return globalReports;
    }

    private List<Report> checkReservedExpr(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        System.out.println("checkReservedExpr");
        Optional<JmmNode> parent = jmmNode.getAncestor("mainDeclaration");


        System.out.println("node: " + jmmNode);
        System.out.println("node attributtes: " + jmmNode.getAttributes());

        System.out.println("children: " + jmmNode.getChildren());
        System.out.println("parent: " + parent);

        String value = jmmNode.get("value");

        if (value.equals("true") || value.equals("false")) {
            jmmNode.put("type", "boolean");
            jmmNode.put("isArray", "false");
            System.out.println("type:" + jmmNode.get("type"));
            return globalReports;
        }

        String className = mySymbolTable.getClassName();
        String methodNode = null;
        Optional<JmmNode> instanceDeclaration = jmmNode.getAncestor("InstanceDeclaration");

        if (instanceDeclaration.isPresent()) {
            methodNode = instanceDeclaration.get().get("instance");
        } else {
            methodNode = "main";
        }

        if (jmmNode.get("value").equals("this")) {
            //FAZER EXCEÇÃO SE FOR STATIC
            System.out.println("dentro do if do this");
            System.out.println("className: " + className);

            if (methodNode.equals("main")) {
                System.out.println("é da main");
                jmmNode.put("type", "none");
                jmmNode.put("isArray", "false");
                globalReports.add(Reports.reportCheckReservedExpr(jmmNode));
                return globalReports;
            }

            jmmNode.put("type", className);
            jmmNode.put("isArray", "false");
            return globalReports;
        }


        if (parent.isPresent()) {
            //dar return do report de erro porque significa que o static tem como pai o mainMethod, o que
            // não pode ser
            System.out.println("has ancestor that is a mainDeclaration");
            globalReports.add(Reports.reportCheckReservedExpr(jmmNode));
            return globalReports;
        }

        return globalReports;
    }


    private List<Report> checkSubscriptOp(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        System.out.println("checkSubscriptOp");

        System.out.println("node: " + jmmNode);
        System.out.println("node attributes: " + jmmNode.getAttributes());

        System.out.println("children: " + jmmNode.getChildren());
        //System.out.println("child 0 attributes: " + jmmNode.getChildren().get(0).getAttributes());

        if (jmmNode.getChildren().get(0).get("isArray").equals("false")) {
            jmmNode.put("type", "int");
            jmmNode.put("isArray", "false");
            globalReports.add(Reports.reportCheckSubscriptOp(jmmNode));
            return globalReports;
        }

        if (!jmmNode.getChildren().get(1).get("type").equals("int")) {
            jmmNode.put("type", "int");
            jmmNode.put("isArray", "false");
            globalReports.add(Reports.reportCheckSubscriptOp(jmmNode));
            return globalReports;
        }

        jmmNode.put("type", "int");
        jmmNode.put("isArray", "false");

        return globalReports;
    }

    private List<Report> checkObjectDeclaration(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        System.out.println("checkObjectDeclaration");

        System.out.println("node: " + jmmNode);
        System.out.println("node attributes: " + jmmNode.getAttributes());

        System.out.println("children: " + jmmNode.getChildren());
        //System.out.println("child 0 atributtes: " + jmmNode.getChildren().get(0).getAttributes());

        List<String> imports = mySymbolTable.getImports();

        for (int i = 0; i < imports.size(); i++) {
            if (imports.get(i).equals(jmmNode.get("objClass"))) {
                jmmNode.put("type", jmmNode.get("objClass"));
                jmmNode.put("isArray", "false");
                return globalReports;
            }
        }

        String superClass = mySymbolTable.getSuper();

        if (superClass != null) {
            if (superClass.equals(jmmNode.get("objClass"))) {
                jmmNode.put("type", jmmNode.get("objClass"));
                jmmNode.put("isArray", "false");
                return globalReports;
            }
        }

        String className = mySymbolTable.getClassName();

        if (className.equals(jmmNode.get("objClass"))) {
            jmmNode.put("type", jmmNode.get("objClass"));
            jmmNode.put("isArray", "false");
            return globalReports;
        }

        jmmNode.put("type", "none");
        jmmNode.put("isArray", "false");
        globalReports.add(Reports.reportCheckObjectDeclaration(jmmNode));
        return globalReports;
    }

    private List<Report> checkReturnStmt(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        System.out.println("checkReturnStmt");

        System.out.println("node: " + jmmNode);
        System.out.println("node attributes: " + jmmNode.getAttributes());

        System.out.println("children: " + jmmNode.getChildren());
        System.out.println("child 0 attributes:" + jmmNode.getChildren().get(0).getAttributes());

        String methodNode = null;
        Optional<JmmNode> instanceDeclaration = jmmNode.getAncestor("InstanceDeclaration");

        if (instanceDeclaration.isPresent()) {
            methodNode = instanceDeclaration.get().get("instance");
        } else {
            methodNode = "main";
        }

        Type returnType = mySymbolTable.getReturnType(methodNode);
        System.out.println("returnType variable:" + returnType);

        if (!returnType.getName().equals(jmmNode.getChildren().get(0).get("type"))
                && !returnType.getName().equals("none")
                && !jmmNode.getChildren().get(0).get("type").equals("none")) {
            //Return type e o child 0 não têm o mesmo type
            globalReports.add(Reports.checkReturnStmt(jmmNode));
            return globalReports;
        }

        return globalReports;
    }

    private List<Report> checkDotOp(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        System.out.println("checkDotOp");

        System.out.println("node: " + jmmNode);
        System.out.println("node attributes: " + jmmNode.getAttributes());

        System.out.println("children: " + jmmNode.getChildren());
        System.out.println("child 0 children: " + jmmNode.getChildren().get(0).getChildren());
        //System.out.println("child 1 children: " + jmmNode.getChildren().get(1).getChildren());

        String methodString = jmmNode.get("method");

        /*if(methodString.equals("length")){
            System.out.println("é .length");
            jmmNode.put("type", "int");
            jmmNode.put("isArray", "false");

            return globalReports;
        }*/


        String methodNode = jmmNode.get("method");
        System.out.println("methodNode: " + methodNode);


        Type returnType = mySymbolTable.getReturnType(methodNode);


        List<String> imports = mySymbolTable.getImports();
        String extendedClassName = mySymbolTable.getSuper();
        String className = mySymbolTable.getClassName();


        System.out.println("import: " + imports);
        System.out.println("jmmNode.getChildren().get(0).get(\"type\"): " + jmmNode.getChildren().get(0).get("type"));
        System.out.println("extendedClassName: " + extendedClassName);


        //Se for de um import
        if (imports.contains(jmmNode.getChildren().get(0).get("type")) ||
                (className.equals(jmmNode.getChildren().get(0).get("type")) && extendedClassName != null)) {
            System.out.println("é um import");
            if (returnType != null) {


                List<Symbol> parameters = mySymbolTable.getParameters(methodNode);

                List<String> parameterTypes = new ArrayList<>();

                List<String> parameterArrays = new ArrayList<>();

                for (int i = 0; i < parameters.size(); i++) {
                    System.out.println("parameters");
                    System.out.println(parameters.get(i));
                    System.out.println(parameters.get(i).getName());

                    parameterTypes.add(parameters.get(i).getType().getName());
                    parameterArrays.add(String.valueOf(parameters.get(i).getType().isArray()));
                    System.out.println("parameterTypes: " + i + " " + parameterTypes.get(i));
                    System.out.println("parameterArrays: " + i + " " + parameterArrays.get(i));

                }

                List<String> parameterTypesCalled = new ArrayList<>();
                List<String> parameterIsArrayCalled = new ArrayList<>();

                for (int i = 0; i < jmmNode.getChildren().size(); i++) {
                    if (i > 0) {
                        System.out.println("child > 0 type: " + jmmNode.getChildren().get(i).get("type"));
                        System.out.println("child > 0 isArray: " + jmmNode.getChildren().get(i).get("isArray"));
                        parameterTypesCalled.add(jmmNode.getChildren().get(i).get("type"));
                        parameterIsArrayCalled.add(jmmNode.getChildren().get(i).get("isArray"));

                    }

                }

                for (int i = 0; i < parameterTypesCalled.size(); i++) {
                    System.out.println("parameterTypes: " + i + " " + parameterTypes.get(i));
                    System.out.println("parameterTypesCalled: " + i + " " + parameterTypesCalled.get(i));
                    System.out.println("parameterArrays: " + i + " " + parameterArrays.get(i));
                    System.out.println("parameterIsArrayCalled: " + i + " " + parameterIsArrayCalled.get(i));
                    if (!parameterTypes.get(i).equals(parameterTypesCalled.get(i))
                            || !parameterArrays.get(i).equals(parameterIsArrayCalled.get(i))) {
                        System.out.println("returnType: " + returnType.getName());
                        System.out.println("returnType isArray: " + returnType.isArray());
                        jmmNode.put("type", returnType.getName());
                        jmmNode.put("isArray", String.valueOf(returnType.isArray()));
                        globalReports.add(Reports.reportCheckDotOp(jmmNode));
                        return globalReports;
                    }
                }

                if (parameterTypes.size() != parameterTypesCalled.size()) {
                    jmmNode.put("type", "none");
                    jmmNode.put("isArray", "false");
                    globalReports.add(Reports.reportCheckDotOp(jmmNode));
                    return globalReports;
                }


                System.out.println("returnType: " + returnType.getName());
                System.out.println("returnType isArray: " + returnType.isArray());
                jmmNode.put("type", returnType.getName());
                jmmNode.put("isArray", String.valueOf(returnType.isArray()));

                return globalReports;
            } else { //significa que é de um import, não se sabem os argumentos da função

                System.out.println("methodNode is null, arguments have to be assumed");

                jmmNode.put("type", "none");
                jmmNode.put("isArray", "false");

                return globalReports;
            }
        } else { //se não tiver imports
            if (returnType != null) {


                List<Symbol> parameters = mySymbolTable.getParameters(methodNode);

                List<String> parameterTypes = new ArrayList<>();
                List<String> parameterArrays = new ArrayList<>();

                for (int i = 0; i < parameters.size(); i++) {
                    System.out.println("parameters");
                    System.out.println(parameters.get(i));
                    System.out.println(parameters.get(i).getName());

                    parameterTypes.add(parameters.get(i).getType().getName());
                    System.out.println("parameterTypes: " + i + " " + parameterTypes.get(i));
                    parameterArrays.add(String.valueOf(parameters.get(i).getType().isArray()));
                    System.out.println("parameterArrays: " + i + " " + parameterTypes.get(i));
                }

                System.out.println("parameterTypes: " + parameterTypes);

                List<String> parameterTypesCalled = new ArrayList<>();
                List<String> parameterIsArrayCalled = new ArrayList<>();

                for (int i = 0; i < jmmNode.getChildren().size(); i++) {
                    if (i > 0) {
                        //System.out.println("child > 0 type: " + jmmNode.getChildren().get(i).get("type"));
                        //System.out.println("child > 0 isArray: " + jmmNode.getChildren().get(i).get("isArray"));
                        if (jmmNode.getChildren().get(i).hasAttribute("type") && jmmNode.getChildren().get(i).hasAttribute("isArray")) {
                            parameterTypesCalled.add(jmmNode.getChildren().get(i).get("type"));
                            parameterIsArrayCalled.add(jmmNode.getChildren().get(i).get("isArray"));
                        }


                    }

                }

                System.out.println("parameterTypesCalled: " + parameterTypesCalled);
                System.out.println("parameterIsArrayCalled: " + parameterIsArrayCalled);

                if (parameterTypes.size() != parameterTypesCalled.size()) {
                    System.out.println("different sizes");
                    jmmNode.put("type", "none");
                    jmmNode.put("isArray", "false");
                    globalReports.add(Reports.reportCheckDotOp(jmmNode));
                    return globalReports;
                }

                for (int i = 0; i < parameterTypesCalled.size(); i++) {
                    System.out.println("i: " + i);
                    System.out.println("parameterTypes: " + i + " " + parameterTypes.get(i));
                    System.out.println("parameterTypesCalled: " + i + " " + parameterTypesCalled.get(i));
                    if ((!parameterTypes.get(i).equals(parameterTypesCalled.get(i))
                            && !parameterTypes.get(i).equals("none")
                            && !parameterTypesCalled.get(i).equals("none"))
                            || !parameterArrays.get(i).equals(parameterIsArrayCalled.get(i))) {
                        System.out.println("returnType: " + returnType.getName());
                        System.out.println("returnType isArray: " + returnType.isArray());
                        jmmNode.put("type", returnType.getName());
                        jmmNode.put("isArray", String.valueOf(returnType.isArray()));
                        globalReports.add(Reports.reportCheckDotOp(jmmNode));
                        return globalReports;
                    }
                }


                System.out.println("returnType: " + returnType.getName());
                System.out.println("returnType isArray: " + returnType.isArray());
                jmmNode.put("type", returnType.getName());
                jmmNode.put("isArray", String.valueOf(returnType.isArray()));

                return globalReports;
            } else { //significa que é de um import, não se sabem os argumentos da função

                System.out.println("erro porque não é import e não está declarado");

                jmmNode.put("type", "none");
                jmmNode.put("isArray", "false");
                globalReports.add(Reports.reportCheckDotOp(jmmNode));
                return globalReports;
            }
        }

        //return globalReports;
    }

    private List<Report> checkParameterType(JmmNode jmmNode, MySymbolTable mySymbolTable) {


        System.out.println("checkParameterType");

        System.out.println("node: " + jmmNode);
        System.out.println("node attributes: " + jmmNode.getAttributes());

        System.out.println("children: " + jmmNode.getChildren());

        jmmNode.put("type", jmmNode.getChildren().get(0).get("type"));
        jmmNode.put("isArray", jmmNode.getChildren().get(0).get("isArray"));


        return globalReports;
    }

    private List<Report> checkInstanceDeclaration(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        System.out.println("checkInstanceDeclaration");

        System.out.println("node: " + jmmNode);
        System.out.println("node attributes: " + jmmNode.getAttributes());

        System.out.println("children: " + jmmNode.getChildren());

        if (jmmNode.getChildren().size() > 2) {
            if (jmmNode.getChildren().get(2).hasAttribute("type") && jmmNode.getChildren().get(2).hasAttribute("isArray")) {
                jmmNode.put("type", jmmNode.getChildren().get(2).get("type"));
                jmmNode.put("isArray", jmmNode.getChildren().get(2).get("isArray"));
                System.out.println("node attributes after the put: " + jmmNode.getAttributes());
            }
        } else {
            jmmNode.put("type", "none");
            jmmNode.put("isArray", "false");
        }


        return globalReports;
    }

    private List<Report> checkLengthOp(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        System.out.println("checkLengthOp");

        System.out.println("node: " + jmmNode);
        System.out.println("node attributes: " + jmmNode.getAttributes());

        String methodString = jmmNode.get("method");

        System.out.println("é .length");
        jmmNode.put("type", "int");
        jmmNode.put("isArray", "false");

        System.out.println("node attributes after the put: " + jmmNode.getAttributes());

        return globalReports;

    }

    private List<Report> checkPrecedenceOp(JmmNode jmmNode, MySymbolTable mySymbolTable) {
        System.out.println("checkPrecedenceOp");
        System.out.println("node: " + jmmNode);
        System.out.println("children: " + jmmNode.getChildren());

        jmmNode.put("type", jmmNode.getChildren().get(0).get("type"));
        jmmNode.put("isArray", jmmNode.getChildren().get(0).get("isArray"));
        //System.out.println("children attributes: " + jmmNode.getChildren().);

        return globalReports;
    }

}
