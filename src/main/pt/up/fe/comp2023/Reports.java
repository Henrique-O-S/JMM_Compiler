package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.jmm.report.ReportType;

public class Reports {
    public static Report reportCheckType(JmmNode jmmNode){
        return new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "There has been an error concerning checkType");
    }

    public static Report reportCheckBinaryOp(JmmNode jmmNode, String specific){
        return new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "There has been an error concerning checkBinaryOp in " + specific);
    }

    public static Report reportcheckConditionalStatement(JmmNode jmmNode){
        return new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "There has been an error concerning checkConditionalStatement, the operation is not boolean");
    }

    public static Report reportcheckAssignment(JmmNode jmmNode){
        return new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "There has been an error concerning checkAssignment");
    }

    public static Report reportCheckReservedExpr(JmmNode jmmNode){
        return new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "There has been an error concerning checkReservedExpression");
    }

    public static Report reportCheckDeclaration(JmmNode jmmNode){
        return new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "There has been an error concerning checkDeclaration");
    }

    public static Report reportCheckSubscriptOp(JmmNode jmmNode){
        return new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "There has been an error concerning checkSubscriptOp");
    }

    public static Report reportCheckObjectDeclaration(JmmNode jmmNode){
        return new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "There has been an error concerning checkObjectDeclaration");
    }


    public static Report checkReturnStmt(JmmNode jmmNode){
        return new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "There has been an error concerning checkReturnStmt");
    }

    public static Report reportCheckDotOp(JmmNode jmmNode){
        return new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "There has been an error concerning checkDotOp");
    }

    public static Report reportCheckLoopStatement(JmmNode jmmNode){
        return new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "There has been an error concerning checkLoopStatement");
    }

}

//    public Report(pt.up.fe.comp.jmm.report.ReportType type, pt.up.fe.comp.jmm.report.Stage stage, int line, java.lang.String message) { /* compiled code */ }