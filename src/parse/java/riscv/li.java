package riscv;

import IR.token.Register;

public class li {
    public Register lhs;
    public Register rhs;

    public li(Register lhs, Register rhs){
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public String toString() {
        return "li " + lhs + ", " + rhs;
    }
}
