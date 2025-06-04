package riscv;

import IR.token.Register;

public class lw {
    public Register lhs; // Destination register
    public int offset;   // Offset value
    public Register base; // Base register

    public lw(Register lhs, int offset, Register base) {
        this.lhs = lhs;
        this.offset = offset;
        this.base = base;
    }

    public String toString() {
        return "lw " + lhs + ", " + offset + "(" + base + ")";
    }
}
