package riscv;

import IR.token.Register;
import IR.token.Label;
import IR.token.FunctionName;


public class jr {
    public Register label;

    public jr(Register label){
        this.label = label;
    }

    public String toString() {
        return "jr " + label;
    }
}
