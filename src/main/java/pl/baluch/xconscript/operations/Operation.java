package pl.baluch.xconscript.operations;

import org.objectweb.asm.tree.LabelNode;
import pl.baluch.xconscript.data.DataType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Operation<T> {
    protected T operand1;

    public Operation(T operand1) {
        this.operand1 = operand1;
    }

    public T getOperand() {
        assert operand1 != null;
        return operand1;
    }

    @Override
    public String toString() {
        if (operand1 == null) {
            return getClass().getSimpleName();
        }
        return getClass().getSimpleName() + "{" +
                "operand1=" + operand1 +
                '}';
    }

    public static class BiOperation<T, R> extends Operation<T> {
        protected R operand2;

        public BiOperation(T operand1, R operand2) {
            super(operand1);
            this.operand2 = operand2;
        }

        public R getOperand2() {
            return operand2;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{" +
                    "operand1=" + operand1 +
                    ",operand2=" + operand2 +
                    '}';
        }
    }

    private static class VoidOperation extends Operation<Void> {
        public VoidOperation() {
            super(null);
        }
    }

    public static class Push<T> extends Operation<T> {
        public Push(T value) {
            super(value);
        }
    }

    public static class Log extends VoidOperation { //todo: add type of log as operand
    }

    public static class Add extends VoidOperation {
    }

    public static class Sub extends VoidOperation {
    }

    public static class Mul extends VoidOperation {
    }

    public static class Div extends VoidOperation {
    }

    public static class Pow extends VoidOperation {
    }

    public static class Mod extends VoidOperation {
    }

    public static class Eq extends VoidOperation {
    }

    public static class Neq extends VoidOperation {
    }

    public static class Gt extends VoidOperation {
    }

    public static class Lt extends VoidOperation {
    }

    public static class Gte extends VoidOperation {
    }

    public static class Lte extends VoidOperation {
    }

    public static class Label extends Operation<LabelNode> {
        public Label(LabelNode s) {
            super(s);
        }
    }

    public static class Jump extends Operation<LabelNode> {
        private final int opcode;

        public Jump(int opcode) {
            super(null);
            this.opcode = opcode;
        }

        public Jump(int opcode, LabelNode labelNode) {
            super(labelNode);
            this.opcode = opcode;
        }

        public int getOpcode() {
            return opcode;
        }

        public void setLabel(LabelNode label) {
            this.operand1 = label;
        }

        @Override
        public String toString() {
            return "Jump{" +
                    "label=" + operand1 +
                    ", opcode=" + opcode +
                    '}';
        }
    }

    public static class Dup extends VoidOperation {
    }

    public static class Swap extends VoidOperation {
    }

    public static class Drop extends VoidOperation {
    }

    public static class Drop2 extends VoidOperation {
    }

    public static class SplitExecution extends VoidOperation {
    }

    public static class SwitchExecution extends VoidOperation {
    }

    public static class SplitExecutionEnd extends VoidOperation {
    }

    public static class Var extends Operation<String> {
        private final DataType type;
        private final boolean fromStack;

        public Var(String name, DataType type, boolean fromStack) {
            super(name);
            this.fromStack = fromStack;
            if(type == DataType.VOID){
                throw new IllegalArgumentException("Void type is not allowed");
            }
            this.type = type;
        }

        public DataType getType() {
            return type;
        }

        public boolean isFromStack() {
            return fromStack;
        }
    }

    public static class SetVar extends Operation<String> {
        public SetVar(String name) {
            super(name);
        }
    }

    public static class LoadVar extends Operation<String> {
        public LoadVar(String name) {
            super(name);
        }
    }

    public static class PushField extends BiOperation<Class<?>, Field> {
        public PushField(Class<?> owner, Field field) {
            super(owner, field);
        }
    }

    public static class CallMethod extends Operation<Method> {
        public CallMethod(Method method) {
            super(method);
        }
    }

    public static class CallSelfMethod extends Operation<String> {
        public CallSelfMethod(String value) {
            super(value);
        }
    }

    public static class CallConstructor extends Operation<Constructor<?>> {
        public CallConstructor(Class<?> classToCall, Constructor<?> constructor) {
            super(constructor);
        }
    }

    public static class PutField extends Operation<Field> {
        public PutField(Field declaredField) {
            super(declaredField);
        }
    }

    public static class Dump extends VoidOperation {
    }
}
