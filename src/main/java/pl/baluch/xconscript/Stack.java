package pl.baluch.xconscript;

import pl.baluch.xconscript.data.DataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Stack {
    private final List<DataType> stack = new ArrayList<>();

    public void push(DataType o) {
        stack.add(o);
    }

    public DataType pop() {
        return stack.remove(stack.size() - 1);
    }

    public DataType peek() {
        return stack.get(stack.size() - 1);
    }

    public int size() {
        return stack.size();
    }

    public Object[] toArray() {
        return stack.stream().map(DataType::getObject).toArray();
    }

    public Stack deepCopy(){
        Stack copy = new Stack();
        copy.stack.addAll(stack);
        return copy;
    }

    @Override
    public String toString() {
        return "Stack{" +
                "stack=[" + stack.stream().map(DataType::getName).collect(Collectors.joining(", ")) +
                "]}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stack stack1 = (Stack) o;
        return Objects.equals(stack, stack1.stack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stack);
    }
}
