import java.beans.ParameterDescriptor;
import java.io.InputStream;
import java.net.IDN;
import java.util.*;

import javax.imageio.stream.FileCacheImageOutputStream;

import org.w3c.dom.traversal.NodeFilter;

import IR.syntaxtree.If;
import IR.token.Identifier;
import IR.token.Label;
import IR.token.FunctionName;

import minijava.*;
import minijava.visitor.*;
import minijava.syntaxtree.*;
import sparrow.*;
import sparrowv.Move_Id_Reg;
import util.*;


public class J2S extends GJNoArguDepthFirst<Identifier>{
    private List<FunctionDecl> functions = new ArrayList<>();
    private List<Instruction> instrs = new ArrayList<>();
    private List<Instruction> tempInstrs = new ArrayList<>();
    private Map<String, Identifier> varMap = new HashMap<>();
    private LinkedHashMap<String, Identifier> fieldMap = new LinkedHashMap<>();
    private LinkedHashMap<String, Identifier> localVarMap = new LinkedHashMap<>();
    private LinkedHashMap<String, Identifier> formalParameterMap = new LinkedHashMap<>();
    private Map<Identifier, Integer> numMap = new HashMap<>();
    private Map<String, Integer> typeMap = new HashMap<>();
    private Map<String, Integer> arrayLengthMap = new HashMap<>();
    private Map<String, Identifier> arrayIdMap = new HashMap<>();
    private Map<String, String> id_to_class = new HashMap<>();
    private int elseLabelCounter = 0;
    private int loopLabelCounter = 0;
    private int endLabelCounter = 0;
    private int tempCounter = 0;
    private int paramTempCounter = 0;
    private int arrayTempCounter = 0;
    private ClassTable classTable;
    private String currentClass;
    private String currentMethod;
    private boolean errorExists = false;

    public J2S(ClassTable ct){
        classTable = ct;
    }

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
    public Identifier visit(MainClass n){
        // create vmts
        instrs.clear();
        create_vtables();

        // broken
        // String functionInitName = "init_vmts";
        // Identifier returnValue = newTemp();
        // instrs.add(new Move_Id_Integer(returnValue, AllocationConstants.DEFAULTRETURN));
        // sparrow.Block block = new sparrow.Block(new ArrayList<>(instrs), returnValue);
        // functions.add(new FunctionDecl(new FunctionName(functionInitName), new ArrayList<>(), block));

        // instrs.clear();
        
        // instrs.add(new Call(newTemp(), new Identifier(functionInitName), new ArrayList<>()));
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);
        n.f6.accept(this);
        n.f7.accept(this);
        n.f8.accept(this);
        n.f9.accept(this);
        n.f10.accept(this);
        n.f11.accept(this);
        n.f12.accept(this);
        n.f13.accept(this);
        n.f14.accept(this);
        n.f15.accept(this);
        n.f16.accept(this);
        n.f17.accept(this);

