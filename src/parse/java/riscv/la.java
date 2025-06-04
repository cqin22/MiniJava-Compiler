package riscv;

import IR.token.Register;
import IR.token.Label;

public class la {
    public Register lhs;
    public Label label;

    public la(Register lhs, Label label){
        this.lhs = lhs;
        this.label = label;
    }

    public String toString() {
        return "la " + lhs + ", " + label;
    }
}
