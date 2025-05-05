import java.io.InputStream;
import java.util.*;

import minijava.MiniJavaParser;
import minijava.syntaxtree.*;
import minijava.visitor.*;
import util.*;

// todo
//method doesnt check correctly
// arraylength didn't do anything?

//in first pass, bulid a inheritance tree that says what fields correspond to each class. and its methods? this will solve issues. do inheritance + cycle detection on first pass, and overwriting/typechecking each method on secnod pass
public class Typecheck extends GJDepthFirst<Type, SymbolTable> { 
  ClassTable classTable;
  String currentClass; 
  String currentMethod;

  public Typecheck(ClassTable ct){
    classTable = ct;
  }

  private void error(){
    System.out.println("Type error");
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
  public Type visit(ClassExtendsDeclaration n, SymbolTable s){
    String className = n.f1.f0.toString();
    String parentName = n.f3.f0.toString();

    if(!classTable.classes.containsKey(parentName)){
      error();
    }
    classTable.inherit(className, parentName);


    ClassInfos classInfo = classTable.getClassInfo(className);
    SymbolTable symbolTable = classInfo.getSymbolTable();

    currentClass = className;

    n.f0.accept(this, symbolTable);
    n.f1.accept(this, symbolTable);
    n.f2.accept(this, symbolTable);
    n.f3.accept(this, symbolTable);
    n.f4.accept(this, symbolTable);
    n.f5.accept(this, symbolTable);
    n.f6.accept(this, symbolTable);
    n.f7.accept(this, symbolTable);
    
    ClassInfos classInQuestion = classInfo;
    while(classInQuestion.parentClass != null){
      if(classInQuestion.parentClass.equals(currentClass)){
        error();
      }
      else{
        classInQuestion = classTable.getClassInfo(classInQuestion.parentClass);
      }
    }

    

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

    // if(n.f3.present()){
    //   for(int i = 0; i < n.f3.size(); i++){
    //     HashMap<String, String> map= new HashMap<>();
    //     VarDeclaration varDeclaration = (VarDeclaration) n.f3.elementAt(i);
    //     String key = extractStringFromNode(varDeclaration.f1);
    //     if(map.containsKey(key)){
    //       error(); 
    //     }
    //     else{
    //       map.put(key, "void");
    //     }
    //   }
    // }
    
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
    // System.out.println("currentMethod: " + currentMethod);

    if(currentClass.equals("") || currentMethod.equals("")){
      // method declared outside a class/method
      error();
    }

    HashMap<String, String> checkShadowing = new HashMap();

    SymbolTable symbolTable = new SymbolTable();

    FormalParameterList formalParameterList;
    // if parameters are present
    if(n.f4.present()){
      formalParameterList = (FormalParameterList) n.f4.node;
      FormalParameter formalParameter = formalParameterList.f0;
      symbolTable.addSymbol(extractStringFromNode(formalParameter.f1), formalParameter.f0);
      checkShadowing.put(extractStringFromNode(formalParameter.f1), "a");
      NodeListOptional formalParameterRestList = formalParameterList.f1;
      if(formalParameterRestList.present()){
        for(int i = 0; i < formalParameterRestList.size(); i++){
          FormalParameterRest formalParameterRest = (FormalParameterRest) formalParameterRestList.elementAt(i);
          formalParameter = formalParameterRest.f1;
          String variableName = extractStringFromNode(formalParameter.f1);
          if(checkShadowing.containsKey(variableName)){
            error();
          }
          checkShadowing.put(variableName, "a");
          symbolTable.addSymbol(variableName, formalParameter.f0);
        }
      }
    }

    for(int i = 0; i < n.f7.size(); i++){
      VarDeclaration varDeclaration = (VarDeclaration) n.f7.elementAt(i);
      if(checkShadowing.containsKey(extractStringFromNode(varDeclaration.f1))){
        error();
      }
    }

    // symbolTable.print();
    n.f0.accept(this, symbolTable);
    Type returnType = n.f1.accept(this, symbolTable);
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
    Type actualReturnType = n.f10.accept(this, symbolTable);
    n.f11.accept(this, symbolTable);
    n.f12.accept(this, symbolTable);

    if(returnType.f0.which == TypeConstants.IDENTIFIER){
      String identifierName = extractStringFromNode(returnType.f0);
      Type identifierType = symbolTable.getSymbolType(identifierName);
      // classTable.print();
      if(identifierType == null && !classTable.classExists(identifierName)){
          error();
      }

      if(identifierType != null){
        returnType = identifierType;
      }
    }

    if(actualReturnType.f0.which == TypeConstants.IDENTIFIER){
      String identifierName = extractStringFromNode(actualReturnType.f0);
      Type identifierType = symbolTable.getSymbolType(identifierName);
      if(identifierType == null) {
        error();
      }

      actualReturnType = identifierType;
    }

    if (returnType.f0.which == TypeConstants.IDENTIFIER && actualReturnType.f0.which == TypeConstants.IDENTIFIER) {
      String returnTypeClass = extractStringFromNode(returnType.f0);
      String actualReturnTypeClass = extractStringFromNode(actualReturnType.f0);

      if (!classTable.classExists(returnTypeClass) || !classTable.classExists(actualReturnTypeClass)) {
        error();
      }

      ClassInfos returnTypeInfo = classTable.getClassInfo(returnTypeClass);
      ClassInfos actualReturnTypeInfo = classTable.getClassInfo(actualReturnTypeClass);

      if (!isSubtype(actualReturnTypeInfo, returnTypeInfo)) {
        error();
      }
    } else if (returnType.f0.which != actualReturnType.f0.which) {
      error();
    }

    return null;
  }

  /* STATEMENTS */

  /**
   * Grammar production:
   * f0 -> "System.out.println"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   * f4 -> ";"
   */
  @Override
  public Type visit(PrintStatement n, SymbolTable s){
    Type type = n.f2.accept(this, s);
    
    if(type.f0.which == TypeConstants.IDENTIFIER){
      String identifierName = extractStringFromNode(type.f0);
      Type identifierType = s.getSymbolType(identifierName);
      if(identifierType == null || identifierType.f0.which != TypeConstants.INTEGERTYPE) {
        error();
      }
    }
    else if(type.f0.which != TypeConstants.INTEGERTYPE){
      error();
    }

    return null;
  }
  
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
    String varName = n.f0.f0.toString();
  
    Type typeLeft = s.getSymbolType(varName);
    if (typeLeft == null) {
      error();
    }
  
    Type typeRight = n.f2.accept(this, s);
    if (typeRight == null) {
      error();
    }
  
    if (typeRight.f0.which == TypeConstants.IDENTIFIER) {
      String id = extractStringFromNode(typeRight.f0);
      Type resolved = s.getSymbolType(id);
      if (resolved != null) {
        typeRight = resolved;
      }
    }
  
    if (typeLeft.f0.which == TypeConstants.IDENTIFIER && typeRight.f0.which == TypeConstants.IDENTIFIER) {
      String leftClass = extractStringFromNode(typeLeft.f0);
      String rightClass = extractStringFromNode(typeRight.f0);
  
      if (!classTable.classExists(leftClass) || !classTable.classExists(rightClass)) {
        error();
      }
  
      if (!isSubtype(classTable.getClassInfo(rightClass), classTable.getClassInfo(leftClass))) {
        error();
      }
    } else {
      if (typeLeft.f0.which != typeRight.f0.which) {
        error();
      }
    }
  
    n.f1.accept(this, s); // =
    n.f3.accept(this, s); // ;
  
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

    return new Type(new NodeChoice(n, TypeConstants.INTEGERTYPE));
  }

