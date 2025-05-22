import java.io.InputStream;
import java.util.*;

import IR.SparrowParser;
import IR.visitor.SparrowConstructor;
import IR.syntaxtree.Node;

import sparrow.visitor.ArgVisitor;

import sparrowUtils.*;
import sparrow.*;



public class S2SV {
    sparrow.Program program;
    ArrayList<ArrayList<InstrsData>> functionList = new ArrayList<>();

    public S2SV(){
    }
    // Part 1: liveness analysis
    // put each line into a vector

    public void runS2SV(sparrow.Program p){
        program = p;
        runLivenessAnalysis();
        runLinearScanAlgorithm();
    }
    
    public void runLivenessAnalysis(){
        Algorithms algorithms = new Algorithms(program, functionList);
        algorithms.createDefUse();
        algorithms.printFunctionList();

        for(int i = 0; i < functionList.size(); i++){
            algorithms.livenessAnalysisAlgorithm(functionList.get(i));
        }
    }

    public void runLinearScanAlgorithm(){
        
    }

    // Part 2: Linear Scan Register Allocation
    // take those vectors and complete linear scan

    public static void main(String [] args) throws Exception {
        InputStream in = System.in;
        new SparrowParser(in);
        Node root = SparrowParser.Program();
        SparrowConstructor constructor = new SparrowConstructor();
        root.accept(constructor);

        sparrow.Program program = constructor.getProgram();

        S2SV s2sv = new S2SV();
        s2sv.runS2SV(program);
        
        // System.err.println(program.toString());
    } 
}