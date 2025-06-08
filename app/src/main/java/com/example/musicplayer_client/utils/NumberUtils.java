package com.example.musicplayer_client.utils;

public class NumberUtils {
    
    /**
     * 安全地将字符串转换为long类型
     * @param str 要转换的字符串
     * @return 转换后的long值，如果转换失败则返回0
     */
    public static long safeParseLong(String str) {
        if (str == null || str.trim().isEmpty()) {
            return 0L;
        }
        
        try {
            // 先尝试直接解析为long
            return Long.parseLong(str.trim());
        } catch (NumberFormatException e1) {
            try {
                // 如果失败，尝试先解析为double再转long
                double d = Double.parseDouble(str.trim());
                return (long) d;
            } catch (NumberFormatException e2) {
                // 如果还是失败，返回0
                return 0L;
            }
        }
    }
    
    /**
     * 安全地将字符串转换为int类型
     * @param str 要转换的字符串
     * @return 转换后的int值，如果转换失败则返回0
     */
    public static int safeParseInt(String str) {
        if (str == null || str.trim().isEmpty()) {
            return 0;
        }
        
        try {
            // 先尝试直接解析为int
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e1) {
            try {
                // 如果失败，尝试先解析为double再转int
                double d = Double.parseDouble(str.trim());
                return (int) d;
            } catch (NumberFormatException e2) {
                // 如果还是失败，返回0
                return 0;
            }
        }
    }
} 