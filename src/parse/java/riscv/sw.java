package riscv;

import IR.token.Register;

public class sw {
    public Register lhs; // Source register
    public int offset;   // Offset value
    public Register base; // Base register

    public sw(Register lhs, int offset, Register base) {
        this.lhs = lhs;
        this.offset = offset;
        this.base = base;
    }

    public String toString() {
        return "sw " + lhs + ", " + offset + "(" + base + ")";
    }
}
