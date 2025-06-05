import java.io.InputStream;

import IR.SparrowParser;
import IR.visitor.SparrowVConstructor;
import sparrowv.*;
import sparrowv.visitor.*;
import IR.syntaxtree.Node;
import IR.token.*;
import IR.registers.Registers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;

import com.sun.source.doctree.ReturnTree;

import riscv.*;

public class FirstPassSV2V implements Visitor {
    Program program;
    public List<Integer> functionInstructions = new ArrayList<>();
    int count = 0;
    HashSet<String> ids = new HashSet<>();

    public FirstPassSV2V(Program p) {
        program = p;
    }

    @Override
    public void visit(Program n) {
        for (FunctionDecl fd : n.funDecls) {
            fd.accept(this);
        }
    }

    @Override
    public void visit(FunctionDecl n) {
        for (Identifier fp : n.formalParameters) {
        }
        n.block.accept(this);
        functionInstructions.add(count);
        count = 0;
    }

    @Override
    public void visit(Block n) {
        for (Instruction i : n.instructions) i.accept(this);
    }

    @Override
    public void visit(LabelInstr n) {
    }

    @Override public void visit(Move_Reg_Integer n) {}
    @Override public void visit(Move_Reg_FuncName n) {}
    @Override public void visit(Add n) {}
    @Override public void visit(Subtract n) {}
    @Override public void visit(Multiply n) {}
    @Override public void visit(LessThan n) {}
    @Override public void visit(Load n) {}
    @Override public void visit(Store n) {}
    @Override public void visit(Move_Reg_Reg n) {}

    @Override
    public void visit(Move_Id_Reg n) {
        if(!ids.contains(n.lhs.toString())){
            ids.add(n.lhs.toString());
            count++;
        }
    }

    @Override
    public void visit(Move_Reg_Id n) {
    }

    @Override
    public void visit(Alloc n) {
    }

    @Override
    public void visit(Print n) {
    }

    @Override
    public void visit(ErrorMessage n) {
    }

    @Override
    public void visit(Goto n) {
    }

    @Override
    public void visit(IfGoto n) {
    }
    

    @Override
    public void visit(Call n) {
    }
}
