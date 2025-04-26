package util;

import java.util.HashMap;
import java.util.Map;
import minijava.syntaxtree.*;

public class ClassTable {
    HashMap<String, ClassInfos> classes;

    public ClassTable() {
        classes = new HashMap<>();
    }

    public boolean classDeclarationExists(String className){
        return classes.containsKey(className);
    }

    public void addClass(String className){
        ClassInfos c = new ClassInfos();
        classes.put(className, c);
    }

    public void addMethodToClass(String methodName, Type returnType, String className){
        classes.get(className).addMethod(methodName, returnType);
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