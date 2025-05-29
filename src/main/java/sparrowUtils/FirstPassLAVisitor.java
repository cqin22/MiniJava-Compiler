package sparrowUtils;
import java.lang.reflect.Array;

import sparrow.*;
import sparrow.visitor.*;

import java.util.*;

import IR.token.*;

public class FirstPassLAVisitor implements ArgVisitor<InstrsData> {
    HashMap<String, HashMap<String, InstrsData>> instrsMap;
    int instrsIndex = 0;
    String currentFunc;

    public FirstPassLAVisitor(HashMap<String, HashMap<String, InstrsData>> im) {
        instrsMap = im;
    }

    @Override
    public void visit(Program n, InstrsData arg) {
        for (FunctionDecl fd: n.funDecls) {
            
            fd.accept(this, null);
        }
    }

    @Override
    public void visit(FunctionDecl n, InstrsData arg) {
        instrsIndex = 0;
        currentFunc = n.functionName.toString();
        // for (Identifier fp: n.formalParameters) {
        //     instrsIndex++;
        // }
        n.block.accept(this, null);
    }

    @Override
    public void visit(Block n, InstrsData arg) {
        for (Instruction i: n.instructions) {
            InstrsData instrsData = new InstrsData();
            instrsData.index = instrsIndex;
            i.accept(this, instrsData);
            instrsIndex++;
        }

        // return
        instrsIndex++;
    }

    @Override
    public void visit(LabelInstr n, InstrsData instrsData) {
        instrsData.labelName = n.label.toString();
        instrsMap.putIfAbsent(currentFunc, new HashMap<>());
        instrsMap.get(currentFunc).put(instrsData.labelName, instrsData);
    }

    @Override
    public void visit(Move_Id_Integer n, InstrsData arg) {
        // TODO: Implement this method
    }

    @Override
    public void visit(Move_Id_FuncName n, InstrsData arg) {
        // TODO: Implement this method
    }

    @Override
    public void visit(Add n, InstrsData arg) {
        // TODO: Implement this method
    }

    @Override
    public void visit(Subtract n, InstrsData arg) {
        // TODO: Implement this method
    }

    @Override
    public void visit(Multiply n, InstrsData arg) {
        // TODO: Implement this method
    }

    @Override
    public void visit(LessThan n, InstrsData arg) {
        // TODO: Implement this method
    }

    @Override
    public void visit(Load n, InstrsData arg) {
        // TODO: Implement this method
    }

    @Override
    public void visit(Store n, InstrsData arg) {
        // TODO: Implement this method
    }

    @Override
    public void visit(Move_Id_Id n, InstrsData arg) {
        // TODO: Implement this method
    }

    @Override
    public void visit(Alloc n, InstrsData arg) {
        // TODO: Implement this method
    }

    @Override
    public void visit(Print n, InstrsData arg) {
        // TODO: Implement this method
    }

    @Override
    public void visit(ErrorMessage n, InstrsData arg) {
        // TODO: Implement this method
    }

    @Override
    public void visit(Goto n, InstrsData arg) {
        // TODO: Implement this method
    }

    @Override
    public void visit(IfGoto n, InstrsData arg) {
        // TODO: Implement this method
    }

    @Override
    public void visit(Call n, InstrsData arg) {
        // TODO: Implement this method
    }
}
