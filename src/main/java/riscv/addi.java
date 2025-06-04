package riscv;

import IR.token.Register;
import IR.token.Label;

public class addi {
    public Register lhs;
    public Register arg1;
    public int arg2;

    public addi(Register lhs, Register arg1, int arg2) {
        this.lhs = lhs;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    public String toString() {
        return "addi " + lhs + ", " + arg1 + ", " + arg2;
}
}
