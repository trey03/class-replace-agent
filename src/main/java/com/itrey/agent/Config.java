package com.itrey.agent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Config {

    /**
     * supperClass -> subClass
     */
    private static Map<String,String> classMap = new ConcurrentHashMap<>(16);

    static {
        //可以从外部读取 ATest为父类 BTest为子类
        classMap.put("com.itrey.ATest","com.itrey.BTest");
    }

    /**
     * 传入type返回type
     * type: com/abc/def/ClassName
     * @param type
     * @return
     */
    public static String getType(String type){
        String source = type.replaceAll("/", ".");
        String target = classMap.get(source);
        if (target!=null) {
            return target.replaceAll("\\.", "/");
        }
        return null;
    }
    /**
     * 传入class返回class
     * class: com.abc.def.ClassName
     * @param className
     * @return
     */
    public static String getClass(String className){
        return classMap.get(className);
    }
}
