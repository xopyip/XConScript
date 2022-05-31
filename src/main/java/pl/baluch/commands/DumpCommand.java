package pl.baluch.commands;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;
import pl.baluch.commands.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.ListIterator;

public class DumpCommand implements Command {
    @Override
    public void execute(CommandArgumentList args) {
        File testFile = args.getArgument(0);
        try {
            FileInputStream fileInputStream = new FileInputStream(testFile);
            byte[] bytes = new byte[1024 * 1024];
            if(fileInputStream.read(bytes) == -1){
                System.err.println("File is empty");
                return;
            }
            fileInputStream.close();
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);
            for (MethodNode method : classNode.methods) {
                System.out.println("\n" + method.name);
                System.out.println(method.signature);
                System.out.println(method.localVariables);
                ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
                System.out.println("instructions");
                while (iterator.hasNext()) {
                    AbstractInsnNode node = iterator.next();
                    if (node instanceof LabelNode) {
                        System.out.println("  Class: " + node.getClass().getSimpleName() + " opcode: " + node.getOpcode() + " args: " + ((LabelNode) node).getLabel());
                    } else if (node instanceof LdcInsnNode) {
                        System.out.println("  Class: " + node.getClass().getSimpleName() + " opcode: " + node.getOpcode() + " args: " + ((LdcInsnNode) node).cst);
                    } else if (node instanceof VarInsnNode) {
                        System.out.println("  Class: " + node.getClass().getSimpleName() + " opcode: " + node.getOpcode() + " args: " + ((VarInsnNode) node).var);
                    } else if (node instanceof FieldInsnNode) {
                        System.out.println("  Class: " + node.getClass().getSimpleName() + " opcode: " + node.getOpcode() + " args: " + ((FieldInsnNode) node).name + " " + ((FieldInsnNode) node).owner + " " + ((FieldInsnNode) node).desc);
                    } else if (node instanceof MethodInsnNode) {
                        System.out.println("  Class: " + node.getClass().getSimpleName() + " opcode: " + node.getOpcode() + " args: " + ((MethodInsnNode) node).owner + " " + ((MethodInsnNode) node).name + " " + ((MethodInsnNode) node).desc);
                    } else if (node instanceof JumpInsnNode) {
                        System.out.println("  Class: " + node.getClass().getSimpleName() + " opcode: " + node.getOpcode() + " args: " + ((JumpInsnNode) node).label);
                    } else if (node instanceof FrameNode) {
                        System.out.println("  Class: " + node.getClass().getSimpleName() + " opcode: " + node.getOpcode() + " args: " + ((FrameNode) node).local);
                    } else if (node instanceof InvokeDynamicInsnNode) {
                        System.out.println("  Class: " + node.getClass().getSimpleName() + " opcode: " + node.getOpcode() + " args: " + ((InvokeDynamicInsnNode) node).name + " " + ((InvokeDynamicInsnNode) node).desc + " " + ((InvokeDynamicInsnNode) node).bsm + " " + Arrays.toString(((InvokeDynamicInsnNode) node).bsmArgs));
                    } else if (node instanceof LineNumberNode) {
                    } else {
                        System.out.println("  !! " + node.getClass().getSimpleName() + " " + node.getOpcode());
                    }

                }
                System.out.println("end");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getName() {
        return "dump";
    }

    @Override
    public String getDescription() {
        return "Dump .class file";
    }

    @Override
    public CommandArgumentListBuilder getArgs() {
        return new CommandArgumentListBuilder()
                .add(CommandArgumentType.FILE.of("class file"));
    }
}
