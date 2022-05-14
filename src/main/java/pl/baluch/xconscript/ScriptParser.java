package pl.baluch.xconscript;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.LabelNode;
import pl.baluch.xconscript.data.*;
import pl.baluch.xconscript.operations.Operation;
import pl.baluch.xconscript.tokens.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Stack;

public class ScriptParser {
    private static final BlockType METHOD_BLOCK_TYPE = new BlockType(BlockType.BlockScope.GLOBAL, "method", true) {
        @Override
        public void endBlock(Block block, Stack<Block> blockSet, ParseContext ctx) {
        }
    };
    private static final BlockType IF_BLOCK_TYPE = new BlockType(BlockType.BlockScope.LOCAL, "if", false) {
        @Override
        public void endBlock(Block currentBlock, Stack<Block> blockSet, ParseContext ctx) throws Exception {
            if (currentBlock.hasNoOpsToSetup()) {
                throw new Exception("END without THEN or ELSE");
            }
            LabelNode label = new LabelNode(new Label());
            currentBlock.addOperation(new Operation.Label(label));
            Operation<?> opToSetup = currentBlock.getOpToSetup();
            assert opToSetup instanceof Operation.Jump;
            ((Operation.Jump) opToSetup).setLabel(label);
            currentBlock.addOperation(new Operation.SplitExecutionEnd());
            blockSet.peek().addOperations(currentBlock.getOperations());
        }
    };
    private static final BlockType WHILE_BLOCK_TYPE = new BlockType(BlockType.BlockScope.LOCAL, "while", false) {
        @Override
        public void endBlock(Block whileBlock, Stack<Block> blockSet, ParseContext ctx) throws Exception {
            if (whileBlock.hasNoOpsToSetup()) {
                throw new Exception("END without WHILE DO");
            }
            Operation<?> doOp = whileBlock.getOpToSetup();
            if (whileBlock.hasNoOpsToSetup()) {
                throw new Exception("END without WHILE DO");
            }
            Operation<?> whileOp = whileBlock.getOpToSetup();
            LabelNode whileLabel = (LabelNode) whileOp.getOperand();
            whileBlock.addOperation(new Operation.Jump(Opcodes.GOTO, whileLabel));
            LabelNode label = new LabelNode(new Label());
            whileBlock.addOperation(new Operation.Label(label));
            ((Operation.Jump) doOp).setLabel(label);
            whileBlock.addOperation(new Operation.SplitExecutionEnd());
            blockSet.peek().addOperations(whileBlock.getOperations());
        }
    };

    private final Map<String, Method> globalMethods = new HashMap<>();
    private final Map<String, BlockType> blockTypes = new HashMap<>();

    public ScriptParser(StdLib ... libs) {
        blockTypes.put("method", METHOD_BLOCK_TYPE);
        blockTypes.put("if", IF_BLOCK_TYPE);
        for(StdLib lib : libs) {
            include(lib);
        }
    }

