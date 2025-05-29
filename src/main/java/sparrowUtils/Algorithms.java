package sparrowUtils;

import java.util.*;

import sparrow.visitor.*;

public class Algorithms {
    sparrow.Program program;

    public Algorithms(sparrow.Program p){
        program = p;
    }
    // order these two vectors by first definition
    // def bitvector
    // use bitvector
    // use visitor pattern
    // for each line
        // populate def/use bitvectors left to right, in order
    public ArrayList<HashMap<Integer, String>> createDefUse(ArrayList<ArrayList<InstrsData>> functionList) {
        // maintain
        // define a vector of <instrs, def, use, in, and out bitvectors>
        HashMap<String, HashMap<String, InstrsData>> instrsMap = new HashMap<>();
        FirstPassLAVisitor firstPassLAVisitor = new FirstPassLAVisitor(instrsMap);
        program.accept((ArgVisitor<InstrsData>) firstPassLAVisitor, null);

        LAVisitor livenessAnalysisVisitor = new LAVisitor(functionList, instrsMap);
        program.accept((ArgVisitor<InstrsData>) livenessAnalysisVisitor, null);

        ArrayList<HashMap<Integer, String>> bitToRegMap = livenessAnalysisVisitor.getBitToRegMaps();
        // printBitToRegMap(bitToRegMap);

        return bitToRegMap;
    }

    public void printBitToRegMap(ArrayList<HashMap<Integer, String>> bitToRegMap) {
        // System.err.println("Bit to Register Mapping:");
        for (int i = 0; i < bitToRegMap.size(); i++) {
            HashMap<Integer, String> mapping = bitToRegMap.get(i);
            // System.err.println("Function " + i + ":");
            for (Map.Entry<Integer, String> mapEntry : mapping.entrySet()) {
                // System.err.println("  Bit: " + mapEntry.getKey() + " -> Register: " + mapEntry.getValue());
            }
        }
    }

    public void printRegToBitMap(HashMap<String, Integer> regToBitMap) {
        // System.err.println("Register to Bit Mapping:");
        for (Map.Entry<String, Integer> entry : regToBitMap.entrySet()) {
            // System.err.println("Register: " + entry.getKey() + " -> Bit: " + entry.getValue());
        }
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

                if (instrsData.goesTo != -1) {
                    if (instrsData.goesTo >= 0 && instrsData.goesTo < instrsDataList.size()) {
                        BitSet jumpedNodeIn = instrsDataList.get(instrsData.goesTo).in;
                        tempOut.or(jumpedNodeIn);
                    } else {
                        System.err.println("Invalid 'goesTo' index at instruction " + i + ": " + instrsData.goesTo);
                    }
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

        }
    }

    public void printFunctionList(ArrayList<ArrayList<InstrsData>> functionList) {
        for (int i = 0; i < functionList.size(); i++) {
            // System.err.println("Function " + i + ":");
            ArrayList<InstrsData> instrsDataList = functionList.get(i);
            for (int j = 0; j < instrsDataList.size(); j++) {
                InstrsData instrsData = instrsDataList.get(j);
                // System.err.println("  Instruction " + j + ":");
                // System.err.println("    Def: " + instrsData.def);
                // System.err.println("    Use: " + instrsData.use);
                // System.err.println("    In: " + instrsData.in);
                // System.err.println("    Out: " + instrsData.out);
                // System.err.println("    GoesTo: " + instrsData.goesTo);
            }
        }
    }

