package com.example.inandouttool_identification.utils;


import java.util.HashMap;
import java.util.Map;

public final class Constants {

    private Constants() {
        throw new AssertionError("Cannot instantiate the Constants class");
    }

    public static final String TARGET_IP_ADDRESS = "http://192.168.100.108";

    public static final String PORT = "5000";

    //应付检查用
    public static final Map<String, String> aa = new HashMap<>();
    static {
        aa.put("wzs", "王增双");
        aa.put("sbl", "孙宝利");
        aa.put("yjh", "于江浩");
        aa.put("wy", "王羽");
        aa.put("yyb", "叶亦滨");
        aa.put("null", "王羽");
    }

}
