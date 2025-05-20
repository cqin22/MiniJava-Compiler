package sparrowUtils;
import sparrowv.*;
import sparrowv.visitor.*;

import java.util.*;

import IR.token.*;



public class LAVisitor implements ArgVisitor<ArrayList<InstrsData>>
{
    int index = 0;

    /*   List<FunctionDecl> funDecls; */
    @Override
    public void visit(Program n, ArrayList<InstrsData> arg) {
        for (FunctionDecl fd: n.funDecls) {
            
            // reset liveness anlaysis
            arg.clear();
            fd.accept(this, arg);
        }
    }

    /*   Program parent;
     *   FunctionName functionName;
     *   List<Identifier> formalParameters;
     *   Block block; */
    @Override
    public void visit(FunctionDecl n, ArrayList<InstrsData> arg) {
        for (Identifier fp: n.formalParameters) {
            // ... fp ...
        }
        n.block.accept(this, arg);
    }

    /*   FunctionDecl parent;
     *   List<Instruction> instructions;
     *   Identifier return_id; */
    @Override
    public void visit(Block n, ArrayList<InstrsData> arg) {
        for (Instruction i: n.instructions) {
            i.accept(this, arg);
        }
        System.err.println("return: " + n.return_id.toString());
    }

    /*   Label label; */
    @Override
    public void visit(LabelInstr n, ArrayList<InstrsData> arg) {
        // Implementation here
    }

    /*   Register lhs;
     *   int rhs; */
    @Override
    public void visit(Move_Reg_Integer n, ArrayList<InstrsData> arg) {
        // Implementation here
    }

    /*   Register lhs;
     *   FunctionName rhs; */
    @Override
    public void visit(Move_Reg_FuncName n, ArrayList<InstrsData> arg) {
        // Implementation here
    }

    /*   Register lhs;
     *   Register arg1;
     *   Register arg2; */
    @Override
    public void visit(Add n, ArrayList<InstrsData> arg) {
        // Implementation here
    }

    /*   Register lhs;
     *   Register arg1;
     *   Register arg2; */
    @Override
    public void visit(Subtract n, ArrayList<InstrsData> arg) {
        // Implementation here
    }

    /*   Register lhs;
     *   Register arg1;
     *   Register arg2; */
    @Override
    public void visit(Multiply n, ArrayList<InstrsData> arg) {
        // Implementation here
    }

    /*   Register lhs;
     *   Register arg1;
     *   Register arg2; */
    @Override
    public void visit(LessThan n, ArrayList<InstrsData> arg) {
        // Implementation here
    }

    /*   Register lhs;
     *   Register base;
     *   int offset; */
    @Override
    public void visit(Load n, ArrayList<InstrsData> arg) {
        // Implementation here
    }

    /*   Register base;
     *   int offset;
     *   Register rhs; */
    @Override
    public void visit(Store n, ArrayList<InstrsData> arg) {
        // Implementation here
    }

    /*   Register lhs;
     *   Register rhs; */
    @Override
    public void visit(Move_Reg_Reg n, ArrayList<InstrsData> arg) {
        // Implementation here
    }

    /*   Identifier lhs;
     *   Register rhs; */
    @Override
    public void visit(Move_Id_Reg n, ArrayList<InstrsData> arg) {
        // Implementation here
    }

    /*   Register lhs;
     *   Identifier rhs; */
    @Override
    public void visit(Move_Reg_Id n, ArrayList<InstrsData> arg) {
        // Implementation here
    }

    /*   Register lhs;
     *   Register size; */
    @Override
    public void visit(Alloc n, ArrayList<InstrsData> arg) {
        // Implementation here
    }

    /*   Register content; */
    @Override
    public void visit(Print n, ArrayList<InstrsData> arg) {
        // Implementation here
    }

    /*   String msg; */
    @Override
    public void visit(ErrorMessage n, ArrayList<InstrsData> arg) {
        // Implementation here
    }

    /*   Label label; */
    @Override
    public void visit(Goto n, ArrayList<InstrsData> arg) {
        // Implementation here
    }

    /*   Register condition;
     *   Label label; */
    @Override
    public void visit(IfGoto n, ArrayList<InstrsData> arg) {
        // Implementation here
    }

    /*   Register lhs;
     *   Register callee;
     *   List<Identifier> args; */
    @Override
    public void visit(Call n, ArrayList<InstrsData> arg) {
        // Implementation here
    }
}