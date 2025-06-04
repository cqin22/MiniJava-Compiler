package riscv;

import IR.token.Label;
import IR.token.Register;

public class bnez {
    public Register register;
    public Label label;

    public bnez(Register register, Label label) {
        this.register = register;
        this.label = label;
    }

    public String toString() {
        return "bnez " + register + ", " + label;
    }
}