    /**c
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

    return new Type(new NodeChoice(n, TypeConstants.INTEGERTYPE));
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
      if(identifierType == null || identifierType.f0.which != TypeConstants.BOOLEANTYPE) {
        error();
      }
    }
    else if(left.f0.which != TypeConstants.BOOLEANTYPE){
      error();
    }

    if(right.f0.which == TypeConstants.IDENTIFIER){
      String identifierName = extractStringFromNode(right.f0);
      Type identifierType = s.getSymbolType(identifierName);
      if(identifierType == null || identifierType.f0.which != TypeConstants.BOOLEANTYPE) {
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

    if(type.f0.which == TypeConstants.IDENTIFIER){
      String identifierName = extractStringFromNode(type.f0);
      Type identifierType = s.getSymbolType(identifierName);
      if(identifierType == null || identifierType.f0.which != TypeConstants.BOOLEANTYPE) {
        error();
      }
    }

    else if(type.f0.which != TypeConstants.BOOLEANTYPE){
      error();
    } 

    return new Type(new NodeChoice(new BooleanType(), TypeConstants.BOOLEANTYPE));
  }

  /**
   * Grammar production:
   * f0 -> ArrayType()
   *       | BooleanType()
   *       | IntegerType()
   *       | Identifier()
   */
  @Override
  public Type visit(Type n, SymbolTable s){
    n.f0.accept(this, s);
    return n;
  }

