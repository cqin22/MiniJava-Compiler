import java.io.InputStream;
import java.util.*;

import minijava.MiniJavaParser;
import minijava.syntaxtree.*;
import minijava.visitor.*;
import util.*;

//in first pass, bulid a inheritance tree that says what fields correspond to each class. and its methods? this will solve issues. do inheritance + cycle detection on first pass, and overwriting/typechecking each method on secnod pass
public class Typecheck extends GJDepthFirst<Type, SymbolTable> { 
  ClassTable classTable;
  String currentClass; 
  String currentMethod;

  public Typecheck(ClassTable ct){
    classTable = ct;
  }

  private void error(){
    System.out.print("Type error");
    System.exit(0);
  }

  /* CLASSES */

    /**
   * Grammar production:
   * f0 -> MainClass()
   * f1 -> ( TypeDeclaration() )*
   * f2 -> <EOF>
   */
  @Override
  public Type visit(Goal n, SymbolTable s_void){
    n.f0.accept(this, s_void);
    n.f1.accept(this, s_void);
    n.f2.accept(this, s_void);
    return null;
  }

    /**
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
  public Type visit(MainClass n, SymbolTable s_void){ //no use for s
    String className = n.f1.f0.toString();
    ClassInfos classInfo = classTable.getClassInfo(className);
    SymbolTable symbolTable = classInfo.getSymbolTable();

    currentClass = className;

    // TODO: check distinct
    n.f0.accept(this, symbolTable);
    n.f1.accept(this, symbolTable);
    n.f2.accept(this, symbolTable);
    n.f3.accept(this, symbolTable);
    n.f4.accept(this, symbolTable);
    n.f5.accept(this, symbolTable);
    n.f6.accept(this, symbolTable);
    n.f7.accept(this, symbolTable);
    n.f8.accept(this, symbolTable);
    n.f9.accept(this, symbolTable);
    n.f10.accept(this, symbolTable);
    n.f11.accept(this, symbolTable);
    n.f12.accept(this, symbolTable);
    n.f13.accept(this, symbolTable);
    n.f14.accept(this, symbolTable);
    n.f15.accept(this, symbolTable);
    n.f16.accept(this, symbolTable);
    n.f17.accept(this, symbolTable);

    return null;
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
  public Type visit(ClassDeclaration n, SymbolTable s_void){
    String className = n.f1.f0.toString();
    ClassInfos classInfo = classTable.getClassInfo(className);
    SymbolTable symbolTable = classInfo.getSymbolTable();

    currentClass = className;

    n.f0.accept(this, symbolTable);
    n.f1.accept(this, symbolTable);
    n.f2.accept(this, symbolTable);
    n.f3.accept(this, symbolTable);
    n.f4.accept(this, symbolTable);
    n.f5.accept(this, symbolTable);
    
    return null;
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
  public Type visit(MethodDeclaration n, SymbolTable class_symbolTable){
    String methodName = n.f2.f0.toString();
    currentMethod = methodName;
    System.out.println("currentMethod: " + currentMethod);

    if(currentClass.equals("") || currentMethod.equals("")){
      // method declared outside a class/method
      error();
    }

    SymbolTable symbolTable = new SymbolTable();

    // if parameters are present
    if(n.f4.present()){
      FormalParameterList formalParameterList = (FormalParameterList) n.f4.node;
      FormalParameter formalParameter = formalParameterList.f0;
      symbolTable.addSymbol(extractStringFromNode(formalParameter.f1), formalParameter.f0);
      NodeListOptional formalParameterRestList = formalParameterList.f1;
      if(formalParameterRestList.present()){
        for(int i = 0; i < formalParameterRestList.size(); i++){
          FormalParameterRest formalParameterRest = (FormalParameterRest) formalParameterRestList.elementAt(i);
          formalParameter = formalParameterRest.f1;
          String variableName = extractStringFromNode(formalParameter.f1);
          symbolTable.addSymbol(variableName, formalParameter.f0);
        }
      }
    }

    // symbolTable.print();
    n.f0.accept(this, symbolTable);
    n.f1.accept(this, symbolTable);
    n.f2.accept(this, symbolTable);
    n.f3.accept(this, symbolTable);
    n.f4.accept(this, symbolTable);
    n.f5.accept(this, symbolTable);
    n.f6.accept(this, symbolTable);
    n.f7.accept(this, symbolTable);

    // TODO: classTable.getClassInfo(currentClass).getMethodInfo(currentMethod).
    symbolTable = combineTables(symbolTable, class_symbolTable); // at this point, duplicatees are ok

    n.f8.accept(this, symbolTable);
    n.f9.accept(this, symbolTable);
    n.f10.accept(this, symbolTable);
    n.f11.accept(this, symbolTable);
    n.f12.accept(this, symbolTable);

    return null;
  }

  /* STATEMENTS */

