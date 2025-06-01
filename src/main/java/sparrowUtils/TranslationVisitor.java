package sparrowUtils;

import java.beans.IntrospectionException;

import sparrow.visitor.*;
import sparrowv.*;
import sparrow.*;
import sparrow.Add;
import sparrow.Alloc;
import sparrow.Block;
import sparrow.Call;
import sparrow.ErrorMessage;
import sparrow.FunctionDecl;
import sparrow.Goto;
import sparrow.IfGoto;
import sparrow.Instruction;
import sparrow.LabelInstr;
import sparrow.LessThan;
import sparrow.Load;
import sparrow.Multiply;
import sparrow.Print;
import sparrow.Program;
import sparrow.Store;
import sparrow.Subtract;

import java.util.*;

import IR.token.*;

public class TranslationVisitor implements ArgVisitor<InstrsData> {
    List<sparrowv.Instruction> instrs = new ArrayList<>();
    List<sparrowv.FunctionDecl> functionDecls = new ArrayList<>();
    public ArrayList<ArrayList<Interval>> intervalLists;

    int instrsIndex = 0;

    public TranslationVisitor(ArrayList<ArrayList<Interval>> il){
        intervalLists = il;
    }

    private final Register t0 = new Register("t0");
    private final Register t1 = new Register("t1");

    public List<sparrowv.FunctionDecl> getFunctionDeclarations() { return functionDecls; }
    Identifier return_id = new Identifier(null);
    Map<String, Register> cachedInRegister = new HashMap<>();


    private int getEndPointFromIdentifier(Identifier id) {
        for (Interval interval : intervalLists.get(functionDecls.size())) {
            if (id.toString().equals(interval.var)) {
                return interval.endPoint;
            }
        }
        return -1; // Return -1 if no matching interval is found
    }

    private Register getRegisterFromInterval(Identifier id) {
        for (Interval interval : intervalLists.get(functionDecls.size())) {
            if (interval.register != null && id.toString().equals(interval.var)) {
                return new Register(interval.register);
            }
        }
        return null;
    }

    private void loadRegister(Identifier id, boolean tzero) {
        Register t = tzero ? t0 : t1;
        for (Interval interval : intervalLists.get(functionDecls.size())) {
            if (interval.register != null && id.toString().equals(interval.var)) {
                instrs.add(new Move_Reg_Reg(t, new Register(interval.register)));
                return;
            }
        }
        instrs.add(new Move_Reg_Id(t, id));
    }

    private void storeRegister(Identifier id, boolean tzero) {
        Register t = tzero ? t0 : t1;
        for (Interval interval : intervalLists.get(functionDecls.size())) {
            if (interval.register != null && id.toString().equals(interval.var)) {
                instrs.add(new Move_Reg_Reg(new Register(interval.register), t));
                return;
            }
        }
        instrs.add(new Move_Id_Reg(id, t));
    }

    @Override
    public void visit(Program n, InstrsData instrsData) {
        for (FunctionDecl fd : n.funDecls) {
            fd.accept(this, instrsData);
        }
    }

    @Override
    public void visit(FunctionDecl n, InstrsData instrsData) {
        instrs.clear();
        instrsIndex = 0;

        // // Prologue: Save registers to the stack
        // for (Interval interval : intervalLists.get(functionDecls.size())) {
        //     if (interval.register != null) {
        //     instrs.add(new sparrowv.Move_Id_Reg(new Identifier("save_" + interval.register), new Register(interval.register)));
        //     }
        // }

        ArrayList<String> funcTable = new ArrayList<>(List.of(
            "a2", "a3", "a4", "a5", "a6", "a7"
        ));
        
        for (int i = 0; i < 6 && i < n.formalParameters.size(); i++) {
            Identifier fp = n.formalParameters.get(i);
            Register reg = getRegisterFromInterval(fp);
            if(reg == null){
                instrs.add(new sparrowv.Move_Id_Reg(fp, new Register(funcTable.remove(0))));
            }
            else{
                instrs.add(new sparrowv.Move_Reg_Reg(reg, new Register(funcTable.remove(0))));
            }
        }

        for (int i = 6; i < n.formalParameters.size(); i++) {
            Register reg = getRegisterFromInterval(n.formalParameters.get(i));
            if(reg != null){
                instrs.add(new sparrowv.Move_Reg_Id(reg, n.formalParameters.get(i)));
            }
        }


        n.block.accept(this, instrsData);
        Register return_reg = getRegisterFromInterval(return_id);
        if(return_reg != null){
            instrs.add(new sparrowv.Move_Id_Reg(return_id, return_reg));
        }
        // TODO:else
        else{
            loadRegister(return_id, true);
            instrs.add(new sparrowv.Move_Id_Reg(return_id, t0));
        }

        // // Epilogue: Restore registers from the stack
        // for (Interval interval : intervalLists.get(functionDecls.size())) {
        //     if (interval.register != null) {
        //     instrs.add(new sparrowv.Move_Reg_Id(new Register(interval.register), new Identifier("save_" + interval.register)));
        //     }
        // }

        List<Identifier> functionParams = new ArrayList<>();
        for(int i = 0; i < n.formalParameters.size(); i++){
            if(i > 5){
                functionParams.add(n.formalParameters.get(i));
            }
        }
        sparrowv.Block block = new sparrowv.Block(new ArrayList<>(instrs), return_id);
        functionDecls.add(new sparrowv.FunctionDecl(n.functionName, functionParams, block));
    }

    @Override
    public void visit(Block n, InstrsData instrsData) {
        for (Instruction i : n.instructions) {
            i.accept(this, instrsData);
            instrsIndex++;
        }
        return_id = n.return_id;
        instrsIndex++;
    }