        Identifier returnValueMain = newTemp();
        instrs.add(new Move_Id_Integer(returnValueMain, AllocationConstants.DEFAULTRETURN));
        sparrow.Block block = new sparrow.Block(new ArrayList<>(instrs), returnValueMain);
        functions.add(new FunctionDecl(new FunctionName("main"), new ArrayList<>(), block));
        
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
    public Identifier visit(ClassDeclaration n){
        String className = n.f1.f0.toString();
        currentClass = className;

        // n.f0.accept(this);
        n.f1.accept(this);

        // add fields to map
        for(int i = 0; i < n.f3.size(); i++){
            VarDeclaration field = (VarDeclaration) n.f3.elementAt(i); 
            Identifier fieldId = newTemp();

            // tempInstrs.add(new Move_Id_Integer(fieldId, AllocationConstants.ZERO));
            fieldMap.put(field.f1.f0.toString(), fieldId);
        }

        //puts fields into its class object address

        // TODO: Methods
        // n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        // n.f5.accept(this);

        fieldMap.clear();
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
    public Identifier visit(MethodDeclaration n){
        System.err.println(currentMethod);
        instrs.clear();
        varMap.clear();
        localVarMap.clear();
        create_vtables();

        // instrs.addAll(tempInstrs);
        // Initialize fields from parent classes and current class
        for (Map.Entry<String, Identifier> field : fieldMap.entrySet()) {
            Identifier fieldId = newTemp();
            instrs.add(new Move_Id_Integer(fieldId, AllocationConstants.ZERO));
            localVarMap.put(field.getKey(), fieldId);
        }

        // copy classes fields into var Map since those shouldn't have been deleted in the first place :(
        varMap.putAll(fieldMap);
        varMap.putAll(localVarMap);

        String methodName = n.f2.f0.toString();
        currentMethod = methodName;

        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);

        String VMTFunctionName = currentClass + "_" + currentMethod;

        // this + formal parameter list
        ArrayList<Identifier> parameters = new ArrayList<>();
        Identifier thisId = new Identifier("this");
        parameters.add(thisId);
        varMap.put("this", thisId);

        FormalParameterList formalParameterList;
        if(n.f4.present()){
            formalParameterList = (FormalParameterList) n.f4.node;
            FormalParameter formalParameter = formalParameterList.f0;
            Identifier formalParameterId = newParamTemp();
            parameters.add(formalParameterId);
            varMap.put(formalParameter.f1.f0.toString(), formalParameterId);
            if(formalParameter.f0.f0.which == TypeConstants.IDENTIFIER){
                minijava.syntaxtree.Identifier node = (minijava.syntaxtree.Identifier) formalParameter.f0.f0.choice;
                id_to_class.put(formalParameter.f1.f0.toString(), node.f0.toString());
            }
            formalParameterMap.put(formalParameter.f1.f0.toString(), formalParameterId);

            NodeListOptional formalParameterRestList = formalParameterList.f1;
            if(formalParameterRestList.present()){
            for(int i = 0; i < formalParameterRestList.size(); i++){
                FormalParameterRest formalParameterRest = (FormalParameterRest) formalParameterRestList.elementAt(i);
                formalParameter = formalParameterRest.f1;
                formalParameterId = newParamTemp();
                parameters.add(formalParameterId);
                varMap.put(formalParameter.f1.f0.toString(), formalParameterId);
                if(formalParameter.f0.f0.which == TypeConstants.IDENTIFIER){
                    minijava.syntaxtree.Identifier node = (minijava.syntaxtree.Identifier) formalParameter.f0.f0.choice;
                    id_to_class.put(formalParameter.f1.f0.toString(), node.f0.toString());
                }
                formalParameterMap.put(formalParameter.f1.f0.toString(), formalParameterId);
            }
            }
        }

        // store local variables into localVarMap
        for (int i = 0; i < n.f7.size(); i++) {
            VarDeclaration varDecl = (VarDeclaration) n.f7.elementAt(i);
            Identifier varId = varDecl.f1.accept(this);
            Identifier localVarId = newTemp();
            localVarMap.put(varId.toString(), localVarId);
            instrs.add(new Move_Id_Integer(localVarId, AllocationConstants.ZERO)); // Initialize to 0
        }

        n.f5.accept(this);
        n.f6.accept(this);
        n.f7.accept(this);
        n.f8.accept(this);
        n.f9.accept(this);
        Identifier returnId = n.f10.accept(this);
        n.f11.accept(this);
        n.f12.accept(this);

        sparrow.Block block = new sparrow.Block(new ArrayList<>(instrs), returnId);

        FunctionDecl f = new FunctionDecl(new FunctionName(VMTFunctionName), parameters, block);
        functions.add(f);

        return null;
    }

    // STATEMENTS

