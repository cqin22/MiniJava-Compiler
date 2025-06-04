package riscv;

import IR.token.Register;
import IR.token.Label;
import IR.token.FunctionName;


public class la {
    public Register lhs;
    public FunctionName label;

    public la(Register lhs, FunctionName label){
        this.lhs = lhs;
        this.label = label;
    }

    public String toString() {
        return "la " + lhs + ", " + label;
    }
}
