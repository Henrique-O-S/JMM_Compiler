package pt.up.fe.comp2023;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.comp2023.BuildingAnalysis;

public class Launcher {

    public static void main(String[] args) {
        // Setups console logging and other things
        SpecsSystem.programStandardInit();

        // Parse arguments as a map with predefined options
        var config = parseArgs(args);


        //Map<String, String> arguments = parseArgs(args);



        // Get input file
        File inputFile = new File(config.get("inputFile"));

        // Check if file exists
        if (!inputFile.isFile()) {
            throw new RuntimeException("Expected a path to an existing input file, got '" + inputFile + "'.");
        }

        // Read contents of input file
        String code = SpecsIo.read(inputFile);

        // Instantiate JmmParser
        SimpleParser parser = new SimpleParser();

        // Parse stage
        JmmParserResult parserResult = parser.parse(code, config);
        if(parserResult.getReports().size() > 0){

            //TestUtils.noErrors(parserResult.getReports()); //imprime as exceções
            Integer errorNumber = parserResult.getReports().size();
            String errorMessage = "A total of " + errorNumber + " errors have occurred";
            System.out.println(errorMessage);
            for(int i = 0; i < parserResult.getReports().size(); i++){
                System.out.println("Errors:" + parserResult.getReports());
            }


        }
        else{
            System.out.println(parserResult.getRootNode().toTree()); //dar print da árvore
            MySymbolTable mySymbolTable = new MySymbolTable(parserResult.getRootNode());

            System.out.println(mySymbolTable.print());
            //System.out.println(mySymbolTable.getImports());
            JmmSemanticsResult semanticAnalysis = new JmmSemanticsResult(parserResult.getRootNode(), mySymbolTable, null, null);

            BuildingAnalysis analyser = new BuildingAnalysis();
            JmmSemanticsResult semanticResults = analyser.semanticAnalysis(parserResult);

            //Para testar com o Launcher:
            //analyser.semanticAnalysis(parserResult);


            Optimization optimization = new Optimization();

            System.out.println("antes da optimization " + semanticResults.getConfig());
            System.out.println("aa: " + semanticAnalysis.getConfig());

            //passar esta lógica para dentro do optimize, aqui chamar só o optimize
            //no server, só o optimize é chamado

            semanticResults.getConfig().put("optimize", "true");

            JmmSemanticsResult semanticResultsOptimized = semanticResults;

            System.out.println("-----------------------Separação do normal com as otimizações-----------------------");

            optimization.optimize(semanticResults);

            System.out.println("depois da optimization: " + semanticResults.getConfig());
            System.out.println(semanticResultsOptimized);



            OllirResult ollirResult = optimization.toOllir(semanticAnalysis);




            //Instantiate JasminBackender
            var jasminBackend = new JasminBackender();

            var jasminResult = jasminBackend.toJasmin(ollirResult);

            // Generate .class file
            jasminResult.compile(new File("jasminResult"));

            // Run .class file
            jasminResult.run();

        }

        //JmmSemanticsResult semanticAnalysis = parserResult;

        //fazer um for para correr os reports






        // Check if there are parsing errors
        //





        // ... add remaining stages
    }

    private static Map<String, String> parseArgs(String[] args) {
        SpecsLogs.info("Executing with args: " + Arrays.toString(args));

        // Check if there is at least one argument
        if (args.length != 1) {
            throw new RuntimeException("Expected a single argument, a path to an existing input file.");
        }

        // Create config
        Map<String, String> config = new HashMap<>();
        config.put("inputFile", args[0]);
        config.put("optimize", "false");
        config.put("registerAllocation", "-1");
        config.put("debug", "false");

        for(int i = 0; i < args.length; i++){
            String arg = args[i];
            if(arg.contains("-o")){
                config.put("optimize", "true");
            }
        }

        return config;
    }

}
