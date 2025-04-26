package util;

import java.util.HashMap;
import java.util.Map;
import minijava.syntaxtree.*;

public class SymbolTable {
    public Map<String, Type> symbols;

    public SymbolTable() {
        this.symbols = new HashMap<>();
    }

    public void addSymbol(String name, Type type) {
        symbols.put(name, type);
    }

    public void print(){
        for(Map.Entry<String, Type> entry : symbols.entrySet()){
            System.out.println("ID: " + entry.getKey());
            System.out.println("  Type: " + entry.getValue().toString());
        }
    }

    public Type getSymbolType(String key) {
        key = key.trim();

        for (String existingKey : symbols.keySet()) {
            if (existingKey.equals(key)) {
                return symbols.get(existingKey);
            }
        }

        return null;
    }
}

