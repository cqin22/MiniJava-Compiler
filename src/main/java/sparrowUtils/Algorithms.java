package sparrowUtils;

import java.util.*;

import sparrow.visitor.*;

public class Algorithms {
    sparrow.Program program;
    ArrayList<ArrayList<InstrsData>> functionList;

    public Algorithms(sparrow.Program p, ArrayList<ArrayList<InstrsData>> fl){
        program = p;
        functionList = fl;
    }
    // order these two vectors by first definition
    // def bitvector
    // use bitvector
    // use visitor pattern
    // for each line
        // populate def/use bitvectors left to right, in order
    public void createDefUse() {
        // maintain
        // define a vector of <instrs, def, use, in, and out bitvectors>
        HashMap<String, InstrsData> instrsMap = new HashMap<>();
        FirstPassLAVisitor firstPassLAVisitor = new FirstPassLAVisitor(instrsMap);
        program.accept((ArgVisitor<InstrsData>) firstPassLAVisitor, null);

        LAVisitor livenessAnalysisVisitor = new LAVisitor(functionList, instrsMap);
        program.accept((ArgVisitor<InstrsData>) livenessAnalysisVisitor, null);
    }


    // maintain a jump table or CFG for gotos
    // for each line
        // init a
        // in bitvector
        // out bitvector
    // iterate through until no changes
    // in and out bitvectors are now complete

    public void livenessAnalysisAlgorithm(ArrayList<InstrsData> instrsDataList){
        boolean changing = true;

        ArrayList<BitSet> groundIn = new ArrayList<>();
        ArrayList<BitSet> groundOut = new ArrayList<>();
        for (int i = 0; i < instrsDataList.size(); i++) {
            groundIn.add(new BitSet());
            groundOut.add(new BitSet());
        }

        while(changing){
            changing = false;
            // process in
            for(int i = 0; i < instrsDataList.size(); i++){
                InstrsData instrsData = instrsDataList.get(i);
                BitSet tempOut = (BitSet) instrsData.out.clone();
                tempOut.andNot(instrsData.def);
                BitSet tempIn = (BitSet) instrsData.use.clone();
                tempIn.or(tempOut);
                instrsData.in = tempIn;
                
                // check if changed
                if(!instrsData.in.equals(groundIn.get(i))){
                    changing = true;
                }

                groundIn.set(i, instrsData.in);
            }

            // process out
            for(int i = 0; i < instrsDataList.size(); i++){
                InstrsData instrsData = instrsDataList.get(i);
                BitSet tempOut = new BitSet();

                // add jump node
                if(instrsData.goesTo != -1){
                    BitSet jumpedNodeIn = instrsDataList.get(instrsData.goesTo).in;
                    System.err.println("jumpednodein:  " + jumpedNodeIn.toString());
                    tempOut.or(jumpedNodeIn);
                }

                // return's out is neglected
                if(i < instrsDataList.size() - 1){
                    InstrsData successor = instrsDataList.get(i + 1);
                    tempOut.or(successor.in);
                }

                instrsData.out = tempOut;

                // check if changed
                if(!instrsData.out.equals(groundOut.get(i))){
                    changing = true;
                }

                groundOut.set(i, instrsData.out);
            }
            printFunctionList();

        }
    }

    public void printFunctionList() {
        for (int i = 0; i < functionList.size(); i++) {
            System.err.println("Function " + i + ":");
            ArrayList<InstrsData> instrsDataList = functionList.get(i);
            for (int j = 0; j < instrsDataList.size(); j++) {
                InstrsData instrsData = instrsDataList.get(j);
                System.err.println("  Instruction " + j + ":");
                System.err.println("    Def: " + instrsData.def);
                System.err.println("    Use: " + instrsData.use);
                System.err.println("    In: " + instrsData.in);
                System.err.println("    Out: " + instrsData.out);
                System.err.println("    GoesTo: " + instrsData.goesTo);
            }
        }
    }
}
