package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//extends PostorderJmmVisitor<MySymbolTable, List<Report>> {
public class BuildingAnalysis implements JmmAnalysis{
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult){

        MySymbolTable mySymbolTable = new MySymbolTable(parserResult.getRootNode());

        System.out.println("parserResult.getConfig().get(\"optimize\"): " + parserResult.getConfig().get("optimize"));


        List<Report> reports = new ArrayList<>();
        List<Report> semanticAnalysis = new ArrayList<>();

        /*if(parserResult.getConfig().get("optimize").equals("true")){
            semanticAnalysis = new OptimizationAnalyser().visit(parserResult.getRootNode(), mySymbolTable);

        } else{
            semanticAnalysis = new Analyser().visit(parserResult.getRootNode(), mySymbolTable);
        }*/

        semanticAnalysis = new Analyser().visit(parserResult.getRootNode(), mySymbolTable);


        //mandar como parâmetro uma lista de reports, que depois conforme as visitas que faz acrescenta os reports de erro

        System.out.println(mySymbolTable);

        reports.addAll(semanticAnalysis);
        System.out.println("os reports finais: " + reports);

        // juntar os symbolTableReports com os da semanticAnalysis e meter aqui
        return new JmmSemanticsResult(parserResult, mySymbolTable, reports);


    }

    /*@Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult jmmParserResult) {

        MySymbolTable symbolTable = new MySymbolTable();
        JmmSemanticsResult jmmSemanticsResult = symbolTable.semanticAnalysis(jmmParserResult);
        System.out.println("\n==========================\n");
        List<Report> reports = new SemanticAnalysis().visit(jmmSemanticsResult.getRootNode(), (MySymbolTable) jmmSemanticsResult.getSymbolTable());
        System.out.println("\n==========================\n");
        System.out.println(reports);

        return new JmmSemanticsResult(jmmParserResult, jmmSemanticsResult.getSymbolTable(), reports);
    }*/


}