  /**
   * Grammar production:
   * f0 -> PrimaryExpression()
   * f1 -> "."
   * f2 -> "length"
   */
  @Override
  public Type visit(ArrayLength n, SymbolTable s){
    Type type = n.f0.accept(this, s);

    if(type.f0.which == TypeConstants.IDENTIFIER){
      String identifierName = extractStringFromNode(type.f0);
      Type identifierType = s.getSymbolType(identifierName);
      if(identifierType == null || identifierType.f0.which != TypeConstants.ARRAYTYPE) {
        error();
      }
    }
    else if(type.f0.which != TypeConstants.ARRAYTYPE){
      error();
    }

    return new Type(new NodeChoice(new IntegerType(), TypeConstants.INTEGERTYPE));
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
    Type expressionListType = n.f4.accept(this, s);
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

    FormalParameterList formalParameterList = classTable.getClassInfo(classDeclString).getMethodInfo(methodName).formalParameterList;

    if(n.f4.present()){
      ExpressionList expressionList = (ExpressionList) n.f4.node;
      Type expressionType = expressionList.f0.accept(this, s);
      
      int whichExpression = expressionType.f0.which;

      if(whichExpression == TypeConstants.IDENTIFIER){
        String identifierName = extractStringFromNode(expressionType.f0);
        Type identifierType = s.getSymbolType(identifierName);
        // classTable.print();
        if(identifierType == null && !classTable.classExists(identifierName)){
            error();
        }
  
        if(identifierType != null){
          whichExpression = identifierType.f0.which;
        }
      }

      if(whichExpression != formalParameterList.f0.f0.f0.which){
        // System.out.println("formal parameter list dont match expression list");
        error();
      }

      if(expressionList.f1.size() != formalParameterList.f1.size()){
        error();
      }

      if(expressionList.f1.present()){
        for(int i = 0; i < expressionList.f1.size(); i++){
          ExpressionRest expressionRest = (ExpressionRest) expressionList.f1.elementAt(i);
          expressionType = expressionRest.f1.accept(this, s);
          whichExpression = expressionType.f0.which;

          if(whichExpression == TypeConstants.IDENTIFIER){
            String identifierName = extractStringFromNode(expressionType.f0);
            Type identifierType = s.getSymbolType(identifierName);
            // classTable.print();
            if(identifierType == null && !classTable.classExists(identifierName)){
                error();
            }
      
            if(identifierType != null){
              whichExpression = identifierType.f0.which;
            }
          }
    
          FormalParameterRest formalParameterRest = (FormalParameterRest) formalParameterList.f1.elementAt(i); 
          if(whichExpression != formalParameterRest.f1.f0.f0.which){ //more than 4 still passing?

            // System.out.println("formal parameter list dont match expression list");
            error();
          }    
        }
      }
    }

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
   * Grammar production:
   * f0 -> Identifier()
   * f1 -> "["
   * f2 -> Expression()
   * f3 -> "]"
   * f4 -> "="
   * f5 -> Expression()
   * f6 -> ";"
   */
  @Override
  public Type visit(ArrayAssignmentStatement n, SymbolTable s){
    Type arrayName = n.f0.accept(this, s);

    if(arrayName.f0.which == TypeConstants.IDENTIFIER){
      String identifierName = extractStringFromNode(arrayName.f0);
      Type identifierType = s.getSymbolType(identifierName);
      if(identifierType == null || identifierType.f0.which != TypeConstants.ARRAYTYPE) {
        error();
      }
    }
    else if(arrayName.f0.which != TypeConstants.ARRAYTYPE){
      error();
    }

    Type index = n.f2.accept(this, s);
    if(index.f0.which == TypeConstants.IDENTIFIER){
      String identifierName = extractStringFromNode(index.f0);
      Type identifierType = s.getSymbolType(identifierName);
      if(identifierType == null || identifierType.f0.which != TypeConstants.INTEGERTYPE) {
        error();
      }
    }
    else if(index.f0.which != TypeConstants.INTEGERTYPE){
      error();
    }

    Type input = n.f5.accept(this, s);
    if(input.f0.which == TypeConstants.IDENTIFIER){
      String identifierName = extractStringFromNode(input.f0);
      Type identifierType = s.getSymbolType(identifierName);
      if(identifierType == null || identifierType.f0.which != TypeConstants.INTEGERTYPE) {
        error();
      }
    }
    else if(input.f0.which != TypeConstants.INTEGERTYPE){
      error();
    }

    return null;
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
    Type type = n.f3.accept(this, s);
    n.f4.accept(this, s);

    if(type.f0.which == TypeConstants.IDENTIFIER){
      String identifierName = extractStringFromNode(type.f0);
      Type identifierType = s.getSymbolType(identifierName);
      if(identifierType == null || identifierType.f0.which != TypeConstants.INTEGERTYPE) {
        error();
      }
    }
    else if(type.f0.which != TypeConstants.INTEGERTYPE){
      error();
    }

    return new Type(new NodeChoice(new ArrayType(), TypeConstants.ARRAYTYPE));
  }

  /**
   * Grammar production:
   * f0 -> PrimaryExpression()
   * f1 -> "["
   * f2 -> PrimaryExpression()
   * f3 -> "]"
   */
  @Override
  public Type visit(ArrayLookup n, SymbolTable s){
    Type left = n.f0.accept(this, s);
    n.f1.accept(this, s);
    Type right = n.f2.accept(this, s);
    n.f3.accept(this, s);

    if(left.f0.which == TypeConstants.IDENTIFIER){
      String identifierName = extractStringFromNode(left.f0);
      Type identifierType = s.getSymbolType(identifierName);
      if(identifierType == null || identifierType.f0.which != TypeConstants.ARRAYTYPE) {
        error();
      }
    }
    else if(left.f0.which != TypeConstants.ARRAYTYPE){
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

    return new Type(new NodeChoice(new IntegerType(), TypeConstants.INTEGERTYPE));
  }

  @Override
  public Type visit(IntegerLiteral n, SymbolTable s){
    n.f0.accept(this, s);
    
    return new Type(new NodeChoice(new IntegerType(), TypeConstants.INTEGERTYPE));
  }

  // @Override
  // public Type visit(ExpressionList n, SymbolTable s){
    
  // }

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

        System.out.println("Program type checked successfully");
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

  public boolean isSubtype(ClassInfos sub, ClassInfos sup) {
    while (sub != null) {
        if (sub.className.equals(sup.className)) {
            return true;
        }
        if (sub.parentClass == null) break;
        sub = classTable.getClassInfo(sub.parentClass);
    }
    return false;
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