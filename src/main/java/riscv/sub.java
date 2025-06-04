package riscv;

import IR.token.Register;

public class sub {
    public Register lhs;
    public Register arg1;
    public Register arg2;

    public sub(Register lhs, Register arg1, Register arg2) {
        this.lhs = lhs;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    public String toString() {
        return "sub " + lhs + ", " + arg1 + ", " + arg2;
    }
}