    /**
     * Grammar production:
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    @Override
    public Identifier visit(AssignmentStatement n){
        Identifier lhs = n.f0.accept(this);
        
        Identifier rhs = n.f2.accept(this);

        varMap.put(varMap.get(lhs.toString()).toString(), rhs);

        // if()
        if(localVarMap.containsKey(lhs.toString())){
            instrs.add(new Move_Id_Id(localVarMap.get(lhs.toString()), rhs));
        }
        else if(formalParameterMap.containsKey(lhs.toString())){
            instrs.add(new Move_Id_Id(formalParameterMap.get(lhs.toString()), rhs));
        }
        else{
            int i =0;
            for(Map.Entry<String, Identifier> entry : fieldMap.entrySet()){
                if(entry.getKey().equals(lhs.toString())){
                    break;
                }
                i++;
            }
            instrs.add(new Store(new Identifier("this"), i * AllocationConstants.FOUR_OFFSET + AllocationConstants.FOUR_OFFSET, rhs));
        }

        return null;
    }

    /**
     * Grammar production:
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    @Override
    public Identifier visit(PrintStatement n){
        Identifier lhs = n.f2.accept(this);
        instrs.add(new Print(lhs));
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
    public Identifier visit(IfStatement n){
        Identifier condition = n.f2.accept(this);
        Label elseLabel = newElseLabel();
        Label endLabel = newEndLabel();

        //if
        instrs.add(new IfGoto(condition, elseLabel));

        //then
        n.f4.accept(this);
        instrs.add(new Goto(endLabel));

        //else
        instrs.add(new LabelInstr(elseLabel));
        n.f6.accept(this);

        //end
        instrs.add(new LabelInstr(endLabel));

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
    public Identifier visit(WhileStatement n){
        Label loop = newLoopLabel();
        Label end = newEndLabel();
        
        //while
        instrs.add(new LabelInstr(loop));

        Identifier condition = n.f2.accept(this);
        instrs.add(new IfGoto(condition, end));

        n.f4.accept(this);

        //repeat
        instrs.add(new Goto(loop));

        //end
        instrs.add(new LabelInstr(end));
        return null;
    }


    // EXPRESSIONS
    

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    @Override
    public Identifier visit(MessageSend n){
        Identifier className = n.f0.accept(this);
        Identifier classObjectAddress;
        // if(id_to_class.containsKey(className.toString())){
        //     classObjectAddress = className;
        //     className = new Identifier(id_to_class.get(className.toString()));
        // }
        if(className.toString().charAt(0) == 'p'){
            System.err.println("found p");
        }
        if(localVarMap.containsKey(className.toString())){
            classObjectAddress = localVarMap.get(className.toString()); //todo what if tha varmap has two classes
        }
        else if(fieldMap.containsKey(className.toString())){
            classObjectAddress = newTemp();
            int i =0;
            for(Map.Entry<String, Identifier> entry : fieldMap.entrySet()){
                if(entry.getKey().equals(className.toString())){
                    break;
                }
                i++;
            }
            instrs.add(new Load(classObjectAddress, new Identifier("this"), i * AllocationConstants.FOUR_OFFSET + AllocationConstants.FOUR_OFFSET));
        }
        else{
            classObjectAddress = varMap.get(className.toString());
        }

        // TODO: new A.run() vs a.run()???
        if(className.toString().equals("this")){
            className = new Identifier(currentClass);
        }
        else if(!classTable.classExists(className.toString())){
            className = new Identifier(id_to_class.get(className.toString()));
        }



        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);

        String methodName = n.f2.f0.toString();

        Identifier loadedCall = newTemp();
        // determine which offset the method is
        int index = 0;

        if(className.toString().equals("this")){
            className = new Identifier(currentClass);
        }
        
        for(String key: classTable.getClassInfo(className.toString()).methods.keySet()){
            if(key.equals(methodName)){
                break;
            }
            index++;
        }

        Identifier returnResult = newTemp();


        String vTable = classTable.getClassInfo(className.toString()).vTableName.toString();
        Identifier vTableId = newTemp();
        // instrs.add(new Move_Id_Id(vTableId, new Identifier(vTable))); no need because dynamic now

        // if "this" is the caller
        if(n.f0.accept(this).toString().equals("this")){
            classObjectAddress = n.f0.accept(this);
        }

        instrs.add(new Load(vTableId, classObjectAddress, AllocationConstants.ZERO));
        instrs.add(new Load(loadedCall, vTableId, index * AllocationConstants.FOUR_OFFSET));

        ArrayList<Identifier> parameters = new ArrayList<>();
        if (classObjectAddress != null) {
            parameters.add(classObjectAddress);
        }

        //add parameters
        if(n.f4.present()){
            ExpressionList expressionList = (ExpressionList) n.f4.node;
            Identifier expressionVar = expressionList.f0.accept(this);
            
            parameters.add(varMap.get(expressionVar.toString()));
            if(expressionList.f1.present()){
                for(int i = 0; i < expressionList.f1.size(); i ++){
                    ExpressionRest expressionRest = (ExpressionRest) expressionList.f1.elementAt(i);
                    Identifier expressionRestVar = expressionRest.f1.accept(this);
                    parameters.add(varMap.get(expressionRestVar.toString()));
                }
            }
        }

        instrs.add(new Call(returnResult, loadedCall, parameters));

        varMap.put(returnResult.toString(), returnResult);

        minijava.syntaxtree.Identifier id;
        if(classTable.getClassInfo(className.toString()).methods.get(methodName).returnType.f0.which == TypeConstants.IDENTIFIER){
            id = (minijava.syntaxtree.Identifier) classTable.getClassInfo(className.toString()).methods.get(methodName).returnType.f0.choice;
            String retType = id.f0.toString();
            if (classTable.classExists(retType)) {
                id_to_class.put(returnResult.toString(), retType); 
            }
        }



        return returnResult;
    }

    /**
     * Grammar production:
     * f0 -> "this"
     */
    @Override
    public Identifier visit(ThisExpression n){
        Identifier thisId =  new Identifier("this");
        varMap.put("this", thisId);
        return thisId;
    }


