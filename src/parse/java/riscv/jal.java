package riscv;

import IR.syntaxtree.Label;

public class jal {
    public Label label;

    public jal(Label label) {
        this.label = label;
    }

    public String toString() {
        return "jal " + label;
    }
}
