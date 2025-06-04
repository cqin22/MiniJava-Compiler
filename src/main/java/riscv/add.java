package riscv;

import IR.token.Register;
import IR.token.Label;

public class add {
    public Register lhs;
    public Register arg1;
    public Register arg2;

    public add(Register lhs, Register arg1, Register arg2) {
        this.lhs = lhs;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    public String toString() {
        return "add " + lhs + ", " + arg1 + ", " + arg2;
}
}
