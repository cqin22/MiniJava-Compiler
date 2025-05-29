package sparrowUtils;
import java.lang.reflect.Array;

import sparrow.*;
import sparrow.visitor.*;

import java.util.*;

import IR.token.*;

public class LAVisitor implements ArgVisitor<InstrsData>
{
    HashMap<String, Integer> regToBitMap = new HashMap<>();
    ArrayList<HashMap<Integer, String>> bitToRegMaps = new ArrayList<>();

    int bitIndex = 0;

    ArrayList<InstrsData> instrsDataList;
    ArrayList<ArrayList<InstrsData>> functionList;
    HashMap<String, InstrsData> instrsMap;
    int instrsLineIndex = 0;

    public LAVisitor(ArrayList<ArrayList<InstrsData>> fl, HashMap<String, InstrsData> im){
        functionList = fl;
        instrsMap = im;
    }

    public ArrayList<HashMap<Integer, String>> getBitToRegMaps() {
        return bitToRegMaps;
    }

    public HashMap<Integer, String> reverseRegToBitMap() {
        HashMap<Integer, String> reversedMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : regToBitMap.entrySet()) {
            reversedMap.put(entry.getValue(), entry.getKey());
        }
        return reversedMap;
    }

    /*   List<FunctionDecl> funDecls; */
    @Override
    public void visit(Program n, InstrsData x) {
        for (FunctionDecl fd: n.funDecls) {
            
            fd.accept(this, null);
        }
    }

    /*   Program parent;
     *   FunctionName functionName;
     *   List<Identifier> formalParameters;
     *   Block block; */
    @Override
    public void visit(FunctionDecl n, InstrsData x) {
        instrsDataList = new ArrayList<>();
        regToBitMap.clear();
        bitIndex = 0;
        // TODO: instrsMap.clear();
        instrsLineIndex = 0;
        InstrsData funcParam = new InstrsData();


        for (Identifier fp: n.formalParameters) {
            funcParam.def.set(defineBit(fp));
        }

        instrsDataList.add(funcParam);
        n.block.accept(this, null);

        String funcName = n.functionName.toString();
        bitToRegMaps.add(reverseRegToBitMap());
    }

    /*   FunctionDecl parent;
     *   List<Instruction> instructions;
     *   Identifier return_id; */
    @Override
    public void visit(Block n, InstrsData x) {
        // reset liveness anlaysis on the function level

        // perform LA on all instructions
        for (Instruction i: n.instructions) {
            InstrsData instrsData = new InstrsData();

            i.accept(this, instrsData);

            instrsData.index = instrsLineIndex;

            instrsDataList.add(instrsData);
            instrsLineIndex++;
        }

        // handle return instrs 
        InstrsData returnInstrs = new InstrsData();
        returnInstrs.index = instrsLineIndex;
        returnInstrs.use.set(useBit(n.return_id));
        instrsDataList.add(returnInstrs);
        instrsLineIndex++;

        functionList.add(instrsDataList);
    }

    // TODO: CFG or jump table
    /*   Label label; */
    @Override
    public void visit(LabelInstr n, InstrsData instrsData) {
        // see first pass
    }

    // TODO: functions
    /*   Identifier lhs;
     *   FunctionName rhs; */
    @Override
    public void visit(Move_Id_FuncName n, InstrsData instrsData) {
        instrsData.def.set(defineBit(n.lhs));
    }

    /*   Identifier lhs;
     *   Identifier arg1;
     *   Identifier arg2; */
    @Override
    public void visit(Add n, InstrsData instrsData) {
        instrsData.def.set(defineBit(n.lhs));
        instrsData.use.set(useBit(n.arg1));
        instrsData.use.set(useBit(n.arg2));
    }

    /*   Identifier lhs;
     *   Identifier arg1;
     *   Identifier arg2; */
    @Override
    public void visit(Subtract n, InstrsData instrsData) {
        instrsData.def.set(defineBit(n.lhs));
        instrsData.use.set(useBit(n.arg1));
        instrsData.use.set(useBit(n.arg2));
    }

    /*   Identifier lhs;
     *   Identifier arg1;
     *   Identifier arg2; */
    @Override
    public void visit(Multiply n, InstrsData instrsData) {
        // Implementation here
        instrsData.def.set(defineBit(n.lhs));
        instrsData.use.set(useBit(n.arg1));
        instrsData.use.set(useBit(n.arg2));
    }

    /*   Identifier lhs;
     *   Identifier arg1;
     *   Identifier arg2; */
    @Override
    public void visit(LessThan n, InstrsData instrsData) {
        // Implementation here
        instrsData.def.set(defineBit(n.lhs));
        instrsData.use.set(useBit(n.arg1));
        instrsData.use.set(useBit(n.arg2));
    }

    /*   Identifier lhs;
     *   Identifier base;
     *   int offset; */
    @Override
    public void visit(Load n, InstrsData instrsData) {
        // Implementation here
        instrsData.def.set(defineBit(n.lhs));
        instrsData.use.set(useBit(n.base));
    }

    /*   Identifier base;
     *   int offset;
     *   Identifier rhs; */
    @Override
    public void visit(Store n, InstrsData instrsData) {
        // Implementation here
        instrsData.use.set(useBit(n.base));
        instrsData.use.set(useBit(n.rhs));
    }

    /*   Identifier lhs;
     *   Identifier rhs; */
    @Override
    public void visit(Move_Id_Id n, InstrsData instrsData) {
        // Implementation here
        instrsData.def.set(defineBit(n.lhs));
        instrsData.use.set(useBit(n.rhs));
    }
    /*   Identifier lhs;
     *   Identifier size; */
    @Override
    public void visit(Alloc n, InstrsData instrsData) {
        // Implementation here
        instrsData.def.set(defineBit(n.lhs));
    }

    /*   Identifier lhs;
     *   int value; */
    @Override
    public void visit(Move_Id_Integer n, InstrsData instrsData) {
        // Implementation here
        instrsData.def.set(defineBit(n.lhs));
    }

    // TODO: identifier
    /*   Identifier content; */
    @Override
    public void visit(Print n, InstrsData instrsData) {
        instrsData.use.set(useBit(n.content));
    }

    // TODO: identifier
    /*   String msg; */
    @Override
    public void visit(ErrorMessage n, InstrsData instrsData) {
        // Implementation here
    }

    /*   Label label; */
    @Override
    public void visit(Goto n, InstrsData instrsData) {
        InstrsData labelInstrsData = instrsMap.get(n.label.toString());
        instrsData.goesTo = labelInstrsData.index + 1; // off by 1
    }

    // TODO: create CFG or jump table
    /*   Identifier condition;
     *   Label label; */
    @Override
    public void visit(IfGoto n, InstrsData instrsData) {
        InstrsData labelInstrsData = instrsMap.get(n.label.toString());
        instrsData.goesTo = labelInstrsData.index + 1; // off by 1
        instrsData.use.set(useBit(n.condition));
    }

    /*   Identifier lhs;
     *   Identifier callee;
     *   List<Identifier> args; */
    @Override
    public void visit(Call n, InstrsData instrsData) {
        instrsData.def.set(defineBit(n.lhs));
        instrsData.use.set(useBit(n.callee));
        for(int i = 0; i < n.args.size(); i++){
            instrsData.use.set(useBit(n.args.get(i)));
        }
    }

    public int defineBit(Identifier r){
        String r_string = r.toString();
        if(!regToBitMap.containsKey(r_string)){
            regToBitMap.put(r_string, bitIndex);
            bitIndex++;
        }

        return regToBitMap.get(r_string);
    }

    public int useBit(Identifier r){
        String r_string = r.toString();
        if(!regToBitMap.containsKey(r_string)){
        }

        return regToBitMap.get(r_string);
    }
}