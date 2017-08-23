package com.youthlin.avatar;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings("WeakerAccess")
public final class HashUtil {
    /**
     * http://en.gravatar.com/site/implement/images/java/
     */
    public static String hex(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (byte anArray : array) {
            sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString();
    }

    public static String md5(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");//不是线程安全的，要每次创建
            return hex(md.digest(message.getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException ignore) {
        } catch (UnsupportedEncodingException ignore) {
        }
        return "";
    }

    public static void main(String[] args) {
        System.out.println(md5("lin123456陈"));//ba92703368beaba9faa2b2613cfebae2
    }
}
