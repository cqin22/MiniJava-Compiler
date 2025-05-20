import java.io.InputStream;
import java.util.Map;

import org.w3c.dom.traversal.NodeFilter;

import IR.syntaxtree.Add;
import minijava.MiniJavaParser;
import minijava.syntaxtree.*;
import minijava.visitor.*;
import util.*;

public class FirstPass extends GJVoidDepthFirst<ClassTable> {
  String currentClass;
  String currentMethod;
    private void error(){
        System.out.println("Type error");
        System.exit(0);
    }

  /**
   * Grammar production:
   * f0 -> "class"
   * f1 -> Identifier()
   * f2 -> "{"
   * f3 -> "public"
   * f4 -> "static"
   * f5 -> "void"
   * f6 -> "main"
   * f7 -> "("
   * f8 -> "String"
   * f9 -> "["
   * f10 -> "]"
   * f11 -> Identifier()
   * f12 -> ")"
   * f13 -> "{"
   * f14 -> ( VarDeclaration() )*
   * f15 -> ( Statement() )*
   * f16 -> "}"
   * f17 -> "}"
   */
  @Override
  public void visit(MainClass n, ClassTable c){
    c.addClass(n.f1.f0.toString());

    // c.print();
    
    n.f0.accept(this, c);
    n.f1.accept(this, c);
    n.f2.accept(this, c);
    n.f3.accept(this, c);
    n.f4.accept(this, c);
    n.f5.accept(this, c);
    n.f6.accept(this, c);
    n.f7.accept(this, c);
    n.f8.accept(this, c);
    n.f9.accept(this, c);
    n.f10.accept(this, c);
    n.f11.accept(this, c);
    n.f12.accept(this, c);
    n.f13.accept(this, c);
    n.f14.accept(this, c);
    n.f15.accept(this, c);
    n.f16.accept(this, c);
    n.f17.accept(this, c);
  }

/**
 * Grammar production:
 * f0 -> "class"
 * f1 -> Identifier()
 * f2 -> "{"
 * f3 -> ( VarDeclaration() )*
 * f4 -> ( MethodDeclaration() )*
 * f5 -> "}"
 */
  @Override
  public void visit(ClassDeclaration n, ClassTable c){
    String className = n.f1.f0.toString();
    currentClass = className;
    c.addClass(className);
    for(int i = 0; i < n.f4.size(); i++){
        MethodDeclaration method = (MethodDeclaration) n.f4.elementAt(i);
        c.addMethodToClass(method.f2.f0.toString(), method.f1, className);
    }

    for(int i = 0; i < n.f3.size(); i++){
      VarDeclaration varDecl = (VarDeclaration) n.f3.elementAt(i);
      c.addFieldToClass(varDecl.f1.toString(), className);
    }
    
    // all the methods are added at this 
    // c.print();
    n.f0.accept(this, c);
    n.f1.accept(this, c);
    n.f2.accept(this, c);
    n.f3.accept(this, c);
    n.f4.accept(this, c);
    n.f5.accept(this, c);
  }

  /**
   * Grammar production:
   * f0 -> "class"
   * f1 -> Identifier()
   * f2 -> "extends"
   * f3 -> Identifier()
   * f4 -> "{"
   * f5 -> ( VarDeclaration() )*
   * f6 -> ( MethodDeclaration() )*
   * f7 -> "}"
   */
  @Override
  public void visit(ClassExtendsDeclaration n, ClassTable c){
    String className = n.f1.f0.toString();
    String parentClass = n.f3.f0.toString();
    currentClass = className;
    c.addClass(className, parentClass);
    for(int i = 0; i < n.f6.size(); i++){
        MethodDeclaration method = (MethodDeclaration) n.f6.elementAt(i);
        c.addMethodToClass(method.f2.f0.toString(), method.f1, className);
    }

    // add parent methods
    

    n.f0.accept(this, c);
    n.f1.accept(this, c);
    n.f2.accept(this, c);
    n.f3.accept(this, c);
    n.f4.accept(this, c);
    n.f5.accept(this, c);
    n.f6.accept(this, c);
    n.f7.accept(this, c);
  }


  /**
   * Grammar production:
   * f0 -> "public"
   * f1 -> Type()
   * f2 -> Identifier()
   * f3 -> "("
   * f4 -> ( FormalParameterList() )?
   * f5 -> ")"
   * f6 -> "{"
   * f7 -> ( VarDeclaration() )*
   * f8 -> ( Statement() )*
   * f9 -> "return"
   * f10 -> Expression()
   * f11 -> ";"
   * f12 -> "}"
   */
  @Override
  public void visit(MethodDeclaration n, ClassTable c){
    // all the methods are added at this 
    // c.print();
    String methodName = n.f2.f0.toString();
    currentMethod = methodName;

    if(n.f4.present()){

      FormalParameterList formalParameterList = (FormalParameterList) n.f4.node;

      MethodInfos methodInfos = c.getClassInfo(currentClass).getMethodInfo(methodName);
      methodInfos.formalParameterList = formalParameterList;


    }

    n.f4.accept(this, c);
  }
}