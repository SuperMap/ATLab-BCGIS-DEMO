package com.supermap.atlab;

import java.security.MessageDigest;

public class Utils {

    /**
     * 获取文件后缀名
     * @param fileName 文件名
     * @return
     */
    public static String getExtName(String fileName) {
        if ("".equals(fileName) || null == (fileName)) {
            return fileName;
        }
        int index = fileName.lastIndexOf('.');
        return fileName.substring(index);
    }

    public static String getSHA256(String str) {
        return getSHA256(str.getBytes());
    }

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
}
