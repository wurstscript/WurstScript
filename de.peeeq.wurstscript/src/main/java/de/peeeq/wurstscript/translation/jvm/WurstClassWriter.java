package de.peeeq.wurstscript.translation.jvm;


import org.objectweb.asm.ClassWriter;

/**
 *
 */
public class WurstClassWriter extends ClassWriter {
    public WurstClassWriter() {
        super(ClassWriter.COMPUTE_FRAMES /* | ClassWriter.COMPUTE_MAXS */);
    }


    @Override
    protected String getCommonSuperClass(String a, String b) {
        // TODO make more precise?
        return "Ljava/lang/Object;";
    }
}