  /**
   * Grammar production:
   * f0 -> "while"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   * f4 -> Statement()
   */
  @Override
  public Type visit(WhileStatement n, SymbolTable s){
    n.f0.accept(this, s);
    n.f1.accept(this, s);
    Type condition = n.f2.accept(this, s);
    n.f3.accept(this, s);
    n.f4.accept(this, s);

    if(condition.f0.which == TypeConstants.IDENTIFIER){
      condition = s.getSymbolType(extractStringFromNode(condition.f0));
    }
    
    if(condition.f0.which != TypeConstants.BOOLEANTYPE){
      error();
    }
    
    return null;
  }


  /**
   * Grammar production:
   * f0 -> "if"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   * f4 -> Statement()
   * f5 -> "else"
   * f6 -> Statement()
   */
  @Override
  public Type visit(IfStatement n, SymbolTable s){
    n.f0.accept(this, s);
    n.f1.accept(this, s);
    Type condition = n.f2.accept(this, s);
    n.f3.accept(this, s);
    n.f4.accept(this, s);
    n.f5.accept(this, s);
    n.f6.accept(this, s);

    if(condition.f0.which == TypeConstants.IDENTIFIER){
      condition = s.getSymbolType(extractStringFromNode(condition.f0));
    }

    if (condition.f0.which != TypeConstants.BOOLEANTYPE){
      error();
    }

    return null;
  }
  

  /**
   * Grammar production:
   * f0 -> Type()
   * f1 -> Identifier()
   * f2 -> ";"
   */
  @Override
  public Type visit(VarDeclaration n, SymbolTable s){
    s.addSymbol(n.f1.f0.toString(), n.f0);

    n.f0.accept(this, s);
    n.f1.accept(this, s);
    n.f2.accept(this, s);

    return null;
  }

    /**
   * Grammar production:
   * f0 -> Identifier()
   * f1 -> "="
   * f2 -> Expression()
   * f3 -> ";"
   */
  @Override
  public Type visit(AssignmentStatement n, SymbolTable s) {
    String left = n.f0.f0.toString().trim();

    n.f0.accept(this, s);
    n.f1.accept(this, s);

    Type typeNumLeft = s.getSymbolType(left);
    Type typeNumRight = n.f2.accept(this, s);
    
    if (typeNumLeft == null || typeNumRight == null || (typeNumLeft.f0.toString().equals(typeNumRight.f0.toString()))) { // TODO: implement checking expression
        error();
    }

    n.f3.accept(this, s);

    return null;
  }

  /* Expression & Primary Expressions */

    /**
   * Grammar production:
   * f0 -> PrimaryExpression()
   * f1 -> "*"
   * f2 -> PrimaryExpression()
   */
  public Type visit(TimesExpression n, SymbolTable s){
    Type left = n.f0.accept(this, s);
    n.f1.accept(this, s);
    Type right = n.f2.accept(this, s);

    if(left.f0.which == TypeConstants.IDENTIFIER){
      String identifierName = extractStringFromNode(left.f0);
      Type identifierType = s.getSymbolType(identifierName);
      if(identifierType == null || identifierType.f0.which != TypeConstants.INTEGERTYPE) {
        error();
      }
    }
    else if(left.f0.which != TypeConstants.INTEGERTYPE){
      error();
    }

    if(right.f0.which == TypeConstants.IDENTIFIER){
      String identifierName = extractStringFromNode(right.f0);
      Type identifierType = s.getSymbolType(identifierName);
      if(identifierType == null || identifierType.f0.which != TypeConstants.INTEGERTYPE) {
        error();
      }
    }
    else if (right.f0.which != TypeConstants.INTEGERTYPE){
      error();
    }

    return new Type(new NodeChoice(n, TypeConstants.BOOLEANTYPE));
  }