    public ParseContext parse(List<Token<?>> tokens) throws Exception {
        ParseContext ctx = new ParseContext();
        Stack<Block> blockSet = new Stack<>();
        //todo: fix checking redefinition problems in method, var, struct, struct var
        while (!tokens.isEmpty()) {
            Token<?> token = tokens.remove(0);
            if (token instanceof PushToken) {
                if (blockSet.isEmpty()) {
                    throw new TokenException(token, "Push " + token.getValue().getClass().getSimpleName() + " token found outside of method");
                }
                blockSet.peek().addOperation(new Operation.Push<>(((PushToken<?>) token).getValue()));
            } else if (token instanceof WordToken) {
                assert KeywordType.values().length == 25;
                KeywordType type = KeywordType.getType(((WordToken) token).getValue());

                if (type != null) {
                    switch (type) {
                        case THEN -> {
                            if (blockSet.isEmpty()) {
                                throw new TokenException(token, "Then token found outside of method");
                            }
                            Block peek = blockSet.peek();
                            if (peek.getBlockType() != IF_BLOCK_TYPE) {
                                throw new TokenException(token, "Then token found outside of if block");
                            }

                            Operation.Jump jump = new Operation.Jump(Opcodes.IFEQ);
                            blockSet.peek().addOperation(jump);
                            peek.addOpToSetup(jump);
                            blockSet.peek().addOperation(new Operation.SplitExecution());
                        }
                        case ELSE -> {
                            if (blockSet.isEmpty()) {
                                throw new TokenException(token, "Else token found outside of method");
                            }
                            Block peek = blockSet.peek();
                            if (peek.getBlockType() != IF_BLOCK_TYPE) {
                                throw new TokenException(token, "Then token found outside of if block");
                            }
                            if (peek.hasNoOpsToSetup()) {
                                throw new TokenException(token, "ELSE without THEN");
                            }
                            blockSet.peek().addOperation(new Operation.SwitchExecution());
                            LabelNode label = new LabelNode(new Label());
                            Operation.Jump gotoOp = new Operation.Jump(Opcodes.GOTO);
                            Operation<?> opToSetup = peek.getOpToSetup();
                            peek.addOpToSetup(gotoOp);
                            blockSet.peek().addOperation(gotoOp);
                            blockSet.peek().addOperation(new Operation.Label(label));
                            assert opToSetup instanceof Operation.Jump;
                            ((Operation.Jump) opToSetup).setLabel(label);
                        }
                        case END -> {
                            if (blockSet.isEmpty()) {
                                throw new TokenException(token, "End token found outside of block");
                            }
                            //todo: proper type check in parsing (check if blocks ends with proper return value)
                            Block currentBlock = blockSet.pop();
                            currentBlock.getBlockType().endBlock(currentBlock, blockSet, ctx);
                        }
                        case WHILE -> {
                            if (blockSet.isEmpty()) {
                                throw new TokenException(token, "While token found outside of method");
                            }
                            blockSet.push(new Block(blockSet.peek(), WHILE_BLOCK_TYPE));
                            LabelNode label = new LabelNode(new Label());
                            Operation.Label whileLabel = new Operation.Label(label);
                            blockSet.peek().addOperation(whileLabel);
                            blockSet.peek().addOpToSetup(whileLabel);
                            //todo: check if while only introduces one stack value type boolean
                        }
                        case DO -> {
                            if (blockSet.isEmpty()) {
                                throw new TokenException(token, "Do token found outside of method");
                            }
                            Block peek = blockSet.peek();
                            if (peek.getBlockType() != WHILE_BLOCK_TYPE) {
                                throw new TokenException(token, "Do token found outside of while");
                            }
                            Operation.Jump jumpOp = new Operation.Jump(Opcodes.IFEQ);
                            peek.addOpToSetup(jumpOp);
                            blockSet.peek().addOperation(jumpOp);
                            blockSet.peek().addOperation(new Operation.SplitExecution());
                        }
                        case LOG -> {
                            if (blockSet.isEmpty()) {
                                throw new TokenException(token, "Log token found outside of method");
                            }
                            blockSet.peek().addOperation(new Operation.Log());
                        }
                        case ADD -> {
                            if (blockSet.isEmpty()) {
                                throw new TokenException(token, "Add token found outside of method");
                            }
                            blockSet.peek().addOperation(new Operation.Add());
                        }
                        case SUB -> {
                            if (blockSet.isEmpty()) {
                                throw new TokenException(token, "Sub token found outside of method");
                            }
                            blockSet.peek().addOperation(new Operation.Sub());
                        }
                        case MUL -> {
                            if (blockSet.isEmpty()) {
                                throw new TokenException(token, "Mul token found outside of method");
                            }
                            blockSet.peek().addOperation(new Operation.Mul());
                        }
                        case DIV -> {
                            if (blockSet.isEmpty()) {
                                throw new TokenException(token, "Div token found outside of method");
                            }
                            blockSet.peek().addOperation(new Operation.Div());
                        }
                        case POW -> {
                            if (blockSet.isEmpty()) {
                                throw new TokenException(token, "Pow token found outside of method");
                            }
                            blockSet.peek().addOperation(new Operation.Pow());
                        }
                        case MOD -> {
                            if (blockSet.isEmpty()) {
                                throw new TokenException(token, "Mod token found outside of method");
                            }
                            blockSet.peek().addOperation(new Operation.Mod());
                        }
                        case EQ -> {
                            if (blockSet.isEmpty()) {
                                throw new TokenException(token, "Eq token found outside of method");
                            }
                            blockSet.peek().addOperation(new Operation.Eq());
                        }
                        case NEQ -> {
                            if (blockSet.isEmpty()) {
                                throw new TokenException(token, "Neq token found outside of method");
                            }
                            blockSet.peek().addOperation(new Operation.Neq());
                        }
                        case GT -> {
                            if (blockSet.isEmpty()) {
                                throw new TokenException(token, "Gt token found outside of method");
                            }
                            blockSet.peek().addOperation(new Operation.Gt());
                        }
                        case LT -> {
                            if (blockSet.isEmpty()) {
                                throw new TokenException(token, "Lt token found outside of method");
                            }
                            blockSet.peek().addOperation(new Operation.Lt());
                        }
                        case GTE -> {
                            if (blockSet.isEmpty()) {
                                throw new TokenException(token, "Gte token found outside of method");
                            }
                            blockSet.peek().addOperation(new Operation.Gte());
                        }
                        case LTE -> {
                            if (blockSet.isEmpty()) {
                                throw new TokenException(token, "Lte token found outside of method");
                            }
                            blockSet.peek().addOperation(new Operation.Lte());
                        }
                        case SWAP -> {
                            if (blockSet.isEmpty()) {
                                throw new TokenException(token, "Swap token found outside of method");
                            }
                            blockSet.peek().addOperation(new Operation.Swap());
                        }
                        case DUP -> {
                            if (blockSet.isEmpty()) {
                                throw new TokenException(token, "Dup token found outside of method");
                            }
                            blockSet.peek().addOperation(new Operation.Dup());
                        }
                        case DROP -> {
                            if (blockSet.isEmpty()) {
                                throw new TokenException(token, "Drop token found outside of method");
                            }
                            blockSet.peek().addOperation(new Operation.Drop());
                        }
                        case DROP2 -> {
                            if (blockSet.isEmpty()) {
                                throw new TokenException(token, "Drop2 token found outside of method");
                            }
                            blockSet.peek().addOperation(new Operation.Drop2());
                        }
                        case VAR -> {
                            if (blockSet.isEmpty()) {
                                throw new TokenException(token, "Var token found outside of method");
                            }
                            if (tokens.size() < 2) {
                                throw new TokenException(token, "VAR without variable name or type");
                            }
                            WordToken varName = (WordToken) tokens.remove(0);
                            String varNameStr = varName.getValue();
                            if (KeywordType.getType(varNameStr) != null) {
                                throw new TokenException(token, "Expected var name but got keyword");
                            }
                            if (!varNameStr.matches("[0-9a-zA-Z-_]+")) {
                                throw new TokenException(token, "Var name must contain only alphanumeric values");
                            }
                            if (ctx.getMethod(varNameStr) != null) {
                                throw new TokenException(token, "Cannot override method " + varNameStr + " with variable");
                            }
                            Token<?> varType = tokens.remove(0);
                            DataType dataType = DataType.getByName(ctx, ((WordToken) varType).getValue());
                            if (dataType == null) {
                                throw new TokenException(token, "Expected var type but got " + varType.getValue());
                            }
                            blockSet.peek().addVariable(varNameStr, dataType);
                            blockSet.peek().addOperation(new Operation.Var(varNameStr, dataType, false));
                        }
                        case IMPORT -> {
                            if (tokens.size() < 2) {
                                throw new TokenException(token, "IMPORT without classname and name");
                            }
                            if (!(tokens.get(0) instanceof WordToken) || !(tokens.get(1) instanceof WordToken)) {
                                throw new TokenException(token, "Expected classname and name but got " + tokens.get(0).getValue() + " and " + tokens.get(1).getValue());
                            }
                            String className = ((WordToken) tokens.remove(0)).getValue();
                            String localName = ((WordToken) tokens.remove(0)).getValue();
                            Class<?> importClass = null;
                            Stack<String> importStack = new Stack<>();
                            while (importClass == null) {
                                try {
                                    importClass = Class.forName(className);
                                } catch (Exception e) {
                                    int i = className.lastIndexOf('.');
                                    if (i == -1) {
                                        throw new TokenException(token, e.getMessage());
                                    }
                                    importStack.push(className.substring(i + 1));
                                    className = className.substring(0, i);
                                }
                            }
                            while (importStack.size() > 0) {
                                String innerClassName = importStack.pop();
                                boolean found = false;
                                for (Class<?> aClass : importClass.getClasses()) {
                                    if (aClass.getSimpleName().equals(innerClassName)) {
                                        importClass = aClass;
                                        found = true;
                                    }
                                }
                                if (found)
                                    continue;
                                throw new TokenException(token, "Could not find class " + innerClassName + " in " + importClass.getName());
                            }
                            DataType.registerAlias(localName, importClass);
                        }
                        case DUMP -> blockSet.peek().addOperation(new Operation.Dump());
                        default -> throw new TokenException(token, "Unknown keyword: " + token.value);
                    }
                } else if (((WordToken) token).getValue().startsWith(">")) {
                    if (blockSet.isEmpty()) {
                        throw new TokenException(token, "Cannot use > outside of method");
                    }
                    String varName = ((WordToken) token).getValue().substring(1);
                    if (blockSet.peek().hasVariable(varName)) {
                        blockSet.peek().addOperation(new Operation.SetVar(varName));
                    } else {
                        parseJavaToken(ctx, blockSet.peek(), varName, true);
                    }
                } else if (((WordToken) token).getValue().endsWith(">")) {
                    if (blockSet.isEmpty()) {
                        throw new TokenException(token, "Cannot use > outside of method");
                    }
                    String varName = ((WordToken) token).getValue();
                    varName = varName.substring(0, varName.length() - 1);
                    if (blockSet.peek().hasVariable(varName)) {
                        blockSet.peek().addOperation(new Operation.LoadVar(varName));
                    } else {
                        parseJavaToken(ctx, blockSet.peek(), varName, false);
                    }
                } else if (ctx.getMethods().containsKey(((WordToken) token).getValue())) {
                    if (blockSet.isEmpty()) {
                        throw new TokenException(token, "Cannot use method outside of method");
                    }
                    blockSet.peek().addOperation(new Operation.CallSelfMethod(((WordToken) token).getValue()));
                } else if (globalMethods.containsKey(((WordToken) token).getValue())) {
                    if (blockSet.isEmpty()) {
                        throw new TokenException(token, "Cannot use method outside of method");
                    }
                    Method method = globalMethods.get(((WordToken) token).getValue());
                    blockSet.peek().addOperation(new Operation.CallMethod(method));
                } else if (blockTypes.containsKey(((WordToken) token).getValue())) {
                    BlockType blockType = blockTypes.get(((WordToken) token).getValue());
                    switch (blockType.getScope()) {
                        case GLOBAL:
                            if (!blockSet.isEmpty()) {
                                throw new TokenException(token, "Cannot use block `" + blockType.getName() + "` inside method");
                            }
                            break;
                        case LOCAL:
                            if (blockSet.isEmpty()) {
                                throw new TokenException(token, "Cannot use block `" + blockType.getName() + "` inside method");
                            }
                            break;
                    }
                    Block block;
                    DataType returnType = blockType.getReturnType();
                    List<MethodArgument> arguments = new ArrayList<>(blockType.getArguments());
                    if (blockType.hasName()) {
                        if (tokens.size() < 1) {
                            throw new TokenException(token, "Expected name for block `" + blockType.getName() + "`");
                        }
                        Token<?> nameToken = tokens.remove(0);
                        if (!(nameToken instanceof WordToken)) {
                            throw new TokenException(token, "Method name expected");
                        }
                        String blockName = ((WordToken) nameToken).getValue();
                        if (blockType == METHOD_BLOCK_TYPE) { //parsing method signature
                            if (!blockName.contains("(") && tokens.get(0) instanceof WordToken && ((WordToken) tokens.get(0)).getValue().startsWith("(")) {
                                //join with arguments after name
                                WordToken argsToken = (WordToken) tokens.remove(0);
                                blockName += argsToken.getValue();
                            }
                            if (blockName.contains("(")) {
                                //method with params
                                while (!blockName.contains(")")) {
                                    if (tokens.size() < 1) {
                                        throw new TokenException(token, "Method arguments list not closed");
                                    }
                                    Token<?> methodArgsPart = tokens.remove(0);
                                    if (!(methodArgsPart instanceof WordToken)) {
                                        throw new TokenException(token, "Expected word but got " + methodArgsPart);
                                    }
                                    if (((WordToken) methodArgsPart).getValue().contains("(")) {
                                        throw new TokenException(token, "Method arguments list not closed");
                                    }
                                    blockName += " " + ((WordToken) methodArgsPart).getValue();
                                }
                                String params = blockName.substring(blockName.indexOf("(") + 1, blockName.indexOf(")"));
                                String[] paramsArray = params.split(",");
                                for (String param : paramsArray) {
                                    String[] paramParts = param.trim().split(" ");
                                    if (paramParts.length != 2) {
                                        throw new TokenException(token, "Invalid method argument");
                                    }
                                    arguments.add(new MethodArgument(paramParts[0], DataType.getByName(ctx, paramParts[1])));
                                }
                                blockName = blockName.substring(0, blockName.indexOf("("));
                            }
                            if (tokens.size() < 1) {
                                throw new TokenException(token, "Missing function return type");
                            }
                            Token<?> returnTypeToken = tokens.remove(0);
                            if (!(returnTypeToken instanceof WordToken)) {
                                throw new TokenException(token, "Expected return type");
                            }
                            returnType = DataType.getByName(ctx, ((WordToken) returnTypeToken).getValue());
                            if (returnType == null) {
                                throw new TokenException(token, "Unknown return type " + ((WordToken) returnTypeToken).getValue());
                            }
                        }
                        if (!blockName.matches("[0-9a-zA-Z-_]+")) {
                            throw new TokenException(token, "Method name must contain only alphanumeric values, got " + blockName);
                        }
                        block = new Block(blockSet.isEmpty() ? null : blockSet.peek(), blockType, blockName, arguments, returnType);
                    } else {
                        block = new Block(blockSet.isEmpty() ? null : blockSet.peek(), blockType, null, arguments, returnType);
                    }
                    if (block.getBlockType() == METHOD_BLOCK_TYPE) {
                        ctx.addMethod(block.getBlockName(), block);
                    }
                    blockSet.push(block);
                } else if (!blockSet.isEmpty() && blockSet.peek().getBlockType().hasContextBlock(((WordToken) token).getValue())) {
                    ContextBlockType<?> contextBlockType = blockSet.peek().getBlockType().getContextBlock(((WordToken) token).getValue());
                    String contextBlockName = ((WordToken) token).getValue();
                    List<Token<?>> contextBlockTokens = new ArrayList<>();
                    while (true) {
                        if(tokens.size() < 1) {
                            throw new TokenException(token, "Missing context block end");
                        }
                        Token<?> contextBlockToken = tokens.remove(0);
                        if (contextBlockToken instanceof WordToken && KeywordType.getType(((WordToken) contextBlockToken).getValue()) == KeywordType.END) {
                            break;
                        }
                        contextBlockTokens.add(contextBlockToken);
                    }
                    blockSet.peek().registerContextBlock(contextBlockName, contextBlockType.parse(contextBlockTokens));
                } else {
                    String text = ((WordToken) token).getValue();
                    try {
                        parseJavaToken(ctx, blockSet.peek(), text, false);
                    } catch (Exception e) {
                        throw new TokenException(token, "Unknown word: " + text);
                    }

                }

            } else {
                throw new TokenException(token, "Unknown token: " + token);
            }
        }
        if (!blockSet.isEmpty()) {
            throw new Exception("Method not closed");
        }
        return ctx;
    }

