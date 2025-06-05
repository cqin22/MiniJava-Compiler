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

import com.sun.source.doctree.ReturnTree;

import riscv.*;

public class SV2V implements Visitor {
    Program program;
    private HashMap<String, Integer> id_to_index = new HashMap<>();
    private final Register fp = new Register("fp");
    private final Register sp = new Register("sp");
    private final Register t6 = new Register("t6");
    private final Register a0 = new Register("a0");
    private final Register ra = new Register("ra");
    private final Label alloc = new Label("alloc");
    private final Label print = new Label("print");
    private final Label error = new Label("error");
    String currentFunction;
    ArrayList<Identifier> fp_list = new ArrayList<>();
    String mainName;
    private int labelCounter = 0;

    int stackCounter;
    private List<Object> riscvObjects = new ArrayList<>();

    public SV2V(Program p) {
        program = p;
    }

    public void runTranslation() {
        program.accept(this);
    }

    public String getRiscVObjectsAsString() {
        StringBuilder sb = new StringBuilder();
        sb.append(".equiv @sbrk, 9\n");
        sb.append(".equiv @print_string, 4\n");
        sb.append(".equiv @print_char, 11\n");
        sb.append(".equiv @print_int, 1\n");
        sb.append(".equiv @exit, 10\n");
        sb.append(".equiv @exit2, 17\n");
        sb.append("\n.text\n\n");
        sb.append(".globl m\n jal ").append(mainName).append("\n");
        sb.append("  li a0, @exit\n  ecall\n\n");
        for (Object obj : riscvObjects) {
            sb.append(obj.toString()).append("\n");
        }
        sb.append("li a1, 10\n");
        sb.append("li a0, 11\n");
        sb.append("ecall\n");
        sb.append("\n.globl print\nprint:\n");
        sb.append("  mv a1, a0\n  li a0, @print_int\n  ecall\n");
        sb.append("  li a1, 10\n  li a0, @print_char\n  ecall\n  jr ra\n\n");

        sb.append(".globl error\nerror:\n");
        sb.append("  mv a1, a0\n  li a0, @print_string\n  ecall\n");
        sb.append("  li a1, 10\n  li a0, @print_char\n  ecall\n");
        sb.append("  li a0, @exit\n  ecall\nabort_17:\n  j abort_17\n\n");

        sb.append(".globl alloc\nalloc:\n");
        sb.append("  mv a1, a0\n  li a0, @sbrk\n  ecall\n  jr ra\n\n");

        sb.append(".data\n\n.globl msg_nullptr\nmsg_nullptr:\n  .asciiz \"null pointer\"\n  .align 2\n\n");
        sb.append(".globl msg_array_oob\nmsg_array_oob:\n  .asciiz \"array index out of bounds\"\n  .align 2\n");
        return sb.toString();
    }

    @Override
    public void visit(Program n) {
        for (FunctionDecl fd : n.funDecls) {
            fd.accept(this);
        }
    }

    @Override
    public void visit(FunctionDecl n) {
        id_to_index.clear();
        stackCounter = -12;
        int offset = 0;
        for (Identifier fp : n.formalParameters) {
            id_to_index.put(fp.toString(), offset);
            offset += 4;
        }

        currentFunction = n.functionName.toString();
        if (mainName == null) mainName = currentFunction;

        riscvObjects.add(new riscvObject(".globl " + currentFunction));
        riscvObjects.add(new function(n.functionName));
        prologue();
        n.block.accept(this);
    }

    @Override
    public void visit(Block n) {
        for (Instruction i : n.instructions) i.accept(this);
        epilogue(n.return_id.toString());
    }

    @Override
    public void visit(LabelInstr n) {
        riscvObjects.add(new riscv.label(new Label(currentFunction + "_" + n.label.toString())));
    }

    @Override public void visit(Move_Reg_Integer n) { riscvObjects.add(new riscv.li(n.lhs, n.rhs)); }
    @Override public void visit(Move_Reg_FuncName n) { riscvObjects.add(new riscv.la(n.lhs, n.rhs)); }
    @Override public void visit(Add n) { riscvObjects.add(new riscv.add(n.lhs, n.arg1, n.arg2)); }
    @Override public void visit(Subtract n) { riscvObjects.add(new riscv.sub(n.lhs, n.arg1, n.arg2)); }
    @Override public void visit(Multiply n) { riscvObjects.add(new riscv.mul(n.lhs, n.arg1, n.arg2)); }
    @Override public void visit(LessThan n) { riscvObjects.add(new riscv.slt(n.lhs, n.arg1, n.arg2)); }
    @Override public void visit(Load n) { riscvObjects.add(new riscv.lw(n.lhs, n.offset, n.base)); }
    @Override public void visit(Store n) { riscvObjects.add(new riscv.sw(n.rhs, n.offset, n.base)); }
    @Override public void visit(Move_Reg_Reg n) { riscvObjects.add(new riscv.mv(n.lhs, n.rhs)); }

