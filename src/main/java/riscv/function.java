package riscv;

import IR.syntaxtree.Label;
import IR.token.FunctionName;

public class function {
    public FunctionName functionName;

    public function(FunctionName functionName) {
        this.functionName = functionName;
    }

    public String toString() {
        return functionName + ":";
    }
}
