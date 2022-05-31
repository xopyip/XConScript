package pl.baluch.xconscript;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import pl.baluch.xconscript.data.*;
import pl.baluch.xconscript.operations.Operation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class ScriptCompiler {

    public Script compile(String name, ParseContext ctx) throws Exception {
        Script script = new Script(name);
        assert KeywordType.values().length == 25;
        for (String methodName : ctx.getMethods().keySet()) {
            Block parsedMethod = ctx.getMethod(methodName);

            MethodNode methodNode = new MethodNode(Opcodes.ASM4,
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    methodName, createDescriptor(parsedMethod.getArgumentTypes(), parsedMethod.getReturnType().getASMType()),
                    null, null);
            java.util.Stack<Stack> stacksHistory = new java.util.Stack<>();
            Stack stack = new Stack();
            Stack secondStack = null;
            int tempVariablesSize = 0;
            List<VarInsnNode> tempVariables = new ArrayList<>();
            for (Operation<?> op : parsedMethod.getOperations()) {
                assert stack != null;
                Object[] localVarTypes = parsedMethod.getVarTypes();
                if (op instanceof Operation.Push<?> push) {// -> int
                    Object operand = push.getOperand();
                    if (operand instanceof Integer) {
                        pushInt(stack, methodNode, (Integer) op.getOperand());
                    } else if (operand instanceof Long) {
                        pushLong(stack, methodNode, (Long) op.getOperand());
                    } else if (operand instanceof Double) {
                        pushDouble(stack, methodNode, (Double) op.getOperand());
                    } else if (operand instanceof Character) {
                        pushChar(stack, methodNode, (Character) op.getOperand());
                    } else if (operand instanceof String) {
                        pushString(stack, methodNode, (String) op.getOperand());
                    } else {
                        throw new RuntimeException("Unknown operand type: " + operand.getClass());
                    }
                } else if (op instanceof Operation.Add) {//int int -> int
                    mathOperation(stack, methodNode, op,
                            new OperationSignature()
                                    .addOverride(DataType.INT, DataType.INT, DataType.INT, Opcodes.IADD)

                                    .addOverride(DataType.CHAR, DataType.INT, DataType.CHAR, Opcodes.IADD)
                                    .addOverride(DataType.INT, DataType.CHAR, DataType.CHAR, Opcodes.IADD)

                                    .addOverride(DataType.DOUBLE, DataType.DOUBLE, DataType.DOUBLE, Opcodes.DADD)
                                    .addOverride(DataType.DOUBLE, DataType.INT, DataType.DOUBLE, Opcodes.DADD)
                                    .addOverride(DataType.INT, DataType.DOUBLE, DataType.DOUBLE, Opcodes.DADD)

                                    .addOverride(DataType.LONG, DataType.DOUBLE, DataType.DOUBLE, Opcodes.DADD)
                                    .addOverride(DataType.DOUBLE, DataType.LONG, DataType.DOUBLE, Opcodes.DADD)

                                    .addOverride(DataType.LONG, DataType.LONG, DataType.LONG, Opcodes.LADD)
                                    .addOverride(DataType.LONG, DataType.INT, DataType.LONG, Opcodes.LADD)
                                    .addOverride(DataType.INT, DataType.LONG, DataType.LONG, Opcodes.LADD)
                    );
                } else if (op instanceof Operation.Sub) {//int int -> int
                    mathOperation(stack, methodNode, op,
                            new OperationSignature()
                                    .addOverride(DataType.INT, DataType.INT, DataType.INT, Opcodes.ISUB)
                                    .addOverride(DataType.CHAR, DataType.INT, DataType.CHAR, Opcodes.ISUB)
                                    .addOverride(DataType.INT, DataType.CHAR, DataType.CHAR, Opcodes.ISUB)

                                    .addOverride(DataType.DOUBLE, DataType.DOUBLE, DataType.DOUBLE, Opcodes.DSUB)
                                    .addOverride(DataType.DOUBLE, DataType.INT, DataType.DOUBLE, Opcodes.DSUB)
                                    .addOverride(DataType.INT, DataType.DOUBLE, DataType.DOUBLE, Opcodes.DSUB)

                                    .addOverride(DataType.DOUBLE, DataType.LONG, DataType.DOUBLE, Opcodes.DSUB)
                                    .addOverride(DataType.LONG, DataType.DOUBLE, DataType.DOUBLE, Opcodes.DSUB)

                                    .addOverride(DataType.LONG, DataType.LONG, DataType.LONG, Opcodes.LSUB)
                                    .addOverride(DataType.LONG, DataType.INT, DataType.LONG, Opcodes.LSUB)
                                    .addOverride(DataType.INT, DataType.LONG, DataType.LONG, Opcodes.LSUB)
                    );
                } else if (op instanceof Operation.Mul) {//int int -> int
                    mathOperation(stack, methodNode, op,
                            new OperationSignature()
                                    .addOverride(DataType.INT, DataType.INT, DataType.INT, Opcodes.IMUL)
                                    .addOverride(DataType.CHAR, DataType.INT, DataType.CHAR, Opcodes.IMUL)
                                    .addOverride(DataType.INT, DataType.CHAR, DataType.CHAR, Opcodes.IMUL)

                                    .addOverride(DataType.DOUBLE, DataType.DOUBLE, DataType.DOUBLE, Opcodes.DMUL)
                                    .addOverride(DataType.DOUBLE, DataType.INT, DataType.DOUBLE, Opcodes.DMUL)
                                    .addOverride(DataType.INT, DataType.DOUBLE, DataType.DOUBLE, Opcodes.DMUL)

                                    .addOverride(DataType.DOUBLE, DataType.LONG, DataType.DOUBLE, Opcodes.DMUL)
                                    .addOverride(DataType.LONG, DataType.DOUBLE, DataType.DOUBLE, Opcodes.DMUL)

                                    .addOverride(DataType.LONG, DataType.LONG, DataType.LONG, Opcodes.LMUL)
                                    .addOverride(DataType.LONG, DataType.INT, DataType.LONG, Opcodes.LMUL)
                                    .addOverride(DataType.INT, DataType.LONG, DataType.LONG, Opcodes.LMUL)
                    );
                } else if (op instanceof Operation.Div) {//int int -> int
                    mathOperation(stack, methodNode, op,
                            new OperationSignature()
                                    .addOverride(DataType.INT, DataType.INT, DataType.INT, Opcodes.IDIV)
                                    .addOverride(DataType.CHAR, DataType.INT, DataType.CHAR, Opcodes.IDIV)
                                    .addOverride(DataType.INT, DataType.CHAR, DataType.CHAR, Opcodes.IDIV)

                                    .addOverride(DataType.DOUBLE, DataType.DOUBLE, DataType.DOUBLE, Opcodes.DDIV)
                                    .addOverride(DataType.DOUBLE, DataType.INT, DataType.DOUBLE, Opcodes.DDIV)
                                    .addOverride(DataType.INT, DataType.DOUBLE, DataType.DOUBLE, Opcodes.DDIV)

                                    .addOverride(DataType.DOUBLE, DataType.LONG, DataType.DOUBLE, Opcodes.DDIV)
                                    .addOverride(DataType.LONG, DataType.DOUBLE, DataType.DOUBLE, Opcodes.DDIV)

                                    .addOverride(DataType.LONG, DataType.LONG, DataType.LONG, Opcodes.LDIV)
                                    .addOverride(DataType.LONG, DataType.INT, DataType.LONG, Opcodes.LDIV)
                                    .addOverride(DataType.INT, DataType.LONG, DataType.LONG, Opcodes.LDIV)
                    );
                } else if (op instanceof Operation.Pow) {//int2 int1 -> int
                    if (stack.size() < 2 || stack.pop() != DataType.INT || stack.pop() != DataType.INT) {
                        throw new Exception("Wrong arguments for " + op + " operation");
                    }
                    stack.push(DataType.INT);
                    methodNode.instructions.add(new InsnNode(Opcodes.I2D));//int2 double1 double1
                    methodNode.instructions.add(new InsnNode(Opcodes.DUP2_X1));//double1 double1 int double1 double1
                    methodNode.instructions.add(new InsnNode(Opcodes.POP2));//double1 double1 int
                    methodNode.instructions.add(new InsnNode(Opcodes.I2D));//double1 double1 double2 double2
                    methodNode.instructions.add(new InsnNode(Opcodes.DUP2_X2));//double2 double2 double1 double1 double2 double2
                    methodNode.instructions.add(new InsnNode(Opcodes.POP2));//double2 double2 double1 double1
                    methodNode.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false));
                    methodNode.instructions.add(new InsnNode(Opcodes.D2I));
                } else if (op instanceof Operation.Mod) {//int2 int1 -> int
                    mathOperation(stack, methodNode, op,
                            new OperationSignature()
                                    .addOverride(DataType.INT, DataType.INT, DataType.INT, Opcodes.IREM)
                                    .addOverride(DataType.CHAR, DataType.INT, DataType.CHAR, Opcodes.IREM)
                                    .addOverride(DataType.INT, DataType.CHAR, DataType.CHAR, Opcodes.IREM)

                                    .addOverride(DataType.DOUBLE, DataType.DOUBLE, DataType.DOUBLE, Opcodes.DADD)
                                    .addOverride(DataType.DOUBLE, DataType.INT, DataType.DOUBLE, Opcodes.DADD)
                                    .addOverride(DataType.INT, DataType.DOUBLE, DataType.DOUBLE, Opcodes.DADD)
                    );
                } else if (op instanceof Operation.Gt) {//int int -> int
                    mathComparison(stack, methodNode, op, localVarTypes, Opcodes.IF_ICMPLE);
                } else if (op instanceof Operation.Lt) {//int int -> int
                    mathComparison(stack, methodNode, op, localVarTypes, Opcodes.IF_ICMPGE);
                } else if (op instanceof Operation.Gte) {//int int -> int
                    mathComparison(stack, methodNode, op, localVarTypes, Opcodes.IF_ICMPLT);
                } else if (op instanceof Operation.Lte) {//int int -> int
                    mathComparison(stack, methodNode, op, localVarTypes, Opcodes.IF_ICMPGT);
                } else if (op instanceof Operation.Eq) {//int int -> int
                    mathComparison(stack, methodNode, op, localVarTypes, Opcodes.IF_ICMPNE);
                } else if (op instanceof Operation.Neq) {//int int -> int
                    mathComparison(stack, methodNode, op, localVarTypes, Opcodes.IF_ICMPEQ);
                } else if (op instanceof Operation.Log) {
                    if (stack.size() < 1) {
                        throw new Exception("Wrong arguments for " + op + " operation");
                    }
                    DataType type = stack.pop();
                    methodNode.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
                    if (DataType.isType2(type)) {
                        methodNode.instructions.add(new InsnNode(Opcodes.DUP_X2));
                        methodNode.instructions.add(new InsnNode(Opcodes.POP));
                    } else {
                        methodNode.instructions.add(new InsnNode(Opcodes.SWAP));
                    }
                    if (DataType.INT.equals(type)) {
                        methodNode.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", "(I)V", false));
                    } else if (DataType.LONG.equals(type)) {
                        methodNode.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", "(J)V", false));
                    } else if (DataType.DOUBLE.equals(type)) {
                        methodNode.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", "(D)V", false));
                    } else if (DataType.STRING.equals(type)) {
                        methodNode.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/String;)V", false));
                    } else if (DataType.CHAR.equals(type)) {
                        methodNode.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", "(C)V", false));
                    } else {
                        methodNode.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/Object;)V", false));
                    }
                } else if (op instanceof Operation.Dup) {
                    if (stack.size() < 1) {
                        throw new Exception("Wrong arguments for " + op + " operation");
                    }
                    DataType type = stack.pop();
                    stack.push(type);
                    stack.push(type);
                    if (DataType.isType2(type)) {
                        methodNode.instructions.add(new InsnNode(Opcodes.DUP2));
                    } else {
                        methodNode.instructions.add(new InsnNode(Opcodes.DUP));
                    }
                } else if (op instanceof Operation.Swap) {
                    if (stack.size() < 2) {
                        throw new Exception("Wrong arguments for " + op + " operation");
                    }
                    DataType type2 = stack.pop();
                    DataType type1 = stack.pop();
                    stack.push(type2);
                    stack.push(type1);
                    if (DataType.isType2(type2) && !DataType.isType2(type1)) {
                        methodNode.instructions.add(new InsnNode(Opcodes.DUP2_X1));
                        methodNode.instructions.add(new InsnNode(Opcodes.POP2));
                    } else if (!DataType.isType2(type2) && DataType.isType2(type1)) {
                        methodNode.instructions.add(new InsnNode(Opcodes.DUP_X2));
                        methodNode.instructions.add(new InsnNode(Opcodes.POP));
                    } else if (DataType.isType2(type2) && DataType.isType2(type1)) {
                        methodNode.instructions.add(new InsnNode(Opcodes.DUP2_X2));
                        methodNode.instructions.add(new InsnNode(Opcodes.POP2));
                    } else {
                        methodNode.instructions.add(new InsnNode(Opcodes.SWAP));
                    }
                } else if (op instanceof Operation.Drop) {
                    if (stack.size() < 1) {
                        throw new Exception("Wrong arguments for " + op + " operation");
                    }
                    DataType pop = stack.pop();
                    if (DataType.isType2(pop)) {
                        methodNode.instructions.add(new InsnNode(Opcodes.POP2));
                    } else {
                        methodNode.instructions.add(new InsnNode(Opcodes.POP));
                    }
                } else if (op instanceof Operation.Drop2) {
                    if (stack.size() < 2) {
                        throw new Exception("Wrong arguments for " + op + " operation");
                    }
                    DataType s1 = stack.pop();
                    DataType s2 = stack.pop();
                    if (!DataType.isType2(s1) && !DataType.isType2(s2)) {
                        methodNode.instructions.add(new InsnNode(Opcodes.POP2));
                    } else {
                        if (DataType.isType2(s1)) {
                            methodNode.instructions.add(new InsnNode(Opcodes.POP2));
                        } else {
                            methodNode.instructions.add(new InsnNode(Opcodes.POP));
                        }
                        if (DataType.isType2(s2)) {
                            methodNode.instructions.add(new InsnNode(Opcodes.POP2));
                        } else {
                            methodNode.instructions.add(new InsnNode(Opcodes.POP));
                        }
                    }
                } else if (op instanceof Operation.Label) {
                    LabelNode operand = ((Operation.Label) op).getOperand();
                    methodNode.instructions.add(operand);
                    methodNode.instructions.add(new FrameNode(Opcodes.F_FULL, localVarTypes.length, localVarTypes, stack.size(), stack.toArray()));
                } else if (op instanceof Operation.Jump jump) {
                    if (((Operation.Jump) op).getOpcode() != Opcodes.GOTO && stack.pop() != DataType.INT) {
                        throw new Exception("Wrong arguments for " + op + " operation");
                    }
                    methodNode.instructions.add(new JumpInsnNode(jump.getOpcode(), jump.getOperand()));
                } else if (op instanceof Operation.SplitExecution) {
                    stacksHistory.push(secondStack);
                    secondStack = stack.deepCopy();
                } else if (op instanceof Operation.SwitchExecution) {
                    Stack tmp = stack;
                    stack = secondStack;
                    secondStack = tmp;
                } else if (op instanceof Operation.SplitExecutionEnd) {
                    if (!stack.equals(secondStack)) {
                        throw new Exception("Split execution ended with different stacks: " + secondStack + " and " + stack);
                    }
                    secondStack = stacksHistory.pop();
                } else if (op instanceof Operation.Var var) {
                    String varName = var.getOperand();
                    initVariable(stack, methodNode, parsedMethod, varName, var.getType());
                } else if (op instanceof Operation.SetVar var) {
                    if (stack.size() < 1) {
                        throw new Exception("Wrong arguments for " + op + " operation");
                    }
                    String varName = var.getOperand();
                    if (!parsedMethod.hasVariable(varName)) {
                        throw new Exception("Variable " + varName + " is not defined");
                    }
                    LocalVariable localVariable = parsedMethod.getVariable(varName);
                    if (stack.pop() != localVariable.type()) {
                        throw new Exception("Wrong type of value for variable " + varName + ": " + stack.pop() + " instead of " + localVariable.type());
                    }
                    methodNode.instructions.add(new VarInsnNode(localVariable.type().getASMType().getOpcode(Opcodes.ISTORE), localVariable.idx()));
                } else if (op instanceof Operation.LoadVar var) {
                    String varName = var.getOperand();
                    if (!parsedMethod.hasVariable(varName)) {
                        throw new Exception("Variable " + varName + " is not defined");
                    }
                    LocalVariable localVariable = parsedMethod.getVariable(varName);
                    stack.push(localVariable.type());
                    methodNode.instructions.add(new VarInsnNode(localVariable.type().getASMType().getOpcode(Opcodes.ILOAD), localVariable.idx()));
                } else if (op instanceof Operation.PushField) {
                    boolean isFieldStatic = (((Operation.PushField) op).getOperand2().getModifiers() & Opcodes.ACC_STATIC) != 0;
                    if (isFieldStatic) {
                        methodNode.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC,
                                ((Operation.PushField) op).getOperand().getName().replace(".", "/"),
                                ((Operation.PushField) op).getOperand2().getName(),
                                Type.getType(((Operation.PushField) op).getOperand2().getType()).getDescriptor()));
                    } else {
                        if (stack.size() < 1) {
                            throw new Exception("Not enough arguments for " + op + " operation");
                        }
                        DataType type = stack.pop();
                        if (type.getTypeClass() != ((Operation.PushField) op).getOperand()) {
                            throw new Exception("Wrong type of object for field " + ((Operation.PushField) op).getOperand2().getName() + ": " + type.getTypeClass().getName() + " instead of " + ((Operation.PushField) op).getOperand().getName());
                        }
                        methodNode.instructions.add(new FieldInsnNode(Opcodes.GETFIELD,
                                ((Operation.PushField) op).getOperand().getName().replace(".", "/"),
                                ((Operation.PushField) op).getOperand2().getName(),
                                Type.getType(((Operation.PushField) op).getOperand2().getType()).getDescriptor()));
                    }
                    stack.push(DataType.getByClass(((Operation.PushField) op).getOperand2().getType()));
                } else if (op instanceof Operation.PutField) {
                    Field field = ((Operation.PutField) op).getOperand();
                    Class<?> owner = field.getDeclaringClass();
                    boolean isFieldStatic = (field.getModifiers() & Opcodes.ACC_STATIC) != 0;
                    if (isFieldStatic) {
                        if (stack.size() < 1) {
                            throw new Exception("Not enough arguments for " + op + " operation");
                        }
                        DataType type = stack.pop();
                        if (type.getTypeClass() != field.getType()) {
                            throw new Exception("Wrong type of object for field " + field.getName() + ": " + type.getTypeClass().getName() + " instead of " + field.getType());
                        }
                        methodNode.instructions.add(new FieldInsnNode(Opcodes.PUTSTATIC,
                                owner.getName().replace(".", "/"),
                                field.getName(),
                                Type.getType(field.getType()).getDescriptor()));
                    } else {
                        if (stack.size() < 2) {
                            throw new Exception("Not enough arguments for " + op + " operation");
                        }
                        DataType type = stack.pop();
                        if (type.getTypeClass() != owner) {
                            throw new Exception("Wrong type of object for field " + field.getName() + ": " + type.getTypeClass().getName() + " instead of " + field.getName());
                        }
                        DataType type2 = stack.pop();
                        if (type2.getTypeClass() != field.getType()) {
                            throw new Exception("Wrong type of object for field " + field.getName() + ": " + type2.getTypeClass().getName() + " instead of " + field.getType());
                        }
                        if (DataType.isType2(type)) {
                            methodNode.instructions.add(new InsnNode(Opcodes.DUP_X2));
                            methodNode.instructions.add(new InsnNode(Opcodes.POP));
                        } else {
                            methodNode.instructions.add(new InsnNode(Opcodes.SWAP));
                        }
                        methodNode.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD,
                                owner.getName().replace(".", "/"),
                                field.getName(),
                                Type.getType(field.getType()).getDescriptor()));
                    }
                } else if (op instanceof Operation.CallConstructor) {
                    Constructor<?> constructor = ((Operation.CallConstructor) op).getOperand();
                    Class<?> owner = constructor.getDeclaringClass();

                    methodNode.instructions.add(new TypeInsnNode(Opcodes.NEW, owner.getName().replace(".", "/")));
                    Type[] argumentTypes = Arrays.stream(constructor.getParameterTypes()).map(Type::getType).toArray(Type[]::new);

                    if (stack.size() < argumentTypes.length) {
                        throw new Exception("Not enough arguments for constructor " + constructor.getName());
                    }
                    DataType[] args = new DataType[argumentTypes.length];
                    for (int i = 0; i < argumentTypes.length; i++) {
                        args[argumentTypes.length - 1 - i] = stack.pop();
                    }
                    if (!Utils.checkConstructor(constructor, args)) {
                        throw new Exception("Wrong arguments for " + op + " operation");
                    }
                    String desc = createDescriptor(argumentTypes, Type.VOID_TYPE);

                    boolean isType2 = Arrays.stream(args).anyMatch(DataType::isType2);
                    if (!isType2 && args.length == 0) {
                        methodNode.instructions.add(new InsnNode(Opcodes.DUP));
                    } else if (!isType2 && args.length == 1) {
                        //arg obj
                        methodNode.instructions.add(new InsnNode(Opcodes.DUP)); //arg obj obj
                        methodNode.instructions.add(new InsnNode(Opcodes.DUP2_X1)); //obj obj arg obj obj
                        methodNode.instructions.add(new InsnNode(Opcodes.POP2)); //obj obj arg
                    } else {
                        useTempVar(methodNode, tempVariables, Opcodes.ASTORE, 0);
                        //rotate variables to put object reference as first argument
                        int currentTempSize = 0;
                        for (int i = argumentTypes.length - 1; i >= 0; i--) {
                            argumentTypes[i] = tryCasting(methodNode, argumentTypes[i]);
                            Type argumentType = argumentTypes[i];
                            currentTempSize += argumentType.getSize();
                            useTempVar(methodNode, tempVariables, argumentType.getOpcode(Opcodes.ISTORE), 1 + i);
                        }
                        useTempVar(methodNode, tempVariables, Opcodes.ALOAD, 0);
                        useTempVar(methodNode, tempVariables, Opcodes.ALOAD, 0);

                        tempVariablesSize = Math.max(tempVariablesSize, currentTempSize);
                        for (int i = 0; i < argumentTypes.length; i++) {
                            Type argumentType = argumentTypes[i];
                            useTempVar(methodNode, tempVariables, argumentType.getOpcode(Opcodes.ILOAD), 1 + i);
                        }
                    }

                    methodNode.instructions.add(new MethodInsnNode(
                            Opcodes.INVOKESPECIAL,
                            owner.getName().replace(".", "/"),
                            "<init>", desc, owner.isInterface()));

                    stack.push(DataType.getByClass(constructor.getDeclaringClass()));
                } else if (op instanceof Operation.CallMethod) {
                    Method method = ((Operation.CallMethod) op).getOperand();
                    Class<?> owner = method.getDeclaringClass();
                    boolean isMethodStatic = (method.getModifiers() & Opcodes.ACC_STATIC) != 0;
                    Type[] argumentTypes = Type.getArgumentTypes(method);

                    if (stack.size() < argumentTypes.length + (isMethodStatic ? 0 : 1)) {
                        throw new Exception("Not enough arguments for method " + method.getName());
                    }
                    if (!isMethodStatic) {
                        DataType object = stack.pop();
                        if (object.getTypeClass() != owner) {
                            throw new Exception("Wrong object type for method " + method.getName() + ": " + object.getTypeClass() + " instead of " + ((Operation.CallMethod) op).getOperand().getName());
                        }
                    }
                    DataType[] args = new DataType[argumentTypes.length];
                    for (int i = 0; i < argumentTypes.length; i++) {
                        args[argumentTypes.length - 1 - i] = stack.pop();
                    }
                    if (!Utils.checkMethod(method, args, method.getName())) {
                        throw new Exception("Wrong arguments for " + op + " operation");
                    }
                    String desc = createDescriptor(argumentTypes, Type.getType(method.getReturnType()));

                    //rotate variables to put object reference as first argument
                    int currentTempSize = 0;
                    if (!isMethodStatic) {
                        currentTempSize += Type.getType(owner).getSize();
                        useTempVar(methodNode, tempVariables, Opcodes.ASTORE, 0);
                    }
                    for (int i = argumentTypes.length - 1; i >= 0; i--) {
                        argumentTypes[i] = tryCasting(methodNode, argumentTypes[i]);
                        Type argumentType = argumentTypes[i];
                        currentTempSize += argumentType.getSize();
                        useTempVar(methodNode, tempVariables, argumentType.getOpcode(Opcodes.ISTORE), i + (isMethodStatic ? 0 : 1));
                    }
                    tempVariablesSize = Math.max(tempVariablesSize, currentTempSize);

                    if (!isMethodStatic) {
                        useTempVar(methodNode, tempVariables, Opcodes.ALOAD, 0);
                    }
                    for (int i = 0; i < argumentTypes.length; i++) {
                        useTempVar(methodNode, tempVariables, argumentTypes[i].getOpcode(Opcodes.ILOAD), i + (isMethodStatic ? 0 : 1));
                    }

                    methodNode.instructions.add(new MethodInsnNode(
                            isMethodStatic ? Opcodes.INVOKESTATIC : Opcodes.INVOKEVIRTUAL,
                            owner.getName().replace(".", "/"),
                            method.getName(), desc, owner.isInterface()));
                    if (method.getReturnType() != void.class)
                        stack.push(DataType.getByClass(method.getReturnType()));
                } else if (op instanceof Operation.CallSelfMethod) {
                    String callMethodName = ((Operation.CallSelfMethod) op).getOperand();
                    Block callMethod = ctx.getMethod(callMethodName);

                    if (stack.size() < callMethod.getArgumentTypes().length) {
                        throw new Exception("Not enough arguments for method " + callMethodName);
                    }
                    DataType[] args = new DataType[callMethod.getArgumentTypes().length];
                    for (int i = 0; i < callMethod.getArgumentTypes().length; i++) {
                        args[callMethod.getArgumentTypes().length - 1 - i] = stack.pop();
                    }
                    if (!Utils.checkSelfMethod(callMethod, args)) {
                        throw new Exception("Wrong arguments for " + op + " operation");
                    }
                    String desc = Arrays.stream(callMethod.getArgumentTypes()).map(Type::getDescriptor).collect(Collectors.joining(""));
                    desc = "(" + desc + ")" + callMethod.getReturnType().getASMType().getDescriptor();

                    methodNode.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            script.getName().replace(".", "/"),
                            callMethodName, desc, false));
                    if (callMethod.getReturnType() != DataType.VOID)
                        stack.push(callMethod.getReturnType());
                } else if (op instanceof Operation.Dump) {
                    methodNode.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "java/NonExistingClass",
                            "nonExistingMethod",
                            "(Ljava/lang/System;)V",
                            false));
                } else {
                    throw new Exception("Unknown operation: " + op);
                }
            }

            assert stack != null;
            if (parsedMethod.getReturnType() != DataType.VOID) {
                if (stack.size() == 0) {
                    throw new Exception("Missing return element on stack in function " + methodName);
                }
                if (stack.size() > 1) {
                    throw new Exception("Too many elements on stack " + stack);
                }
                DataType returnType = stack.pop();
                if (!returnType.equals(parsedMethod.getReturnType())) {
                    throw new Exception("Wrong return type " + returnType.getName() + " expected " + parsedMethod.getReturnType().getName());
                }
            } else if (stack.size() != 0) {
                throw new Exception("Unhandled data on stack when compiling method " + methodName + "! " + stack);
            }
            if (stacksHistory.size() != 0) {
                throw new Exception("Unclosed split stack block! " + stacksHistory);
            }
            methodNode.maxLocals = parsedMethod.localVarSize;
            for (VarInsnNode tempVariable : tempVariables) {
                tempVariable.var += methodNode.maxLocals;
            }
            methodNode.maxLocals += tempVariablesSize;
            methodNode.instructions.add(new InsnNode(parsedMethod.getReturnType().getASMType().getOpcode(Opcodes.IRETURN)));
            script.addMethod(methodName, methodNode);
        }
        return script;
    }

    private void useTempVar(MethodNode method, List<VarInsnNode> tempVariables, int opcode, int offset) {
        VarInsnNode objectVar = new VarInsnNode(opcode, offset);
        tempVariables.add(objectVar);
        method.instructions.add(objectVar);
    }

    private String createDescriptor(Type[] argumentTypes, Type returnType) {
        return "(" + Arrays.stream(argumentTypes).map(Type::getDescriptor).collect(Collectors.joining("")) + ")" + returnType.getDescriptor();
    }

    private Type tryCasting(MethodNode method, Type methodArgumentType) {
        if (!methodArgumentType.getDescriptor().startsWith("L")) {
            return methodArgumentType;
        }
        switch (methodArgumentType.getDescriptor()) {
            case "Ljava/lang/Integer;" -> {
                method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false));
                return Type.getType("Ljava/lang/Integer;");
            }
            case "Ljava/lang/Long;" -> {
                method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Long;", "valueOf", "(J)Ljava/lang/Long;;", false));
                return Type.getType("Ljava/lang/Long;");
            }
            case "Ljava/lang/Double;" -> {
                method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double;", "valueOf", "(D)Ljava/lang/Double;;", false));
                return Type.getType("Ljava/lang/Double;");
            }
            case "Ljava/lang/Character;" -> {
                method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false));
                return Type.getType("Ljava/lang/Character;");
            }
        }
        return methodArgumentType;
    }

    private void mathComparison(Stack stack, MethodNode methodNode, Operation<?> op, Object[] localVarTypes, int opcode) throws Exception {
        if (stack.size() < 2) {
            throw new Exception("Not enough elements on stack for math operation " + op);
        }
        DataType type1 = stack.pop();
        DataType type2 = stack.pop();
        //todo: compare between different types
        if (type1.equals(DataType.DOUBLE) && type2.equals(DataType.DOUBLE)) {
            methodNode.instructions.add(new InsnNode(Opcodes.DCMPG));
            methodNode.instructions.add(new InsnNode(Opcodes.ICONST_0));
            type1 = DataType.INT;
            type2 = DataType.INT;
        }else if (type1.equals(DataType.LONG) && type2.equals(DataType.LONG)) {
            methodNode.instructions.add(new InsnNode(Opcodes.LCMP));
            methodNode.instructions.add(new InsnNode(Opcodes.ICONST_0));
            type1 = DataType.INT;
            type2 = DataType.INT;
        } else if (!type1.equals(DataType.INT) || !type2.equals(DataType.INT)) {
            throw new Exception("Invalid types for math operation " + op + " " + type1 + " " + type2);
        }
        LabelNode leBranch = new LabelNode();
        LabelNode endBranch = new LabelNode();
        methodNode.instructions.add(new JumpInsnNode(opcode, leBranch)); //less equal
        methodNode.instructions.add(new InsnNode(Opcodes.ICONST_1));
        stack.push(DataType.INT);
        methodNode.instructions.add(new JumpInsnNode(Opcodes.GOTO, endBranch));
        methodNode.instructions.add(leBranch);
        methodNode.instructions.add(new FrameNode(Opcodes.F_FULL, localVarTypes.length, localVarTypes, stack.size() - 1, stack.toArray()));
        methodNode.instructions.add(new InsnNode(Opcodes.ICONST_0));
        methodNode.instructions.add(endBranch);
        methodNode.instructions.add(new FrameNode(Opcodes.F_FULL, localVarTypes.length, localVarTypes, stack.size(), stack.toArray()));
    }

    private void mathOperation(Stack stack, MethodNode methodNode, Operation<?> op, OperationSignature signature) throws Exception {
        if (stack.size() < 2) {
            throw new Exception("Wrong arguments for " + op + " operation");
        }
        DataType topValue = stack.pop();
        DataType bottomValue = stack.pop();
        for (OperationSignature.InsOuts override : signature.getOverrides()) {
            if (override.in1 == topValue && override.in2 == bottomValue) {
                stack.push(override.out);
                castToEqualType(methodNode, override.out, topValue, bottomValue);
                methodNode.instructions.add(new InsnNode(override.opcode));
                return;
            }
        }
        throw new Exception("Wrong arguments for " + op + " operation: " + topValue.getASMType() + " " + bottomValue.getASMType());
    }

    private void castToEqualType(MethodNode methodNode, DataType out, DataType topValue, DataType bottomValue) {
        if (out == DataType.DOUBLE) {
            if (bottomValue == DataType.DOUBLE && topValue == DataType.INT) { //double int () -> double
                methodNode.instructions.add(new InsnNode(Opcodes.I2D));
            } else if (bottomValue == DataType.INT && topValue == DataType.DOUBLE) { //int double () -> double
                methodNode.instructions.add(new InsnNode(Opcodes.DUP2_X1));
                methodNode.instructions.add(new InsnNode(Opcodes.POP2));
                methodNode.instructions.add(new InsnNode(Opcodes.I2D));
                methodNode.instructions.add(new InsnNode(Opcodes.DUP2_X2));
                methodNode.instructions.add(new InsnNode(Opcodes.POP2));
            } else if(bottomValue == DataType.DOUBLE && topValue == DataType.LONG) { //double long () -> double
                methodNode.instructions.add(new InsnNode(Opcodes.L2D));
            }else if(bottomValue == DataType.LONG && topValue == DataType.DOUBLE) { //long double () -> double
                methodNode.instructions.add(new InsnNode(Opcodes.DUP2_X2));
                methodNode.instructions.add(new InsnNode(Opcodes.POP2));
                methodNode.instructions.add(new InsnNode(Opcodes.L2D));
                methodNode.instructions.add(new InsnNode(Opcodes.DUP2_X2));
                methodNode.instructions.add(new InsnNode(Opcodes.POP2));
            }
        }else if(out == DataType.LONG){
            if (bottomValue == DataType.LONG && topValue == DataType.INT) { //long int () -> long
                methodNode.instructions.add(new InsnNode(Opcodes.I2L));
            } else if (bottomValue == DataType.INT && topValue == DataType.LONG) { //int long () -> long
                methodNode.instructions.add(new InsnNode(Opcodes.DUP2_X1));
                methodNode.instructions.add(new InsnNode(Opcodes.POP2));
                methodNode.instructions.add(new InsnNode(Opcodes.I2L));
                methodNode.instructions.add(new InsnNode(Opcodes.DUP2_X2));
                methodNode.instructions.add(new InsnNode(Opcodes.POP2));
            }
        }
    }

    private void pushChar(Stack stack, MethodNode methodNode, char value) {
        stack.push(DataType.CHAR);
        methodNode.instructions.add(new IntInsnNode(Opcodes.BIPUSH, value));
    }

    private void pushString(Stack stack, MethodNode methodNode, String value) {
        stack.push(DataType.STRING);
        methodNode.instructions.add(new LdcInsnNode(value));
    }

    private void pushInt(Stack stack, MethodNode methodNode, int value) {
        stack.push(DataType.INT);
        if (value >= -1 && value <= 5) {
            methodNode.instructions.add(new InsnNode(Opcodes.ICONST_0 + value));
        } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            methodNode.instructions.add(new IntInsnNode(Opcodes.BIPUSH, value));
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            methodNode.instructions.add(new IntInsnNode(Opcodes.SIPUSH, value));
        } else {
            methodNode.instructions.add(new LdcInsnNode(value));
        }
    }

    private void pushLong(Stack stack, MethodNode methodNode, long value) {
        stack.push(DataType.LONG);
        methodNode.instructions.add(new LdcInsnNode(value));
    }

    private void pushDouble(Stack stack, MethodNode methodNode, double value) {
        stack.push(DataType.DOUBLE);
        methodNode.instructions.add(new LdcInsnNode(value));
    }

    private void initVariable(Stack stack, MethodNode methodNode, Block currentMethod, String varName, DataType type) {
        int i = currentMethod.getVariable(varName).idx();
        //set default values
        if (type == DataType.STRING) {
            pushString(stack, methodNode, "");
            stack.pop();
            methodNode.instructions.add(new VarInsnNode(Opcodes.ASTORE, i));
        } else if (type == DataType.INT) {
            pushInt(stack, methodNode, 0);
            stack.pop();
            methodNode.instructions.add(new VarInsnNode(Opcodes.ISTORE, i));
        } else if (type == DataType.CHAR) {
            pushChar(stack, methodNode, '\0');
            stack.pop();
            methodNode.instructions.add(new VarInsnNode(Opcodes.ISTORE, i));
        } else {
            methodNode.instructions.add(new InsnNode(Opcodes.ACONST_NULL));
            methodNode.instructions.add(new VarInsnNode(Opcodes.ASTORE, i));
        }
    }

}
