package riscv;

import IR.token.Label;

public class label {
    public Label label;

    public label(Label label) {
        this.label = label;
    }

    public String toString() {
        return label + ":";
    }
}
