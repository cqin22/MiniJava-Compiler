package util;

import java.util.HashMap;
import java.util.Map;

import jdk.jshell.MethodSnippet;
import minijava.syntaxtree.*;

public class ClassTable {
    public HashMap<String, ClassInfos> classes;

    public ClassTable() {
        classes = new HashMap<>();
    }

    public void inherit(String childClass, String parentClass){
        ClassInfos childInfo = classes.get(childClass);

        ClassInfos parentInfo = classes.get(parentClass);
        if (parentInfo != null) {
            for (Map.Entry<String, MethodInfos> entry : parentInfo.methods.entrySet()) {
            childInfo.addMethod(entry.getKey(), entry.getValue().getReturnType());
            }

            for (Map.Entry<String, Type> entry : parentInfo.symbolTable.symbols.entrySet()) {
                childInfo.symbolTable.addSymbol(entry.getKey(), entry.getValue());
            }
        }

        classes.remove(childClass);
        classes.put(childClass, childInfo);
    }

    public boolean classDeclarationExists(String className){
        return classes.containsKey(className);
    }

    public void addClass(String className){
        ClassInfos c = new ClassInfos();
        c.className = className;
        classes.put(className, c);
    }

    public void addClass(String className, String parentName){
        ClassInfos c = new ClassInfos(parentName);
        c.className = className;
        classes.put(className, c);
    }

    public void addMethodToClass(String methodName, Type returnType, String className){
        classes.get(className).addMethod(methodName, returnType);
    }

    public boolean classExists(String className){
        return classes.containsKey(className);
    }

    public ClassInfos getClassInfo(String className){
        return classes.get(className);
    }
    
    public void print() {
        for (Map.Entry<String, ClassInfos> entry : classes.entrySet()) {
            String className = entry.getKey();
            ClassInfos classInfo = entry.getValue();
            System.out.println("Class: " + className);
            classInfo.print();
        }
    }
}