    /**
     * Grammar production:
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    @Override
    public Identifier visit(ArrayAllocationExpression n){
        Identifier length = n.f3.accept(this);
        int allocationSize = AllocationConstants.FOUR_OFFSET * numMap.get(length)+ AllocationConstants.FOUR_OFFSET;

        Identifier newVar = newTemp();
        instrs.add(new Move_Id_Integer(newVar, allocationSize));
        varMap.put(newVar.toString(), newVar);
        
        Identifier newArrayId = newTemp();
        varMap.put(newArrayId.toString(), newArrayId);

        instrs.add(new Alloc(newArrayId, newVar));

        instrs.add(new Store(newArrayId, AllocationConstants.ZERO, length));

        arrayLengthMap.put(newArrayId.toString(), numMap.get(length));

        return newArrayId;
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    @Override
    public Identifier visit(ArrayLookup n){
        Identifier arrayId = n.f0.accept(this);
        Identifier indexId = n.f2.accept(this);
        int index = numMap.get(indexId);

        Identifier loaded = newTemp();
        if(index < 0 || index >= arrayLengthMap.get(varMap.get(varMap.get(arrayId.toString()).toString()).toString())){
            instrs.add(new ErrorMessage("\"array index out of bounds\""));  // Add to IR
            errorExists = true;
        } 

        instrs.add(new Load(loaded, varMap.get(arrayId.toString()), AllocationConstants.FOUR_OFFSET * index + AllocationConstants.FOUR_OFFSET));
        
        varMap.put(loaded.toString(), loaded);
        
        return loaded;
    }

    /**
     * Grammar production:
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    @Override
    public Identifier visit(VarDeclaration n){
        int type = n.f0.f0.which;

        // var declaration
        Identifier variableName = n.f1.accept(this);
        Identifier localVariableId;
        if(localVarMap.containsKey(variableName.toString())){
            localVariableId = localVarMap.get(variableName.toString());
        }
        else{
            localVariableId = newTemp();
        }
        varMap.put(variableName.toString(), localVariableId);
        typeMap.put(variableName.toString(), type);

        if(type == TypeConstants.IDENTIFIER){
            minijava.syntaxtree.Identifier className = (minijava.syntaxtree.Identifier) n.f0.f0.choice;
            id_to_class.put(variableName.toString(), className.f0.toString());
        }

        // all types are init to 0, which is null in sparrow
        instrs.add(new Move_Id_Integer(localVariableId, AllocationConstants.ZERO));

        return null;
    }

    /**
     * Grammar production:
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    @Override
    public Identifier visit(AllocationExpression n){
        
        Identifier classObjectAddress = newTemp();
        
        int numberOfVTable = 1;
        String className = n.f1.f0.toString();
        ClassInfos classInfos = classTable.getClassInfo(className);
        int allocationSize = (classInfos.getNumberOfFields() + numberOfVTable) * AllocationConstants.INTBYTES; // number of fields + vtable
        Identifier allocationSizeTemp = newTemp();
        instrs.add(new Move_Id_Integer(allocationSizeTemp, allocationSize));
        instrs.add(new Alloc(classObjectAddress, allocationSizeTemp));
        
        // TODO: init fields
        for(int i = 0; i < classInfos.getNumberOfFields(); i++){
            Identifier zero = newTemp();
            instrs.add(new Move_Id_Integer(zero, AllocationConstants.ZERO));
            instrs.add(new Store(classObjectAddress, i * AllocationConstants.INTBYTES + AllocationConstants.INTBYTES, zero));
        }

        // store vtable
        instrs.add(new Store(classObjectAddress, AllocationConstants.ZERO, classInfos.vTableName));
        
        varMap.put(className, classObjectAddress);
        return new Identifier(className);
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    @Override
    public Identifier visit(PlusExpression n){
        Identifier lhs = n.f0.accept(this);
        Identifier rhs = n.f2.accept(this);
        Identifier result = newTemp();

        instrs.add(new Add(result, varMap.get(lhs.toString()), varMap.get(rhs.toString())));
        varMap.put(result.toString(), result);
        return result;
    }

    /**
    * f0 -> <INTEGER_LITERAL>
    */
    @Override
    public Identifier visit(IntegerLiteral n){
        Identifier newVar = newTemp();
        instrs.add(new Move_Id_Integer(newVar, Integer.parseInt(n.f0.toString())));
        varMap.put(newVar.toString(), newVar);
        numMap.put(newVar, Integer.parseInt(n.f0.toString()));
        return newVar;
    }