    /**
   * Grammar production:
   * f0 -> PrimaryExpression()
   * f1 -> "-"
   * f2 -> PrimaryExpression()
   */
  public Type visit(MinusExpression n, SymbolTable s){
    Type left = n.f0.accept(this, s);
    n.f1.accept(this, s);
    Type right = n.f2.accept(this, s);

    if(left.f0.which == TypeConstants.IDENTIFIER){
      String identifierName = extractStringFromNode(left.f0);
      Type identifierType = s.getSymbolType(identifierName);
      if(identifierType == null || identifierType.f0.which != TypeConstants.INTEGERTYPE) {
        error();
      }
    }
    else if(left.f0.which != TypeConstants.INTEGERTYPE){
      error();
    }

    if(right.f0.which == TypeConstants.IDENTIFIER){
      String identifierName = extractStringFromNode(right.f0);
      Type identifierType = s.getSymbolType(identifierName);
      if(identifierType == null || identifierType.f0.which != TypeConstants.INTEGERTYPE) {
        error();
      }
    }
    else if (right.f0.which != TypeConstants.INTEGERTYPE){
      error();
    }

    return new Type(new NodeChoice(n, TypeConstants.BOOLEANTYPE));
  }

  /**
   * Grammar production:
   * f0 -> PrimaryExpression()
   * f1 -> "&&"
   * f2 -> PrimaryExpression()
   */
  public Type visit(AndExpression n, SymbolTable s){
    Type left = n.f0.accept(this, s);
    n.f1.accept(this, s);
    Type right = n.f2.accept(this, s);
 
    if(left.f0.which == TypeConstants.IDENTIFIER){
      String identifierName = extractStringFromNode(left.f0);
      Type identifierType = s.getSymbolType(identifierName);
      if(identifierType == null || identifierType.f0.which != TypeConstants.INTEGERTYPE) {
        error();
      }
    }
    else if(left.f0.which != TypeConstants.BOOLEANTYPE){
      error();
    }

    if(right.f0.which == TypeConstants.IDENTIFIER){
      String identifierName = extractStringFromNode(right.f0);
      Type identifierType = s.getSymbolType(identifierName);
      if(identifierType == null || identifierType.f0.which != TypeConstants.INTEGERTYPE) {
        error();
      }
    }
    else if (right.f0.which != TypeConstants.BOOLEANTYPE){
      error();
    }

    return new Type(new NodeChoice(n, TypeConstants.BOOLEANTYPE));
  }

  /**
   * Grammar production:
   * f0 -> "("
   * f1 -> Expression()
   * f2 -> ")"
   */
  @Override
  public Type visit(BracketExpression n, SymbolTable s){
    n.f0.accept(this, s);
    Type type = n.f1.accept(this, s);
    n.f2.accept(this, s);

    return type;
  }

