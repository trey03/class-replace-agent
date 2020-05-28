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

    /**
     * 访问成员属性
     * @param access 访问标志符
     * @param name 变量名
     * @param desc 类型描述
     * @param signature 签名
     * @param value
     * @return
     */
    @Override
    public FieldVisitor visitField(int access, String name, String desc,
                                   String signature, Object value) {
        if (cv != null) {
            return cv.visitField(access, name, getTargetDesc(desc), signature, value);
        }
        return null;
    }

    /**
     * 访问方法，包括构造函数
     * @param access
     * @param name
     * @param desc
     * @param signature
     * @param exceptions
     * @return
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        //如果是接口直接跳过
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

    /**
     * 用于处理在方法域中的指令
     */
    class ReplaceMethodAdapter extends MethodVisitor {
        boolean start = false;
        public ReplaceMethodAdapter(String methodName, MethodVisitor mv) {
            super(Opcodes.ASM5,mv);
        }

        /**
         * 处理成员属性,包括读取、设值等
         * @param opcode
         * @param owner
         * @param name
         * @param desc
         */
        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            if (mv != null) {
                mv.visitFieldInsn(opcode, owner, name, getTargetDesc(desc));
            }
        }

        /**
         * 访问一个类型，比如定义一个对象: Object obj;
         * @param opcode
         * @param type
         */
        @Override
        public void visitTypeInsn(int opcode, String type) {
            //只有经过一次NEW指令后才说明是正常的方法域指令，因为默认构造函数初始化时会自动调用父类的init
            if (mv != null && opcode == Opcodes.NEW ) {
                start = true;
                type = getTargetType(type);
            }
            mv.visitTypeInsn(opcode, type);
        }

        /**
         * 调用一个方法：obj.toString
         * @param opcode
         * @param owner
         * @param name
         * @param desc
         * @param itf
         */
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
