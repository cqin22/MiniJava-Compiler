import java.io.InputStream;
import java.util.*;

import IR.SparrowParser;
import IR.visitor.SparrowConstructor;
import IR.syntaxtree.Node;

import sparrow.visitor.*;

import sparrowUtils.*;
import sparrow.*;


public class S2SV {
    sparrow.Program program;
    Algorithms algorithms;
    // create a list of Interval objects with all relevant information
    ArrayList<ArrayList<InstrsData>> functionList = new ArrayList<>();
    ArrayList<HashMap<Integer, String>> bitToRegMap;
    ArrayList<ArrayList<Interval>> intervalLists;

    int stackCount;

    public S2SV(){
    }
    // Part 1: liveness analysis
    // put each line into a vector

    public void runS2SV(sparrow.Program p){
        program = p;
        algorithms = new Algorithms(program);

        runLivenessAnalysis();
        // algorithms.printFunctionList(functionList);

        ArrayList<String> allocTable = new ArrayList<>(List.of(
            "a2", "a3", "a4", "a5", "a6", "a7",
            "s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11",
            "t2", "t3", "t4", "t5"
        ));
        runLinearScanAlgorithm(allocTable);

        runTranslation();
    }
    
    public void runLivenessAnalysis(){
        bitToRegMap = algorithms.createDefUse(functionList);

        for(int i = 0; i < functionList.size(); i++){
            algorithms.livenessAnalysisAlgorithm(functionList.get(i));
        }
        // algorithms.printFunctionList(functionList);
    }

    public void runTranslation(){
        TranslationVisitor translationVisitor = new TranslationVisitor(intervalLists);
        program.accept((ArgVisitor<InstrsData>) translationVisitor, null);
        List<sparrowv.FunctionDecl> functionDecl = translationVisitor.getFunctionDeclarations();
        sparrowv.Program sparrowProgram = new sparrowv.Program(functionDecl);
        System.out.println(sparrowProgram.toString());
    }

    // active= max heap sorted by length of interval
    // availableRegister = list of registers -- how do you know how many?
    // endpoint = map of endpoint given interval
    // startpoint = map of startpoint given interval
    // location = map interval to stack slot -- how to calculate this?
    // register = map interval to register
    public void runLinearScanAlgorithm(ArrayList<String> allocTable) {
        int R = allocTable.size();
        ArrayList<String> staticAllocTable = new ArrayList<>(allocTable);
        Comparator<Interval> intervalComparator = (i1, i2) -> Integer.compare(i2.endPoint, i1.endPoint);

        intervalLists = algorithms.createIntervalLists(functionList, bitToRegMap);
    
        for (int i = 0; i < intervalLists.size(); i++) {
            // System.err.println("=== Allocating for Function " + i + " ===");
    
            ArrayList<Interval> intervalList = intervalLists.get(i);
            PriorityQueue<Interval> maxHeapInterval = new PriorityQueue<>(intervalComparator);
            stackCount = 0;
            allocTable = new ArrayList<>(staticAllocTable);

            for (int j = 0; j < intervalList.size(); j++) {

                Interval interval = intervalList.get(j);
    
                // System.err.println("\n>> Processing Interval " + j + " [" + interval.startPoint + ", " + interval.endPoint + "]");
    
                // System.err.println("   Available Registers: " + allocTable);
                // System.err.print("   Active Intervals: ");
                for (Interval active : maxHeapInterval) {
                    // System.err.print("[" + active.startPoint + "," + active.endPoint + "] ");
                }
                // System.err.println();
    
                expireOldIntervals(interval, maxHeapInterval, allocTable);
    
                // System.err.println("maxHeapInterval.size(): " + maxHeapInterval.size());
                if (maxHeapInterval.size() == R) {
                    // System.err.println("   >> No available registers. Spilling interval " + j);
                    spillAtInterval(interval, maxHeapInterval, allocTable);
                } else {
                    String reg = allocTable.remove(0);
                    interval.register = reg;
                    maxHeapInterval.add(interval);

                    // System.err.println("   >> Assigned register '" + reg + "' to interval " + j);
                }
            }
        }
    
        // System.err.println("\n=== Final Interval Allocation ===");
        algorithms.printIntervalLists(intervalLists);
    }

    public void expireOldIntervals(Interval i, PriorityQueue<Interval> maxHeapInterval, ArrayList<String> allocTable){
        // System.err.println(">> Expiring old intervals for interval [" + i.startPoint + ", " + i.endPoint + "]");
        List<Interval> intervalsToExpire = new ArrayList<>(maxHeapInterval);
        for (int j = intervalsToExpire.size() - 1; j >= 0; j--) {
            Interval interval = intervalsToExpire.get(j);

            // System.err.println("   Checking active interval [" + interval.startPoint + ", " + interval.endPoint + "]");
            if (interval.endPoint >= i.startPoint) {
            // System.err.println("   >> Interval [" + interval.startPoint + ", " + interval.endPoint + "] is still active.");
            return;
            }

            // System.err.println("   >> Expiring interval [" + interval.startPoint + ", " + interval.endPoint + "]");
            maxHeapInterval.remove(interval);

            allocTable.add(interval.register);
            // System.err.println("   >> Register '" + interval.register + "' returned to available pool.");
        }
    }

    public void spillAtInterval(Interval i, PriorityQueue<Interval> maxHeapInterval, List<String> allocTable){
        // System.err.println(">> Spilling interval [" + i.startPoint + ", " + i.endPoint + "]");
        Interval spill = maxHeapInterval.peek();

        if(spill.endPoint > i.endPoint){
            // System.err.println("   >> Spilling active interval [" + spill.startPoint + ", " + spill.endPoint + "] to make room for [" + i.startPoint + ", " + i.endPoint + "]");
            i.register = spill.register;

            spill.stackCount = stackCount;
            spill.register = null; // Mark the spilled interval as not having a register

            // System.err.println("   >> Interval [" + spill.startPoint + ", " + spill.endPoint + "] spilled to stack slot " + stackCount);
            stackCount++;

            maxHeapInterval.remove(spill);
            maxHeapInterval.add(i);
            // System.err.println("   >> Assigned register '" + i.register + "' to interval [" + i.startPoint + ", " + i.endPoint + "]");
        }
        else{
            // System.err.println("   >> Spilling current interval [" + i.startPoint + ", " + i.endPoint + "] to stack slot " + stackCount);
            i.register = null;
            i.stackCount = stackCount;

            stackCount++;
        }
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

        // // System.err.println(program.toString());
    } 
}