    /**
     * Grammar production:
     * f0 -> "true"
     */
    @Override
    public Identifier visit(TrueLiteral n){
        int boolNum = 1;
        Identifier newVar = newTemp();
        instrs.add(new Move_Id_Integer(newVar, boolNum));
        varMap.put(newVar.toString(), newVar);
        numMap.put(newVar, boolNum);
        return newVar;
    }

    /**
     * Grammar production:
     * f0 -> "false"
     */ 
    @Override
    public Identifier visit(FalseLiteral n){
        int boolNum = 0;
        Identifier newVar = newTemp();
        instrs.add(new Move_Id_Integer(newVar, boolNum));
        varMap.put(newVar.toString(), newVar);
        numMap.put(newVar, boolNum);
        return newVar;
    }



    /**
     * Grammar production:
     * f0 -> <IDENTIFIER>
     */
    @Override
    public Identifier visit(minijava.syntaxtree.Identifier n){
        return new Identifier(n.f0.toString());
    }

    /**
     * Grammar production:
     * f0 -> IntegerLiteral()
     *       | TrueLiteral()
     *       | FalseLiteral()
     *       | Identifier()
     *       | ThisExpression()
     *       | ArrayAllocationExpression()
     *       | AllocationExpression()
     *       | NotExpression()
     *       | BracketExpression()
     */
    @Override
    public Identifier visit(PrimaryExpression n){
        return n.f0.accept(this);
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    @Override
    public Identifier visit(CompareExpression n){
        Identifier lhs = n.f0.accept(this);
        Identifier rhs = n.f2.accept(this);

        Identifier lhsId;
        
        Identifier rhsId;

        if(localVarMap.containsKey(lhs.toString())){
            lhsId = localVarMap.get(lhs.toString());
        }
        else if(fieldMap.containsKey(lhs.toString())){
            lhsId = fieldMap.get(lhs.toString());
        }
        else{
            lhsId = varMap.get(lhs.toString());
        }

        if(localVarMap.containsKey(rhs.toString())){
            rhsId = localVarMap.get(rhs.toString());
        }
        else if(fieldMap.containsKey(rhs.toString())){
            rhsId = fieldMap.get(rhs.toString());
        }
        else{
            rhsId = varMap.get(rhs.toString());
        }

        Identifier result = newTemp();
        instrs.add(new LessThan(result, lhsId, rhsId));
        varMap.put(result.toString(), result);

        return result;
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "&&"
     * f2 -> PrimaryExpression()
     */
    @Override
    public Identifier visit(AndExpression n){
        Identifier first = n.f0.accept(this);
        
        Identifier result = newTemp();
        Label failed = newEndLabel(); //L_false
        Label passed = newEndLabel(); //L_true
        Label meetUp = newEndLabel(); //L_true
        
        // if first is false
        instrs.add(new IfGoto(varMap.get(first.toString()), failed));

        Identifier second = n.f2.accept(this);

        instrs.add(new IfGoto(varMap.get(second.toString()), failed));

        instrs.add(new Goto(passed));
        
        instrs.add(new LabelInstr(failed));
        instrs.add(new Move_Id_Integer(result, AllocationConstants.ZERO));
        instrs.add(new Goto(meetUp));

        instrs.add(new LabelInstr(passed));
        instrs.add(new Move_Id_Integer(result, AllocationConstants.ONE));

        instrs.add(new LabelInstr(meetUp));

        varMap.put(result.toString(), result);
        return result;
    }


    /**
     * Grammar production:
     * f0 -> "!"
     * f1 -> Expression()
     */
    @Override
    public Identifier visit(NotExpression n){
        Identifier condition = n.f1.accept(this);
        
        Identifier result = newTemp();
        Label failed = newEndLabel(); //L_false
        Label passed = newEndLabel(); //L_true
        Label meetUp = newEndLabel(); //L_true
        
        // if condition is false
        instrs.add(new IfGoto(condition, failed));
        instrs.add(new Move_Id_Integer(result, AllocationConstants.ZERO));
        instrs.add(new Goto(meetUp));

        instrs.add(new LabelInstr(failed));
        instrs.add(new Move_Id_Integer(result, AllocationConstants.ONE));
        instrs.add(new Goto(meetUp));

        instrs.add(new LabelInstr(meetUp));
        varMap.put(result.toString(), result);
        return result;
    }

    /**
     * Grammar production:
     * f0 -> AndExpression()
     *       | CompareExpression()
     *       | PlusExpression()
     *       | MinusExpression()
     *       | TimesExpression()
     *       | ArrayLookup()
     *       | ArrayLength() TODO: 
     *       | MessageSend() 
     *       | PrimaryExpression()
     */
    @Override
    public Identifier visit(Expression n){
        Identifier id = n.f0.accept(this);
        return varMap.get(id.toString());
    }

    /**
     * Grammar production:
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    @Override
    public Identifier visit(BracketExpression n){
        Identifier result = n.f1.accept(this);
        return result;
    }

    public List<FunctionDecl> getFunctions(){
        return functions;
    }

    private Identifier newTemp(){
        return new Identifier("v" + (tempCounter++));
    }

    private Label newElseLabel(){
        return new Label("else_" + (elseLabelCounter++));
    }

    private Label newLoopLabel(){
        return new Label("loop_" + (loopLabelCounter++));
    }

    private Label newEndLabel(){
        return new Label("end_" + (endLabelCounter++));
    }

    private Identifier newParamTemp(){
        return new Identifier("param" + (paramTempCounter++));
    }

    private Identifier newTempCustom(String s){
        return new Identifier(s);
    }

    public void create_vtables(){
        for(Map.Entry<String, ClassInfos> entry: classTable.classes.entrySet()){
            String className = entry.getKey();

            //fill vtable
            Identifier vtable = newTempCustom("vmt_" + className);
            int allocSize = classTable.getClassInfo(className).methods.size() * AllocationConstants.INTBYTES;
            Identifier allocSizeId = newTemp();
            instrs.add(new Move_Id_Integer(allocSizeId, allocSize));
            instrs.add(new Alloc(vtable, allocSizeId));
            int i = 0;
            for(HashMap.Entry<String, MethodInfos> methodName : classTable.getClassInfo(className).methods.entrySet()){
                Identifier methodID = newTemp();
                instrs.add(new Move_Id_FuncName(methodID, new FunctionName(className + "_" + methodName.getKey())));
                instrs.add(new Store(vtable, i * AllocationConstants.FOUR_OFFSET, methodID));
                i++;
            }

            classTable.getClassInfo(className).vTableName = vtable;
        }

        System.err.println("====finished creating vTables====");
    }

    public static void main(String[] args) throws Exception {
        InputStream in = System.in;
        new MiniJavaParser(in);
        Goal root = MiniJavaParser.Goal();
        
        ClassTable classTable = new ClassTable();
        FirstPass firstPass = new FirstPass();
        root.accept(firstPass, classTable);

        J2S j2s = new J2S(classTable);
        
        try{
            root.accept(j2s);
        }
        catch (TranslationError e) {
            System.err.println("⚠️ Translation error: " + e.getMessage());
        }


        Program sparrowProgram = new Program(j2s.getFunctions());

        System.out.println(sparrowProgram.toString());
    }
}