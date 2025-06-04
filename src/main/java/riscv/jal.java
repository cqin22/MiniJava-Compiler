package riscv;

import IR.token.Label;

public class jal {
    public Label label;
    String currentFunction;

    public jal(Label label) {
        this.label = label;
    }

    public jal(String cf, Label label) {
        currentFunction = cf;
        this.label = label;
    }


    public String toString() {
        if(currentFunction == null){
            return "jal " + label;
        }
        return "jal " + currentFunction + "_" + label;
    }
}
