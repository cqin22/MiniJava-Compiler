package sparrowUtils;

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

    // Reserve only t0 and t1 as temp registers
    private final Register t0 = new Register("t0");
    private final Register t1 = new Register("t1");

    public List<sparrowv.FunctionDecl> getFunctionDeclarations() { return functionDecls; }
    Identifier return_id = new Identifier(null);

    @Override
    public void visit(Program n, InstrsData instrsData) {
        for (FunctionDecl fd : n.funDecls) {
            fd.accept(this, instrsData);
        }
    }

    @Override
    public void visit(FunctionDecl n, InstrsData instrsData) {
        instrs.clear();
        n.block.accept(this, instrsData);
        sparrowv.Block block = new sparrowv.Block(new ArrayList<>(instrs), return_id);
        functionDecls.add(new sparrowv.FunctionDecl(n.functionName, n.formalParameters, block));
    }
    
    @Override
    public void visit(Block n, InstrsData instrsData) {
        for (Instruction i : n.instructions) {
            i.accept(this, instrsData);
        }
        return_id = n.return_id;
    }

    @Override public void visit(LabelInstr n, InstrsData instrsData) {
        instrs.add(new sparrowv.LabelInstr(n.label));
    }

    @Override public void visit(Add n, InstrsData instrsData) {
        instrs.add(new Move_Reg_Id(t0, n.arg1));
        instrs.add(new Move_Reg_Id(t1, n.arg2));
        instrs.add(new sparrowv.Add(t0, t0, t1));
        instrs.add(new Move_Id_Reg(n.lhs, t0));
    }

    @Override public void visit(Subtract n, InstrsData instrsData) {
        instrs.add(new Move_Reg_Id(t0, n.arg1));
        instrs.add(new Move_Reg_Id(t1, n.arg2));
        instrs.add(new sparrowv.Subtract(t0, t0, t1));
        instrs.add(new Move_Id_Reg(n.lhs, t0));
    }

    @Override public void visit(Multiply n, InstrsData instrsData) {
        instrs.add(new Move_Reg_Id(t0, n.arg1));
        instrs.add(new Move_Reg_Id(t1, n.arg2));
        instrs.add(new sparrowv.Multiply(t0, t0, t1));
        instrs.add(new Move_Id_Reg(n.lhs, t0));
    }

    @Override public void visit(LessThan n, InstrsData instrsData) {
        instrs.add(new Move_Reg_Id(t0, n.arg1));
        instrs.add(new Move_Reg_Id(t1, n.arg2));
        instrs.add(new sparrowv.LessThan(t0, t0, t1));
        instrs.add(new Move_Id_Reg(n.lhs, t0));
    }

    @Override public void visit(Load n, InstrsData instrsData) {
        instrs.add(new Move_Reg_Id(t0, n.base));
        instrs.add(new sparrowv.Load(t1, t0, n.offset));
        instrs.add(new Move_Id_Reg(n.lhs, t1));
    }

    @Override public void visit(Store n, InstrsData instrsData) {
        instrs.add(new Move_Reg_Id(t0, n.base));
        instrs.add(new Move_Reg_Id(t1, n.rhs));
        instrs.add(new sparrowv.Store(t0, n.offset, t1));
    }

    @Override public void visit(Move_Id_Id n, InstrsData instrsData) {
        instrs.add(new Move_Reg_Id(t0, n.rhs));
        instrs.add(new Move_Id_Reg(n.lhs, t0));
    }

    @Override public void visit(Move_Id_FuncName n, InstrsData instrsData) {
        instrs.add(new Move_Reg_FuncName(t0, n.rhs));
        instrs.add(new Move_Id_Reg(n.lhs, t0));
    }

    @Override public void visit(Move_Id_Integer n, InstrsData instrsData) {
        instrs.add(new Move_Reg_Integer(t0, n.rhs));
        instrs.add(new Move_Id_Reg(n.lhs, t0));
    }

    @Override public void visit(Alloc n, InstrsData instrsData) {
        instrs.add(new Move_Reg_Id(t0, n.size));
        instrs.add(new sparrowv.Alloc(t1, t0));
        instrs.add(new Move_Id_Reg(n.lhs, t1));
    }

    @Override public void visit(Print n, InstrsData instrsData) {
        instrs.add(new Move_Reg_Id(t0, n.content));
        instrs.add(new sparrowv.Print(t0));
    }

    @Override public void visit(ErrorMessage n, InstrsData instrsData) {
        instrs.add(new sparrowv.ErrorMessage(n.msg));
    }

    @Override public void visit(Goto n, InstrsData instrsData) {
        instrs.add(new sparrowv.Goto(n.label));
    }

    @Override public void visit(IfGoto n, InstrsData instrsData) {
        instrs.add(new Move_Reg_Id(t0, n.condition));
        instrs.add(new sparrowv.IfGoto(t0, n.label));
    }

    @Override
    public void visit(Call n, InstrsData instrsData) {
        instrs.add(new Move_Reg_Id(t0, n.callee));
        instrs.add(new sparrowv.Call(t1, t0, n.args));
        instrs.add(new Move_Id_Reg(n.lhs, t1));
    }
}
