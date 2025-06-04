package riscv;

import IR.token.Register;

public class sw {
    public Register rhs; // Source register
    public int offset;   // Offset value
    public Register base; // Base register

    public sw(Register rhs, int offset, Register base) {
        this.rhs = rhs;
        this.offset = offset;
        this.base = base;
    }

    public String toString() {
        return "sw " + rhs + ", " + offset + "(" + base + ")";
    }
}
