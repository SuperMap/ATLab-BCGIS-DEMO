package com.supermap.atlab;

import java.io.*;
import java.security.MessageDigest;

public class Utils {

    /**
     * 获取哈希值
     * @param str
     * @return
     */
    public static String getSHA256(String str) {
        if (null == str) {
            return null;
        }
        return getSHA256(str.getBytes());
    }

    /**
     * 获取哈希值
     * @param bytes
     * @return
     */
    public static String getSHA256(byte[] bytes) {
        if (null == bytes) {
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.reset();
            messageDigest.update(bytes);
            return byte2Hex(messageDigest.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);}
    }

    private static String byte2Hex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

    /**
     * 将目录中的文件名中的特殊字符替换为指定字符
     * @param path  文件目录
     * @param sourceStr 文件名中的特殊字符
     * @param targetStr 替换为该普通字符
     */
    public static void replaceFileName(String path, String sourceStr, String targetStr) {
        File files = new File(path);
        File[] fileList = files.listFiles();
        String names = "";
        for (File file : fileList) {
            String res =  file.getName().replace(sourceStr, targetStr);
            names += "\"" + res + "\", ";
        }
        System.out.println(names);
    }

    public static String inputStreamToString(InputStream inputStream){
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while (true){
            try {
                if (!((line = in.readLine()) != null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            buffer.append(line);
        }
        return buffer.toString();
    }

    /**
     * 获取文件后缀名
     * @param fileName 文件名
     * @return
     */
    public static String getExtName(String fileName) {
        int index = fileName.lastIndexOf('.');
        return fileName.substring(index, fileName.length());
    }
}