    @Override
    public void visit(Move_Id_Reg n) {
        if (!id_to_index.containsKey(n.lhs.toString())) {
            id_to_index.put(n.lhs.toString(), stackCounter);
            stackCounter -= 4;
        }
        riscvObjects.add(new riscv.sw(n.rhs, id_to_index.get(n.lhs.toString()), fp));
    }

    @Override
    public void visit(Move_Reg_Id n) {
        if (!id_to_index.containsKey(n.rhs.toString())) {
            id_to_index.put(n.rhs.toString(), stackCounter);
            stackCounter -= 4;
        }
        riscvObjects.add(new riscv.lw(n.lhs, id_to_index.get(n.rhs.toString()), fp));
    }

    @Override
    public void visit(Alloc n) {
        riscvObjects.add(new riscv.mv(a0, n.size));
        riscvObjects.add(new riscv.jal(alloc));
        riscvObjects.add(new riscv.mv(n.lhs, a0));
    }

    @Override
    public void visit(Print n) {
        riscvObjects.add(new riscv.mv(a0, n.content));
        riscvObjects.add(new riscv.jal(print));
    }

    @Override
    public void visit(ErrorMessage n) {
        riscvObjects.add(new riscv.la(a0, new FunctionName("msg_nullptr")));
        riscvObjects.add(new riscv.jal(error));
    }

    @Override
    public void visit(Goto n) {
        riscvObjects.add(new riscv.jal(currentFunction, n.label));
    }

    @Override
    public void visit(IfGoto n) {
        String skipLabelName = "No_Jump_" + currentFunction + "_" + n.label + "_" + labelCounter++;
        Label skipLabel = new Label(skipLabelName);
    
        riscvObjects.add(new riscv.bnez(n.condition, skipLabel));
        riscvObjects.add(new riscv.jal(currentFunction, n.label));
        riscvObjects.add(new riscv.label(skipLabel));
    }
    

    @Override
    public void visit(Call n) {
        int numArgs = n.args.size();
        int totalSize = numArgs * 4;
        riscvObjects.add(new riscv.li(t6, totalSize));
        riscvObjects.add(new riscv.sub(sp, sp, t6));

        for (int i = 0; i < numArgs; i++) {
            Identifier arg = n.args.get(i);
            int offset = id_to_index.get(arg.toString());
            riscvObjects.add(new riscv.lw(t6, offset, fp));
            riscvObjects.add(new riscv.sw(t6, i * 4, sp));
        }

        riscvObjects.add(new riscv.jalr(new Register(n.callee.toString())));
        // riscvObjects.add(new riscv.addi(sp, sp, totalSize));

        Register ret = new Register("a2");
        riscvObjects.add(new riscv.mv(ret, a0));
        int destOffset = id_to_index.computeIfAbsent(n.lhs.toString(), k -> {
            int off = stackCounter;
            stackCounter -= 4;
            return off;
        });
        // riscvObjects.add(new riscv.sw(ret, destOffset, fp));
    }

    
    public void prologue(){
        riscvObjects.add(new sw(fp, -8, sp));
        riscvObjects.add(new mv(fp, sp));
        riscvObjects.add(new li(t6, 1000));
        riscvObjects.add(new sub(sp, sp, t6));
        riscvObjects.add(new sw(ra, -4, fp));
    }

    public void epilogue(String return_value){
        riscvObjects.add(new lw(a0, id_to_index.get(return_value), fp));
        riscvObjects.add(new lw(ra, -4, fp));
        riscvObjects.add(new lw(fp, -8, fp));
        riscvObjects.add(new addi(sp, sp, 1000));
        riscvObjects.add(new jr(ra));
    }

    public static void main(String[] args) throws Exception {
        Registers.SetRiscVregs();
        InputStream in = System.in;
        new SparrowParser(in);
        Node root = SparrowParser.Program();
        SparrowVConstructor constructor = new SparrowVConstructor();
        root.accept(constructor);
        Program program = constructor.getProgram();

        SV2V sv2v = new SV2V(program);
        sv2v.runTranslation();

        System.out.println(sv2v.getRiscVObjectsAsString());
    }
}
