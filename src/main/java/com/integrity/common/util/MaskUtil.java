package com.integrity.common.util;

/**
 * 脱敏工具（手册 §9.4）。
 */
public final class MaskUtil {

    private MaskUtil() {
    }

    /** 姓名：保留姓 + *（张*）。 */
    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        if (name.length() == 1) {
            return name + "*";
        }
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }

    /** 学号：前 6 位 + ******（202304******）。 */
    public static String maskStudentNo(String studentNo) {
        if (studentNo == null || studentNo.isEmpty()) {
            return "";
        }
        if (studentNo.length() <= 6) {
            return studentNo.charAt(0) + "*".repeat(studentNo.length() - 1);
        }
        return studentNo.substring(0, 6) + "*".repeat(studentNo.length() - 6);
    }

    /** 学号后 4 位（导出文件名用）。 */
    public static String last4(String studentNo) {
        if (studentNo == null || studentNo.length() <= 4) {
            return studentNo == null ? "" : studentNo;
        }
        return studentNo.substring(studentNo.length() - 4);
    }
}