    @Override public void visit(LabelInstr n, InstrsData instrsData) {
        instrs.add(new sparrowv.LabelInstr(n.label));
    }

    @Override public void visit(Add n, InstrsData instrsData) {
        Register r1 = getRegisterFromInterval(n.arg1);
        Register r2 = getRegisterFromInterval(n.arg2);
        Register dst = getRegisterFromInterval(n.lhs);

        // load spilled arg1 → t0 (if needed)
        if (r1 == null) {
            loadRegister(n.arg1, true);   // t0
            r1 = t0;
        }

        // load spilled arg2 → choose t1 if t0 already in use, else t0
        boolean t0Used = r1 == t0;
        if (r2 == null) {
            loadRegister(n.arg2, !t0Used);           // t1 if t0Used else t0
            r2 = t0Used ? t1 : t0;
        }

        if (dst != null) {
            // result already has a physical register
            instrs.add(new sparrowv.Add(dst, r1, r2));
        } else {
            // compute into t0 and spill back
            instrs.add(new sparrowv.Add(t0, r1, r2));
            storeRegister(n.lhs, true);
        }
    }

    @Override public void visit(Subtract n, InstrsData instrsData) {
        loadRegister(n.arg1, true);
        loadRegister(n.arg2, false);
        instrs.add(new sparrowv.Subtract(t0, t0, t1));
        storeRegister(n.lhs, true);
    }

    @Override public void visit(Multiply n, InstrsData instrsData) {
        loadRegister(n.arg1, true);
        loadRegister(n.arg2, false);
        instrs.add(new sparrowv.Multiply(t0, t0, t1));
        storeRegister(n.lhs, true);
    }

    @Override public void visit(LessThan n, InstrsData instrsData) {
        loadRegister(n.arg1, true);
        loadRegister(n.arg2, false);
        instrs.add(new sparrowv.LessThan(t0, t0, t1));
        storeRegister(n.lhs, true);
    }

    @Override public void visit(Load n, InstrsData instrsData) {
        loadRegister(n.base, true);
        instrs.add(new sparrowv.Load(t1, t0, n.offset));
        storeRegister(n.lhs, false);
    }

    @Override public void visit(Store n, InstrsData instrsData) {
        loadRegister(n.base, true);
        loadRegister(n.rhs, false);
        instrs.add(new sparrowv.Store(t0, n.offset, t1));
    }

    @Override public void visit(Move_Id_Id n, InstrsData instrsData) {
        loadRegister(n.rhs, false);
        storeRegister(n.lhs, false);
    }

    @Override public void visit(Move_Id_FuncName n, InstrsData instrsData) {
        instrs.add(new Move_Reg_FuncName(t0, n.rhs));
        storeRegister(n.lhs, true);
    }

    @Override public void visit(Move_Id_Integer n, InstrsData instrsData) {
        instrs.add(new Move_Reg_Integer(t0, n.rhs));
        storeRegister(n.lhs, true);
    }

    @Override public void visit(Alloc n, InstrsData instrsData) {
        loadRegister(n.size, true);
        instrs.add(new sparrowv.Alloc(t1, t0));
        storeRegister(n.lhs, false);
    }

    @Override public void visit(Print n, InstrsData instrsData) {
        loadRegister(n.content, true);
        instrs.add(new sparrowv.Print(t0));
    }

    @Override public void visit(ErrorMessage n, InstrsData instrsData) {
        instrs.add(new sparrowv.ErrorMessage(n.msg));
    }

    @Override public void visit(Goto n, InstrsData instrsData) {
        instrs.add(new sparrowv.Goto(n.label));
    }

    @Override public void visit(IfGoto n, InstrsData instrsData) {
        loadRegister(n.condition, true);
        instrs.add(new sparrowv.IfGoto(t0, n.label));
    }


    @Override
    public void visit(Call n, InstrsData instrsData) {


        List<Identifier> newArgs = new ArrayList<>();

        ArrayList<String> funcTable = new ArrayList<>(List.of(
            "a2", "a3", "a4", "a5", "a6", "a7"
        ));

        int i = 0;
        for (Identifier id : n.args) {
            if(i < 6){
                String a = funcTable.remove(0);
                loadRegister(id, true);

                instrs.add(new Move_Reg_Reg(new Register(a), t0));

                i++;
                continue;
            }
            loadRegister(id, true);
            Identifier tmp = new Identifier("temp_" + id.toString());
            instrs.add(new sparrowv.Move_Id_Reg(tmp, t0));
            newArgs.add(tmp);
        }

        int callIndex = instrsIndex; // ← this is the actual line of the Call instruction

        Set<String> liveOutRegisters = new HashSet<>();
        for (Interval interval : intervalLists.get(functionDecls.size())) {
            if (interval.register != null &&
                interval.startPoint < callIndex && interval.endPoint > callIndex) {
                // This register is live *across* the call instruction at callIndex
                liveOutRegisters.add(interval.register);
                instrs.add(new sparrowv.Move_Id_Reg(
                    new Identifier("save_" + interval.register),
                    new Register(interval.register)
                ));
            }
        }
        
        // Load callee and arguments
        loadRegister(n.callee, false);
        instrs.add(new sparrowv.Call(t0, t1, newArgs));

        // Restore live-out t registers after the call
        for (String reg : liveOutRegisters) {
            instrs.add(new sparrowv.Move_Reg_Id(new Register(reg), new Identifier("save_" + reg)));
        }

        // Store the result of the call
        storeRegister(n.lhs, true);
    }
}