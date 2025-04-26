package util;

import java.util.HashMap;
import java.util.Map;
import minijava.syntaxtree.*;

public class ClassInfos{
    HashMap<String, MethodInfos> methods;
    SymbolTable symbolTable;
    
    public ClassInfos(){
        methods = new HashMap<>();
        symbolTable = new SymbolTable();
    }

    public SymbolTable getSymbolTable(){
        return symbolTable;
    }

    public void addMethod(String s, Type returnType){
        MethodInfos m = new MethodInfos(returnType);
        methods.put(s, m);
    }

    public MethodInfos getMethodInfo(String methodName){
        return methods.get(methodName);
    }

    public void print(){
        for(Map.Entry<String, MethodInfos> entry : methods.entrySet()){
            String methodName = entry.getKey();
            System.out.println("  Method: " + methodName);
        }
    }
}