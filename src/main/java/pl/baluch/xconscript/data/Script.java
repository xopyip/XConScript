package pl.baluch.xconscript.data;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import pl.baluch.xconscript.ScriptClassLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class Script {
    private final ClassNode classNode;
    private final String name;
    private boolean dirty = true;
    private static final ScriptClassLoader scriptClassLoader = new ScriptClassLoader();

    public Script(String name) {
        this.name = name;

        classNode = new ClassNode();
        classNode.access = Opcodes.ACC_PUBLIC;
        classNode.name = name;
        classNode.version = 52;
        classNode.superName = "java/lang/Object";
    }

    public void saveClass(File outputFile) {
        dirty = false;
        ClassWriter cWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        if (getMethods().stream().noneMatch(methodNode -> methodNode.name.equals("main") && methodNode.desc.equalsIgnoreCase("([Ljava/lang/String;)V"))) {
            System.out.println("Adding entrypoint to main()V");
            MethodNode entryPoint = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
            entryPoint.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, name, "main", "()V", false));
            entryPoint.instructions.add(new InsnNode(Opcodes.RETURN));
            classNode.methods.add(entryPoint);
        }

        classNode.accept(cWriter);

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(cWriter.toByteArray());
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Class<?> getScriptClass() throws ClassNotFoundException {
        //todo: calculate hash of each method
        if (!dirty) {
            return scriptClassLoader.findClass(classNode.name);
        }
        dirty = false;
        ClassWriter cWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(cWriter);

        scriptClassLoader.loadScript(classNode.name, cWriter.toByteArray());

        try {
            return scriptClassLoader.findClass(classNode.name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void addMethod(String name, MethodNode methodNode) {
        this.classNode.methods.removeIf(methodNode1 -> methodNode1.name.equals(name));
        this.classNode.methods.add(methodNode);
    }

    public ClassNode getClassNode() {
        return classNode;
    }

    public List<MethodNode> getMethods() {
        return this.classNode.methods;
    }

    public boolean containsMethod(String name) {
        return this.classNode.methods.stream().anyMatch(methodNode -> methodNode.name.equals(name));
    }
}
