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

    int stackCounter = -12;

    private List<Object> riscvObjects = new ArrayList<>();

    public SV2V(Program p){
        program = p;
    }

    public void runTranslation(){
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
        sb.append(".globl main\n");
        sb.append("main:\n");
        sb.append("  jal Main\n");
        sb.append("  li a0, @exit\n");
        sb.append("  ecall\n\n");
        for (Object obj : riscvObjects) {
            sb.append(obj.toString()).append("\n");
        }
        sb.append("\n.globl print\n");
        sb.append("print:\n");
        sb.append("  mv a1, a0\n");
        sb.append("  li a0, @print_int\n");
        sb.append("  ecall\n");
        sb.append("  li a1, 10\n");
        sb.append("  li a0, @print_char\n");
        sb.append("  ecall\n");
        sb.append("  jr ra\n\n");

        sb.append(".globl error\n");
        sb.append("error:\n");
        sb.append("  mv a1, a0\n");
        sb.append("  li a0, @print_string\n");
        sb.append("  ecall\n");
        sb.append("  li a1, 10\n");
        sb.append("  li a0, @print_char\n");
        sb.append("  ecall\n");
        sb.append("  li a0, @exit\n");
        sb.append("  ecall\n");
        sb.append("abort_17:\n");
        sb.append("  j abort_17\n\n");

        sb.append(".globl alloc\n");
        sb.append("alloc:\n");
        sb.append("  mv a1, a0\n");
        sb.append("  li a0, @sbrk\n");
        sb.append("  ecall\n");
        sb.append("  jr ra\n\n");

        sb.append(".data\n\n");

        sb.append(".globl msg_nullptr\n");
        sb.append("msg_nullptr:\n");
        sb.append("  .asciiz \"null pointer\"\n");
        sb.append("  .align 2\n\n");

        sb.append(".globl msg_array_oob\n");
        sb.append("msg_array_oob:\n");
        sb.append("  .asciiz \"array index out of bounds\"\n");
        sb.append("  .align 2\n");
        return sb.toString();
    }

    @Override
    public void visit(Program n) {
        /* List<FunctionDecl> funDecls; */
        for (FunctionDecl fd: n.funDecls) {
            fd.accept(this);
        }
    }

    @Override
    public void visit(FunctionDecl n) {
        /* Program parent;
         * FunctionName functionName;
         * List<Identifier> formalParameters;
         * Block block; */
        int count = 0;
        for (Identifier fp: n.formalParameters) {
            // ... fp ...
            id_to_index.put(fp.toString(), count);
            count += 4;
        }

        currentFunction = n.functionName.toString();
        riscvObjects.add(new riscvObject(".globl " + n.functionName.toString()));
        riscvObjects.add(new function(n.functionName));
        prologue();

        n.block.accept(this);

    }

    @Override
    public void visit(Block n) {
        /* FunctionDecl parent;
         * List<Instruction> instructions;
         * Identifier return_id; */
        for (Instruction i: n.instructions) {
            i.accept(this);
        }
        epilogue(n.return_id.toString());
    }

    @Override
    public void visit(LabelInstr n) {
        /* Label label; */
        Label newLabel = new Label(currentFunction + "_" + n.label.toString());
        riscvObjects.add(new riscv.label(newLabel));
    }

    @Override
    public void visit(Move_Reg_Integer n) {
        /* Register lhs;
         * int rhs; */
        riscvObjects.add(new riscv.li(n.lhs, n.rhs));
    }

    @Override
    public void visit(Move_Reg_FuncName n) {
        /* Register lhs;
         * FunctionName rhs; */
        riscvObjects.add(new riscv.la(n.lhs, n.rhs));
    }

    @Override
    public void visit(Add n) {
        /* Register lhs;
         * Register arg1;
         * Register arg2; */
        riscvObjects.add(new riscv.add(n.lhs, n.arg1, n.arg2));
    }

    @Override
    public void visit(Subtract n) {
        /* Register lhs;
         * Register arg1;
         * Register arg2; */
        riscvObjects.add(new riscv.sub(n.lhs, n.arg1, n.arg2));
    }

    @Override
    public void visit(Multiply n) {
        /* Register lhs;
         * Register arg1;
         * Register arg2; */
        riscvObjects.add(new riscv.mul(n.lhs, n.arg1, n.arg2));
    }

    @Override
    public void visit(LessThan n) {
        /* Register lhs;
         * Register arg1;
         * Register arg2; */
        riscvObjects.add(new riscv.slt(n.lhs, n.arg1, n.arg2));
    }

    @Override
    public void visit(Load n) {
        /* Register lhs;
         * Register base;
         * int offset; */
        riscvObjects.add(new riscv.lw(n.lhs, n.offset, n.base));
    }

    @Override
    public void visit(Store n) {
        /* Register base;
         * int offset;
         * Register rhs; */
        riscvObjects.add(new riscv.sw(n.rhs, n.offset, n.base));
    }

    @Override
    public void visit(Move_Reg_Reg n) {
        /* Register lhs;
         * Register rhs; */
        riscvObjects.add(new riscv.mv(n.lhs, n.rhs));
    }

    @Override
    public void visit(Move_Id_Reg n) {
        if (!id_to_index.containsKey(n.lhs.toString())) {
            id_to_index.put(n.lhs.toString(), stackCounter);
            stackCounter -= 4;
        }
        int offset = id_to_index.get(n.lhs.toString());
        riscvObjects.add(new riscv.sw(n.rhs, offset, fp));
    }
    

    @Override
    public void visit(Move_Reg_Id n) {
        if (!id_to_index.containsKey(n.rhs.toString())) {
            id_to_index.put(n.rhs.toString(), stackCounter);
            stackCounter -= 4;
        }
        int offset = id_to_index.get(n.rhs.toString());
        riscvObjects.add(new riscv.lw(n.lhs, offset, fp));
    }   

    @Override
    public void visit(Alloc n) {
        /* Register lhs;
         * Register size; */
        riscvObjects.add(new riscv.mv(a0, n.size));
        riscvObjects.add(new riscv.jal(alloc));
        riscvObjects.add(new riscv.mv(n.lhs, a0));
    }

    @Override
    public void visit(Print n) {
        /* Register content; */
        riscvObjects.add(new riscv.mv(a0, n.content));
        riscvObjects.add(new riscv.jal(print));
    }

    @Override
    public void visit(ErrorMessage n) {
        /* String msg; */
        FunctionName nullptr = new FunctionName("msg_nullptr");
        riscvObjects.add(new riscv.la(a0, nullptr));
        riscvObjects.add(new riscv.jal(error));
        // riscvObjects.add(new riscv.error(n.message));
    }

    @Override
    public void visit(Goto n) {
        /* Label label; */
        riscvObjects.add(new riscv.jal(currentFunction, n.label));

        // riscvObjects.add(new riscv.j(n.label));
    }

    @Override
    public void visit(IfGoto n) {
        /* Register condition;
         * Label label; */
        if(currentFunction == null){
            Label mangledNoJump = new Label("No_Jump_" + n.label.toString());
            riscvObjects.add(new riscv.bnez(n.condition, mangledNoJump));
            riscvObjects.add(new riscv.jal(n.label));
            riscvObjects.add(new riscv.label(mangledNoJump));
            return;
        }

        Label mangledNoJump = new Label("No_Jump_" + currentFunction +  n.label.toString());
        riscvObjects.add(new riscv.bnez(n.condition, mangledNoJump));
        riscvObjects.add(new riscv.jal(currentFunction, n.label));
        riscvObjects.add(new riscv.label(mangledNoJump));
    }

    @Override
    public void visit(Call n) {
        // Step 1: Reserve space on stack for arguments
        int numArgs = n.args.size();
        int totalArgSize = numArgs * 4;
        riscvObjects.add(new riscv.li(t6, totalArgSize));
        riscvObjects.add(new riscv.sub(sp, sp, t6));
    
        // Step 2: Load args from caller frame and store to callee frame
        for (int i = 0; i < numArgs; i++) {
            Identifier arg = n.args.get(i);
            int argOffset = id_to_index.get(arg.toString());
            Register temp = new Register("t6"); // reuse t6 like in your pattern
    
            riscvObjects.add(new riscv.lw(temp, argOffset, fp));        // lw t6, offset(fp)
            riscvObjects.add(new riscv.sw(temp, i * 4, sp));             // sw t6, i*4(sp)
        }
    
        // Step 3: Function call
        riscvObjects.add(new riscv.jalr(new Register(n.callee.toString())));              // jalr t1
    
        // Step 4: Restore sp
        // riscvObjects.add(new riscv.addi(sp, sp, totalArgSize));         // addi sp, sp, 8
    
        // Step 5: Store return value into lhs
        Register tempRet = new Register("t0");                           // mv t0, a0
        riscvObjects.add(new riscv.mv(tempRet, a0));
        int destOffset;
        if (!id_to_index.containsKey(n.lhs.toString())) {
            destOffset = stackCounter;
            id_to_index.put(n.lhs.toString(), destOffset);
            stackCounter -= 4;
        } else {
            destOffset = id_to_index.get(n.lhs.toString());
        }
        // riscvObjects.add(new riscv.sw(tempRet, destOffset, fp));
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
        riscvObjects.add(new addi(sp, sp, 1000)); // remove 8 slots from activation record
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
