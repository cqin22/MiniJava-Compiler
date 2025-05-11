package util;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import minijava.syntaxtree.*;
import IR.token.Identifier;

public class ClassInfos{
    public LinkedHashMap<String, MethodInfos> methods;
    public ArrayList<String> variables = new ArrayList<>();
    public SymbolTable symbolTable;
    public String parentClass;
    public String className;
    public Identifier vTableName;
    
    public ClassInfos(){
        methods = new LinkedHashMap<>();
        symbolTable = new SymbolTable();
    }

    public ClassInfos(String pc){
        methods = new LinkedHashMap<>();
        symbolTable = new SymbolTable();
        parentClass = pc;
    }

    public SymbolTable getSymbolTable(){
        return symbolTable;
    }

    public int getNumberOfMethods(){
        return methods.size();
    }

    public int getNumberOfFields(){
        return variables.size();
    }

    public void addMethod(String s, Type returnType){
        MethodInfos m = new MethodInfos(returnType);
        methods.put(s, m);
    }

    public void addField(String s){
        variables.add(s);
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