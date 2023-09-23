package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;

public class Analysis implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult jmmParserResult) {
        MySymbolTable mySymbolTable = new MySymbolTable(jmmParserResult.getRootNode());
        List<Report> reports = new ArrayList<>();
        return new JmmSemanticsResult(jmmParserResult, mySymbolTable, reports);
    }
}
