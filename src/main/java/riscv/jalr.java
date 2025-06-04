package riscv;

import IR.token.Register;

public class jalr {
    private Register register;

    public jalr(Register register) {
        this.register = register;
    }

    @Override
    public String toString() {
        return "jalr " + register;
    }
}