  /**
   * Grammar production:
   * f0 -> "!"
   * f1 -> Expression()
   */
  @Override
  public Type visit(NotExpression n, SymbolTable s){
    n.f0.accept(this, s);
    Type type = n.f1.accept(this, s);

    if(type.f0.which != TypeConstants.BOOLEANTYPE){
      error();
    } 

    return new Type(new NodeChoice(new BooleanType(), TypeConstants.BOOLEANTYPE));
  }
  /**
   * Grammar production:
   * f0 -> PrimaryExpression()
   * f1 -> "+"
   * f2 -> PrimaryExpression()
   */
  @Override
  public Type visit(PlusExpression n, SymbolTable s){
    Type left = n.f0.accept(this, s);
    n.f1.accept(this, s);
    Type right = n.f2.accept(this, s);
    
    //stwap - what if you id as one side

    if(left.f0.which == TypeConstants.IDENTIFIER){
      String identifierName = extractStringFromNode(left.f0);
      Type identifierType = s.getSymbolType(identifierName);
      if(identifierType == null || identifierType.f0.which != TypeConstants.INTEGERTYPE) {
        error();
      }
    }
    else if(left.f0.which != TypeConstants.INTEGERTYPE){
      error();
    }
    
    if(right.f0.which == TypeConstants.IDENTIFIER){
      String identifierName = extractStringFromNode(right.f0);
      Type identifierType = s.getSymbolType(identifierName);
      if(identifierType == null || identifierType.f0.which != TypeConstants.INTEGERTYPE) {
        error();
      }
    }
    else if (right.f0.which != TypeConstants.INTEGERTYPE){
      error();
    }

    return new Type(new NodeChoice(n, TypeConstants.INTEGERTYPE));
  }

  /**
  * f0 -> PrimaryExpression()
  * f1 -> "."
  * f2 -> Identifier()
  * f3 -> "("
  * f4 -> ( ExpressionList() )?
  * f5 -> ")"
  */
  @Override
  public Type visit(MessageSend n, SymbolTable s){

    // check if class exists
    Type classType = n.f0.accept(this, s);
    if (classType == null) {
        error();
    }

    String className = extractStringFromNode(classType.f0);
    n.f1.accept(this, s);

    Type methodType = n.f2.accept(this, s);
    String methodName = ((Identifier) methodType.f0.choice).f0.toString();

    n.f3.accept(this, s);
    n.f4.accept(this, s);
    n.f5.accept(this, s);

    // get class
    String classDeclString;
    if(classTable.classDeclarationExists(className)){
      classDeclString = className;
    }
    else{
      Type classDeclType = s.getSymbolType(className);
      classDeclString = ((Identifier) classDeclType.f0.choice).f0.toString();
    }

    // check expression list
    


    return classTable.getClassInfo(classDeclString).getMethodInfo(methodName).getReturnType();
  }

  @Override
  public Type visit(Expression n, SymbolTable s){
    return n.f0.accept(this, s);
  }

  @Override
  public Type visit(PrimaryExpression n, SymbolTable s){
    return n.f0.accept(this, s);
  }

  /**
    * f0 -> <IDENTIFIER>
    */
  @Override
  public Type visit(Identifier n, SymbolTable s){
    n.f0.accept(this, s);
    return new Type(new NodeChoice(n, TypeConstants.IDENTIFIER));
  }

  /**
   * Grammar production:
   * f0 -> "new"
   * f1 -> Identifier()
   * f2 -> "("
   * f3 -> ")"
   */
  @Override
  public Type visit(AllocationExpression n, SymbolTable s){
    n.f0.accept(this, s);
    Type type = n.f1.accept(this, s);
    Identifier id;
    if (type.f0.choice instanceof Identifier) {
        id = (Identifier) type.f0.choice;
    } else {
        error();
        return null; // should never reach
    }
    n.f2.accept(this, s);
    n.f3.accept(this, s);
    return new Type(new NodeChoice(id, TypeConstants.IDENTIFIER));
  }

  /**
   * Grammar production:
   * f0 -> "true"
   */
  @Override
  public Type visit(TrueLiteral n, SymbolTable s){
    n.f0.accept(this, s);
    return new Type(new NodeChoice(new BooleanType(), TypeConstants.BOOLEANTYPE));
  }

  @Override
  public Type visit(ThisExpression n, SymbolTable s){
    n.f0.accept(this, s);
    // return null;
    return new Type(new NodeChoice(new Identifier(new NodeToken(currentClass)), 3));
  }
  /**
   * Grammar production:
   * f0 -> "false"
   */
  @Override
  public Type visit(FalseLiteral n, SymbolTable s){
    n.f0.accept(this, s);
    return new Type(new NodeChoice(new BooleanType(), TypeConstants.BOOLEANTYPE));
  }

