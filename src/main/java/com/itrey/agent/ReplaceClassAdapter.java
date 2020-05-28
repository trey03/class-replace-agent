package com.itrey.agent;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author
 */
public class ReplaceClassAdapter extends ClassVisitor {
    private boolean isInterface;
    public ReplaceClassAdapter(ClassVisitor cv) {
        super(Opcodes.ASM5,cv);
    }
    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        cv.visit(version, access, name, signature, superName, interfaces);
        isInterface = (access & Opcodes.ACC_INTERFACE) != 0;
    }
    @Override
    public FieldVisitor visitField(int access, String name, String desc,
                                   String signature, Object value) {
        if (cv != null) {
            return cv.visitField(access, name, getTargetDesc(desc), signature, value);
        }
        return null;
    }
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (!isInterface && mv != null) {
            mv = new ReplaceMethodAdapter(name,mv);
        }
        return mv;
    }
    private String getTargetType(String type){
        String targetClass = Config.getType(type);
        if (targetClass!=null) {
            return targetClass;
        }
        return type;
    }

    private String getTargetDesc(String desc){
        if (desc.length()<=2) {
            return desc;
        }
        String type = desc.substring(1,desc.length()-1);
        String targetType = Config.getType(type);
        if (targetType==null) {
            return desc;
        }
        return "L"+targetType+";";
    }

    class ReplaceMethodAdapter extends MethodVisitor {
        boolean start = false;
        public ReplaceMethodAdapter(String methodName, MethodVisitor mv) {
            super(Opcodes.ASM5,mv);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            if (mv != null) {
                mv.visitFieldInsn(opcode, owner, name, getTargetDesc(desc));
            }
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            if (mv != null && opcode == Opcodes.NEW ) {
                start = true;
                type = getTargetType(type);
            }
            mv.visitTypeInsn(opcode, type);
        }
        @Override
        public void visitMethodInsn(int opcode, String owner, String name,
                                    String desc, boolean itf) {
            if (mv != null && start) {
                owner = getTargetType(owner);
            }
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }
}
