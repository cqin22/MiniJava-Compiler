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

    ArrayList<ArrayList<Interval>> intervalLists;

    int instrsLineIndex = 0;

    public List<sparrowv.FunctionDecl> getFunctionDeclarations() {
        return functionDecls;
    }

    Identifier return_id = new Identifier(null);

    public TranslationVisitor(ArrayList<ArrayList<Interval>> il) {
        intervalLists = il;
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
        instrsLineIndex = 0;

        // Save registers t0–t9 (prologue)
        for (int i = 0; i <= 5; i++) {
            String reg = "t" + i;
            String saveReg = "save_" + reg;
            instrs.add(new sparrowv.Move_Reg_Reg(new Register(saveReg), new Register(reg)));
        }

        for (Identifier fp : n.formalParameters) {
            Register paramReg = getRegisterForIdentifier(fp);
            instrs.add(new sparrowv.Move_Reg_Reg(paramReg, new Register(fp.toString())));
        }
        n.block.accept(this, instrsData);
        instrs.add(new sparrowv.Move_Id_Reg(return_id, getRegisterForIdentifier(return_id)));
        // Restore registers t0–t9 (epilogue)
        for (int i = 0; i <= 5; i++) {
            String reg = "t" + i;
            String saveReg = "save_" + reg;
            instrs.add(new sparrowv.Move_Reg_Reg(new Register(reg), new Register(saveReg)));
        }
        
        sparrowv.Block block = new sparrowv.Block(new ArrayList<>(instrs), return_id);

        functionDecls.add(new sparrowv.FunctionDecl(n.functionName, n.formalParameters, block));
    }

    @Override
    public void visit(Block n, InstrsData instrsData) {
        for (Instruction i : n.instructions) {
            i.accept(this, instrsData);
            instrsLineIndex++;
        }
        return_id = n.return_id;
        instrsLineIndex++;
    }

    @Override
    public void visit(LabelInstr n, InstrsData instrsData) {
        instrs.add(new sparrowv.LabelInstr(n.label));
    }

    private Register getRegisterForIdentifier(Identifier id) {
        for (Interval interval : intervalLists.get(functionDecls.size())) {
            if (interval.register != null && id.toString().equals(interval.var)) {
                return new Register(interval.register);
            }
        }
        throw new RuntimeException("Register not found for identifier: " + id);
    }

    @Override
    public void visit(Load n, InstrsData instrsData) {
        Register lhs = getRegisterForIdentifier(n.lhs);
        Register base = getRegisterForIdentifier(n.base);
        instrs.add(new sparrowv.Load(lhs, base, n.offset));
    }

    @Override
    public void visit(Store n, InstrsData instrsData) {
        Register base = getRegisterForIdentifier(n.base);
        Register rhs = getRegisterForIdentifier(n.rhs);
        instrs.add(new sparrowv.Store(base, n.offset, rhs));
    }

    @Override
    public void visit(Move_Id_Id n, InstrsData instrsData) {
        Register rhs = getRegisterForIdentifier(n.rhs);
        Register lhs = getRegisterForIdentifier(n.lhs);
        instrs.add(new sparrowv.Move_Reg_Reg(lhs, rhs));
    }

    @Override
    public void visit(Move_Id_Integer n, InstrsData instrsData) {
        Register lhs = getRegisterForIdentifier(n.lhs);
        instrs.add(new sparrowv.Move_Reg_Integer(lhs, n.rhs));
    }

    @Override
    public void visit(Move_Id_FuncName n, InstrsData instrsData) {
        Register lhs = getRegisterForIdentifier(n.lhs);
        instrs.add(new sparrowv.Move_Reg_FuncName(lhs, n.rhs));
    }

    @Override
    public void visit(Add n, InstrsData instrsData) {
        Register lhs = getRegisterForIdentifier(n.lhs);
        Register arg1 = getRegisterForIdentifier(n.arg1);
        Register arg2 = getRegisterForIdentifier(n.arg2);
        instrs.add(new sparrowv.Add(lhs, arg1, arg2));
    }

    @Override
    public void visit(Subtract n, InstrsData instrsData) {
        Register lhs = getRegisterForIdentifier(n.lhs);
        Register arg1 = getRegisterForIdentifier(n.arg1);
        Register arg2 = getRegisterForIdentifier(n.arg2);
        instrs.add(new sparrowv.Subtract(lhs, arg1, arg2));
    }

    @Override
    public void visit(Multiply n, InstrsData instrsData) {
        Register lhs = getRegisterForIdentifier(n.lhs);
        Register arg1 = getRegisterForIdentifier(n.arg1);
        Register arg2 = getRegisterForIdentifier(n.arg2);
        instrs.add(new sparrowv.Multiply(lhs, arg1, arg2));
    }

    @Override
    public void visit(Alloc n, InstrsData instrsData) {
        Register lhsReg = getRegisterForIdentifier(n.lhs);
        Register sizeReg = getRegisterForIdentifier(n.size);
        instrs.add(new sparrowv.Alloc(lhsReg, sizeReg));
    }

    @Override
    public void visit(LessThan n, InstrsData instrsData) {
        Register lhs = getRegisterForIdentifier(n.lhs);
        Register arg1 = getRegisterForIdentifier(n.arg1);
        Register arg2 = getRegisterForIdentifier(n.arg2);
        instrs.add(new sparrowv.LessThan(lhs, arg1, arg2));
    }

    @Override
    public void visit(Print n, InstrsData instrsData) {
        Register content = getRegisterForIdentifier(n.content);
        instrs.add(new sparrowv.Print(content));
    }

    @Override
    public void visit(ErrorMessage n, InstrsData instrsData) {
        instrs.add(new sparrowv.ErrorMessage(n.msg));
    }

    @Override
    public void visit(IfGoto n, InstrsData instrsData) {
        Register cond = getRegisterForIdentifier(n.condition);
        instrs.add(new sparrowv.IfGoto(cond, n.label));
    }

    @Override
    public void visit(Goto n, InstrsData instrsData) {
        instrs.add(new sparrowv.Goto(n.label));
    }

    @Override
    public void visit(Call n, InstrsData instrsData) {
        Register lhs = getRegisterForIdentifier(n.lhs);
        Register callee = getRegisterForIdentifier(n.callee);
        List<Identifier> args = n.args;
        List<Identifier> newArgs = new ArrayList<>();

        for (Identifier id : args) {
            Identifier newId = new Identifier("temp_" + getRegisterForIdentifier(id).toString());
            instrs.add(new sparrowv.Move_Id_Reg(newId, getRegisterForIdentifier(id)));
            newArgs.add(newId);
        }

        instrs.add(new sparrowv.Call(lhs, callee, newArgs));
    }
}
