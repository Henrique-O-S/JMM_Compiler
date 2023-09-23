package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.*;

public class MySymbolTable implements SymbolTable {
    private HashMap<String, ClassDescriptor> classDescriptorMap;;

    private ArrayList<String> imports;
    private ClassDescriptor mainClass;

    public MySymbolTable(JmmNode root){
        classDescriptorMap = new HashMap<>();
        imports = new ArrayList<>();
        new MyVisitor(this).visit(root);
    }
    @Override
    public List<String> getImports() {
        return imports;
    }

    @Override
    public String getClassName() {
        return mainClass.getClassName();
    }

    @Override
    public String getSuper() {
        return mainClass.getExtendedClassName();
    }

    @Override
    public List<Symbol> getFields() {
        List<Symbol> symbols = new ArrayList<>();
        for (FieldDescriptor fieldDescriptor : mainClass.getFieldDescriptor().values()){
            symbols.add(fieldDescriptor.getSymbol());
        }
        return symbols;
    }

    @Override
    public List<String> getMethods() {
        List<String> methods = new ArrayList<>();
        for (String methodName : mainClass.getMethodDescriptor().keySet()){
            methods.add(methodName);
        }
        return methods;
    }

    @Override
    public Type getReturnType(String s) {

        MethodDescriptor method = mainClass.getMethodDescriptor().get(s);

        if (method == null) {
            return null;
        }
        else {
            return method.getReturnType();
        }
    }

    @Override
    public List<Symbol> getParameters(String s) {
        return mainClass.getMethodDescriptor().get(s).getParameters();
    }

    @Override
    public List<Symbol> getLocalVariables(String s) {
        return mainClass.getMethodDescriptor().get(s).getLocalVariables();
    }

    public void setMainClass(ClassDescriptor mainClass) {
        this.mainClass = mainClass;
    }

    public ClassDescriptor getMainClass(){
        return mainClass;
    }

    //Add Nodes

    public void addImport(JmmNode node){
        imports.addAll(List.of(node.get("library").substring(1, node.get("library").length() - 1).split(", ")));
    }

    public void addClass(JmmNode node){
        ClassDescriptor classDescriptor = new ClassDescriptor(node);
        if(mainClass == null){
            mainClass = classDescriptor;
        }
        classDescriptorMap.put(node.get("name"), classDescriptor);
    }

    public void addMethod(JmmNode node){
        JmmNode instanceNode = node.getJmmChild(0);
        if(Objects.equals(instanceNode.getKind(), "MainDeclaration")){
            mainClass.getMethodDescriptor().put("main", new MethodDescriptor(instanceNode));
        }
        else{
            mainClass.getMethodDescriptor().put(instanceNode.get("instance"), new MethodDescriptor(instanceNode));
        }
    }

    public void addField(JmmNode node){
        mainClass.getFieldDescriptor().put(node.get("var"), new FieldDescriptor(node));
    }

    public void addLocalVar(String methodName, JmmNode varNode){
        mainClass.getMethodDescriptor().get(methodName).addVar(varNode);
    }

    public void addLocalArg(String methodName, JmmNode varNode){
        mainClass.getMethodDescriptor().get(methodName).addArg(varNode);
    }

}