  /**
  * f0 -> "new"
  * f1 -> "int"
  * f2 -> "["
  * f3 -> Expression()
  * f4 -> "]"
  */
  @Override
  public Type visit(ArrayAllocationExpression n, SymbolTable s){
    n.f0.accept(this, s);
    n.f1.accept(this, s);
    n.f2.accept(this, s);
    n.f3.accept(this, s);
    n.f4.accept(this, s);
    return new Type(new NodeChoice(new ArrayType(), TypeConstants.ARRAYTYPE));
  }

  @Override
  public Type visit(IntegerLiteral n, SymbolTable s){
    n.f0.accept(this, s);
    
    return new Type(new NodeChoice(new IntegerType(), TypeConstants.INTEGERTYPE)); // TODO: do you need the const?
  }

  /**
   * Grammar production:
   * f0 -> PrimaryExpression()
   * f1 -> "<"
   * f2 -> PrimaryExpression()
   */
  @Override
  public Type visit(CompareExpression n, SymbolTable s){
    Type left = n.f0.accept(this, s);
    n.f1.accept(this, s);
    Type right = n.f2.accept(this, s);
    
    if(left.f0.which == TypeConstants.IDENTIFIER){
      String identifierName = extractStringFromNode(left.f0);
      Type identifierType = s.getSymbolType(identifierName);
      if(identifierType == null || identifierType.f0.which != TypeConstants.INTEGERTYPE) {
        error();
      }
    }
    else if(left.f0.which != TypeConstants.INTEGERTYPE){
      error();
    }

    if(right.f0.which == TypeConstants.IDENTIFIER){
      String identifierName = extractStringFromNode(right.f0);
      Type identifierType = s.getSymbolType(identifierName);
      if(identifierType == null || identifierType.f0.which != TypeConstants.INTEGERTYPE) {
        error();
      }
    }
    else if (right.f0.which != TypeConstants.INTEGERTYPE){
      error();
    }

    return new Type(new NodeChoice(n, TypeConstants.BOOLEANTYPE));
  }

  public static void main(String[] args) throws Exception {
        InputStream in = System.in;
        new MiniJavaParser(in);
        Goal root = MiniJavaParser.Goal();
        ClassTable classTable = new ClassTable();
        FirstPass firstPass = new FirstPass();

        Typecheck tc = new Typecheck(classTable);

        root.accept(firstPass, classTable);
        root.accept(tc, null);

        System.out.print("Program type checked successfully");
  }

  public static String extractStringFromNode(Node node) {
    if (node instanceof NodeToken) {
        return ((NodeToken) node).tokenImage;
    } else if (node instanceof Identifier) {
        return ((Identifier) node).f0.tokenImage;
    } else if (node instanceof NodeChoice) {
        return extractStringFromNode(((NodeChoice) node).choice);
    } else {
        // Fall back to node.toString() or visitor-based logic
        return "";
    }
  }

  // symbols from s1 override symbols from s2
  public SymbolTable combineTables(SymbolTable s1, SymbolTable s2){
      SymbolTable resultSymbolTable = new SymbolTable();
      for(Map.Entry<String, Type> entry : s1.symbols.entrySet()){
          resultSymbolTable.addSymbol(entry.getKey(), entry.getValue());
      }

      for(Map.Entry<String, Type> entry : s2.symbols.entrySet()){
          if(resultSymbolTable.symbols.get(entry.getKey()) == null){
              resultSymbolTable.addSymbol(entry.getKey(), entry.getValue());
          }
      }
      
      return resultSymbolTable;
  }

  // check for duplicateds
  // public static boolean distinct(SymbolTable symbolTable){
  //   Set<String> uniqueKeys = new HashSet<>();
  //   for(Map.Entry<String, Type> entry : symbolTable.symbols.entrySet()){
  //     if(uniqueKeys.contains(entry.getKey())){
  //       return false;
  //     }
  //     else{
  //       uniqueKeys.add(entry.getKey());
  //     }
  //   }
  //   return true;
  // }
} 