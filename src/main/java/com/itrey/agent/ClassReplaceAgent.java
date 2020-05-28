package com.itrey.agent;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Properties;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class ClassReplaceAgent {
    private static final String KEY_REP_PREFIX = "rep.";
    private static final String CONFIG_FILE_NAME = "config_file";
    private static final String KEY_SCAN = "scan";

    private static Properties properties = new Properties();

    /**
     * 以vm参数的方式载入，在Java程序的main方法执行之前执行
     */
    public static void premain(String agentArgs, Instrumentation instrumentation){
        System.err.println("装载成功 方法 premain 参数：" + agentArgs);
        //加载配置
        loadConfig();
        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                if (className.startsWith(Config.scan.replaceAll("\\.","/"))){
                    try {
                        ClassReader cr = new ClassReader(className);
                        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                        ClassVisitor classAdapter = new ReplaceClassAdapter(cw);
                        cr.accept(classAdapter, ClassReader.SKIP_DEBUG);
                        byte[] data = cw.toByteArray();
                        return data;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        });
    }

    private static void loadConfig()  {
        String fileName = System.getProperty(CONFIG_FILE_NAME);
        if (fileName==null) {
            return;
        }
        try (FileInputStream fis = new FileInputStream(fileName)){
            properties.load(fis);
            Set<Object> keys = properties.keySet();
            for (Object k : keys) {
                String key = k.toString();
                if (!key.startsWith(KEY_REP_PREFIX)) {
                    continue;
                }
                Config.addReplace(key.substring(KEY_REP_PREFIX.length()),properties.getProperty(key));
            }
            Config.scan = properties.getProperty(KEY_SCAN,"_UNDEFIND_");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 以Attach的方式载入，在Java程序启动后执行
     */
    public static void agentmain(String agentArgs, Instrumentation inst){
        System.err.println("装载成功 方法 agentmain 参数：" + agentArgs);
    }
}
