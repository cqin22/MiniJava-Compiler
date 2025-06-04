package riscv;

import IR.token.Register;

public class li {
    public Register lhs;
    public int rhs;

    public li(Register lhs, int rhs){
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public String toString() {
        return "li " + lhs + ", " + rhs;
    }
}