    public ArrayList<ArrayList<Interval>> createIntervalLists(
    ArrayList<ArrayList<InstrsData>> functionList,
    ArrayList<HashMap<Integer, String>> bitToRegMaps
    ) {
        ArrayList<ArrayList<Interval>> intervalLists = new ArrayList<>();

        for (int i = 0; i < functionList.size(); i++) {
            ArrayList<InstrsData> instrsDataList = functionList.get(i);
            HashMap<Integer, String> bitToRegMap = bitToRegMaps.get(i);

            int intervalListSize = bitToRegMap.size();
            ArrayList<Interval> intervalList = new ArrayList<>();

            // Initialize intervalList with one Interval per bit index
            for (int k = 0; k < intervalListSize; k++) {
                intervalList.add(new Interval());
            }

            // Scan each instruction
            for (int j = 0; j < instrsDataList.size(); j++) {
                InstrsData instrsData = instrsDataList.get(j);
                BitSet def = instrsData.def;
                BitSet out = instrsData.out;

                // Process in-set: variable must be live at this instruction
                int bitIndex = def.nextSetBit(0);
                while (bitIndex >= 0) {
                    Interval interval = intervalList.get(bitIndex);
                    if (interval.startPoint == -1) {
                        interval.startPoint = j;
                    }
                    interval.endPoint = Math.max(interval.endPoint, j);
                    bitIndex = def.nextSetBit(bitIndex + 1);
                }

                // Process out-set: variable must be live after this instruction
                bitIndex = out.nextSetBit(0);
                while (bitIndex >= 0) {
                    Interval interval = intervalList.get(bitIndex);
                    // if (interval.startPoint == -1) {
                    //     interval.startPoint = j;
                    // }
                    if(instrsData.goesTo == -1){
                        interval.endPoint = Math.max(interval.endPoint, j) + 1;
                    }
                    else{
                        interval.endPoint = Math.max(interval.endPoint, j);
                    }

                    bitIndex = out.nextSetBit(bitIndex + 1);
                }
            }

            // off by one
            // for(int z = 0; z < intervalList.size(); z++){
            //     intervalList.get(z).endPoint++;
            // }

            intervalLists.add(intervalList);
        }

        // printIntervalLists(intervalLists);
        return intervalLists;
    }

    public void printInterval(Interval interval) {
        // System.err.println("Interval: Start = " + interval.startPoint + ", End = " + interval.endPoint);
    }

    public void printInstrsData(InstrsData instrsData) {
        // System.err.println("Instruction Data:");
        // System.err.println("  Def: " + instrsData.def);
        // System.err.println("  Use: " + instrsData.use);
        // System.err.println("  In: " + instrsData.in);
        // System.err.println("  Out: " + instrsData.out);
        // System.err.println("  GoesTo: " + instrsData.goesTo);

        // System.err.print("  Out Bits: ");
        int bitIndex = instrsData.out.nextSetBit(0);
        while (bitIndex >= 0) {
            // System.err.print(bitIndex + " ");
            bitIndex = instrsData.out.nextSetBit(bitIndex + 1);
        }
        // System.err.println();
    }
    public void printInstrsDataList(ArrayList<InstrsData> instrsDataList) {
        // System.err.println("Instructions Data List:");
        for (int i = 0; i < instrsDataList.size(); i++) {
            InstrsData instrsData = instrsDataList.get(i);
            // System.err.println("  Instruction " + i + ":");
            // System.err.println("    Def: " + instrsData.def);
            // System.err.println("    Use: " + instrsData.use);
            // System.err.println("    In: " + instrsData.in);
            // System.err.println("    Out: " + instrsData.out);
            // System.err.println("    GoesTo: " + instrsData.goesTo);

            // System.err.print("    Out Bits: ");
            int bitIndex = instrsData.out.nextSetBit(0);
            while (bitIndex >= 0) {
            // System.err.print(bitIndex + " ");
            bitIndex = instrsData.out.nextSetBit(bitIndex + 1);
            }
            // System.err.println();
        }
    }

    public void printIntervalLists(ArrayList<ArrayList<Interval>> intervalLists) {
        // System.err.println("Interval Lists:");
        for (int i = 0; i < intervalLists.size(); i++) {
            // System.err.println("Function " + i + ":");
            ArrayList<Interval> intervalList = intervalLists.get(i);
            for (int j = 0; j < intervalList.size(); j++) {
                Interval interval = intervalList.get(j);
                // System.err.println("  Interval " + j + ": Start = " + interval.startPoint + ", End = " + interval.endPoint + 
                                // ", Register = " + interval.register + 
                                //    ", Stack Count = " + interval.stackCount);
            }
        }
    }


}
