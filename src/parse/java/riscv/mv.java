package riscv;

import IR.token.Register;

public class mv {
    public Register lhs;
    public Register arg;

    public mv(Register lhs, Register arg) {
        this.lhs = lhs;
        this.arg = arg;
    }

    public String toString() {
        return "mv " + lhs + ", " + arg;
    }
}