    private void parseJavaToken(ParseContext ctx, Block currentMethod, String val, boolean put) throws Exception {
        if (currentMethod == null) {
            throw new Exception("Cannot use java token outside of method");
        }
        if (val.contains("(")) {
            //method call
            String classAndMethodName = val.substring(0, val.indexOf("("));
            String args = val.substring(val.indexOf("(") + 1, val.indexOf(")", val.indexOf("(")));
            DataType[] params = Arrays.stream(args.split(","))
                    .filter(s -> !s.isEmpty())
                    .map(s -> DataType.getByName(ctx, s))
                    .toArray(DataType[]::new);
            String toProcess = val.substring(val.indexOf(")", val.indexOf("(")) + 1);
            if (toProcess.length() > 0) {
                toProcess = toProcess.substring(1);
            }
            Stack<String> callPath = new Stack<>();
            Class<?> classToCall = null;
            //lookup for class
            while (classToCall == null) {
                try {

                    if (DataType.hasAliasDefined(classAndMethodName)) {
                        classToCall = DataType.getAliasClass(classAndMethodName);
                    } else if (currentMethod.hasVariable(classAndMethodName)) {
                        currentMethod.addOperation(new Operation.LoadVar(classAndMethodName));
                        classToCall = currentMethod.getVariable(classAndMethodName).type().getTypeClass();
                        if (callPath.size() == 0) {
                            throw new Exception("Cannot call method on variable");
                        }
                    } else {
                        classToCall = Class.forName(classAndMethodName);
                    }
                } catch (ClassNotFoundException e) {
                    int dotIdx = classAndMethodName.lastIndexOf('.');
                    if (dotIdx == -1) {
                        throw new Exception("Class not found: " + classAndMethodName);
                    }
                    callPath.push(classAndMethodName.substring(dotIdx + 1));
                    classAndMethodName = classAndMethodName.substring(0, dotIdx);
                }
            }
            if (callPath.size() == 0) {
                //constructor
                for (Constructor<?> constructor : classToCall.getConstructors()) {
                    if (Utils.checkConstructor(constructor, params)) {
                        currentMethod.addOperation(new Operation.CallConstructor(classToCall, constructor));
                        break;
                    }
                }
            } else {
                //fields
                outer:
                while (callPath.size() > 0) {
                    String pop = callPath.pop();
                    if (callPath.size() > 0) {
                        for (Class<?> declaredClass : classToCall.getDeclaredClasses()) {
                            if (declaredClass.getSimpleName().equals(pop)) {
                                classToCall = declaredClass;
                                continue outer;
                            }
                        }
                        Field declaredField = classToCall.getDeclaredField(pop);
                        currentMethod.addOperation(new Operation.PushField(classToCall, declaredField));
                        classToCall = declaredField.getType();
                    } else {
                        Method method = findMethod(params, classToCall, pop);
                        currentMethod.addOperation(new Operation.CallMethod(method));
                        classToCall = method.getReturnType();
                    }
                }
            }

            //process rest of java path
            String[] split = toProcess.split("\\.");
            for (String s : split) {
                if (s.length() == 0) {
                    continue;
                }
                if (s.contains("(")) {
                    //method call
                    String methodName = s.substring(0, s.indexOf("("));
                    String arguments = s.substring(s.indexOf("(") + 1, s.indexOf(")", s.indexOf("(")));
                    Method method = findMethod(Arrays.stream(arguments.split(","))
                            .filter(ss -> !ss.isEmpty())
                            .map(ss -> DataType.getByName(ctx, ss))
                            .toArray(DataType[]::new), classToCall, methodName);
                    currentMethod.addOperation(new Operation.CallMethod(method));
                    classToCall = method.getReturnType();
                } else {
                    Field declaredField = classToCall.getDeclaredField(s);
                    if (put && callPath.size() == 0) {
                        currentMethod.addOperation(new Operation.PutField(declaredField));
                    } else {
                        currentMethod.addOperation(new Operation.PushField(classToCall, declaredField));
                        classToCall = declaredField.getType();
                    }
                }
            }
        } else {

            Stack<String> callPath = new Stack<>();
            Class<?> classToCall = null;
            while (classToCall == null) {
                try {
                    if (DataType.hasAliasDefined(val)) {
                        classToCall = DataType.getAliasClass(val);
                    } else if (currentMethod.hasVariable(val)) {
                        currentMethod.addOperation(new Operation.LoadVar(val));
                        classToCall = currentMethod.getVariable(val).type().getTypeClass();
                        if (callPath.size() == 0) {
                            throw new Exception("Unreachable");
                        }
                    } else {
                        classToCall = Class.forName(val);
                    }
                } catch (ClassNotFoundException e) {
                    int dotIdx = val.lastIndexOf('.');
                    if (dotIdx == -1) {
                        throw new Exception("Class not found: " + val);
                    }
                    callPath.push(val.substring(dotIdx + 1));
                    val = val.substring(0, dotIdx);
                }
            }
            while (callPath.size() > 0) {
                String pop = callPath.pop();
                Field declaredField = classToCall.getDeclaredField(pop);
                if (put && callPath.size() == 0) {
                    currentMethod.addOperation(new Operation.PutField(declaredField));
                } else {
                    currentMethod.addOperation(new Operation.PushField(classToCall, declaredField));
                    classToCall = declaredField.getType();
                }
            }
        }
    }

    private Method findMethod(DataType[] params, Class<?> classToCall, String name) throws NoSuchMethodException {
        for (Method method : classToCall.getDeclaredMethods()) {
            if (Utils.checkMethod(method, params, name)) {
                return method;
            }
        }
        throw new NoSuchMethodException("Method not found: " + classToCall.getName() + "." + name);
    }

    public void registerMethod(String globalName, Class<?> owner, String methodName, Class<?>... args) {
        Method method = null;
        try {
            method = owner.getDeclaredMethod(methodName, args);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (method == null) {
            throw new RuntimeException("Method not found: " + owner.getName() + "." + methodName);
        }

        globalMethods.put(globalName, method);
    }

    public void registerBlock(BlockType blockType) {
        this.blockTypes.put(blockType.getName(), blockType);
    }

    private void include(StdLib lib){
        lib.globalMethods(globalMethods);
        lib.blockTypes(blockTypes);
    }
}
