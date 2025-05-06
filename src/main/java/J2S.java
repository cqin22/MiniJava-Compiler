import java.io.InputStream;
import java.net.IDN;
import java.util.*;

import IR.token.Identifier;

import minijava.*;
import minijava.visitor.*;
import minijava.syntaxtree.*;
import sparrow.*;
import sparrowv.Move_Id_Reg;
import util.*;


public class J2S extends GJNoArguDepthFirst<Identifier>{
    private List<FunctionDecl> functions = new ArrayList<>();
    private List<Instruction> instrs = new ArrayList<>();
    private Map<String, Identifier> varMap = new HashMap<>();
    private int tempCounter = 0;
    private ClassTable classTable;

    public J2S(ClassTable ct){
        classTable = ct;
    }

    private Identifier newTemp(){
        return new Identifier("v" + (tempCounter++));
    }

    //TODO: paste in all the visits

   /**
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    * f2 -> <EOF>
    */
    @Override
    public Identifier visit(Goal n){
        n.f0.accept(this);
        n.f1.accept(this);
        return null;
    }

    /**
    * f0 -> <INTEGER_LITERAL>
    */
    @Override
    public Identifier visit(IntegerLiteral n){
        Identifier newVar = newTemp();
        instrs.add(new Move_Id_Integer(newVar, Integer.parseInt(n.f0.toString())));
        return newVar;
    }



    public List<FunctionDecl> getFunctions(){
        return functions;
    }

    public static void main(String[] args) throws Exception {
        InputStream in = System.in;
        new MiniJavaParser(in);
        Goal root = MiniJavaParser.Goal();
        
        ClassTable classTable = new ClassTable();
        FirstPass firstPass = new FirstPass();
        root.accept(firstPass, classTable);

        J2S j2s = new J2S(classTable);
        root.accept(j2s);

        Program sparrowProgram = new Program(j2s.getFunctions());

        System.out.println(sparrowProgram.toString());
    }
}