package util;

import java.util.HashMap;
import java.util.Map;
import minijava.syntaxtree.*;

public class MethodInfos{
    public SymbolTable symbolTable;
    Type returnType;
    public FormalParameterList formalParameterList;

    public MethodInfos(Type rt) {
        symbolTable = new SymbolTable();
        returnType = rt;
    }

    public SymbolTable getSymbolTable(){
        return symbolTable;
    }

    public Type getReturnType(){
        return returnType;
    }
}