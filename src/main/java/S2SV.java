import java.io.InputStream;
import java.util.ArrayList;

import IR.SparrowParser;
import IR.visitor.SparrowConstructor;
import IR.syntaxtree.Node;

import sparrow.visitor.ArgVisitor;

import sparrowUtils.*;
import sparrowv.*;


public class S2SV {
    sparrow.Program program;

    public S2SV(sparrow.Program p){
        program = p;
    }

    // Part 1: liveness analysis
    // put each line into a vector
    
    public void runLivenessAnalysis(){
        analyze();
    }

    public void analyze() {
        ArrayList<InstrsData> instrsData = new ArrayList<>();
        LAVisitor livenessAnalysisVisitor = new LAVisitor();
    
        program.accept((ArgVisitor<ArrayList<InstrsData>>) livenessAnalysisVisitor, instrsData);
    }

    // maintain
    // define a vector of <instrs, def, use, in, and out bitvectors>
    // order these two vectors by first definition
        // def bitvector
        // use bitvector
        // use visitor pattern
    // for each line
        // populate def/use bitvectors left to right, in order

    // maintain a jump table or CFG for gotos
    // for each line
        // init a
        // in bitvector
        // out bitvector
    // iterate through until no changes
    // in and out bitvectors are now complete

    // Part 2: Linear Scan Register Allocation
    // take those vectors and complete linear scan

    public static void main(String [] args) throws Exception {
        InputStream in = System.in;
        new SparrowParser(in);
        Node root = SparrowParser.Program();
        SparrowConstructor constructor = new SparrowConstructor();
        root.accept(constructor);
        sparrow.Program program = constructor.getProgram();
        // System.err.println(program.toString());
    } 
}