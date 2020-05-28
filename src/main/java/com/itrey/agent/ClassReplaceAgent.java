package com.itrey.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class ClassReplaceAgent {
    /**
     * 以vm参数的方式载入，在Java程序的main方法执行之前执行
     */
    public static void premain(String agentArgs, Instrumentation instrumentation){
        System.err.println("装载成功 方法 premain 参数：" + agentArgs);

        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                if (className.startsWith("com/itrey")){
                    try {
                        ClassReader cr = new ClassReader(className);
                        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                        ClassVisitor classAdapter = new ReplaceClassAdapter(cw);
                        //使给定的访问者访问Java类的ClassReader
                        cr.accept(classAdapter, ClassReader.SKIP_DEBUG);
                        byte[] data = cw.toByteArray();
                        //return toByteArray(loader.getResourceAsStream(targetClass+".class"));
                        return data;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


                return null;
            }
        });
    }
    /**
     * 以Attach的方式载入，在Java程序启动后执行
     */
    public static void agentmain(String agentArgs, Instrumentation inst){
        System.err.println("装载成功 方法 agentmain 参数：" + agentArgs);
    }
}
