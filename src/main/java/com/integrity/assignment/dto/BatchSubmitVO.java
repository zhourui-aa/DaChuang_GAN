package com.integrity.assignment.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量提交结果（整班上传）。
 */
@Data
public class BatchSubmitVO {

    private int total;
    private int success;
    private int failed;
    private List<Item> items = new ArrayList<>();

    /** 单个文件的处理结果 */
    @Data
    public static class Item {
        private String fileName;
        private String studentNo;
        private boolean success;
        private String message;
        private Long submissionId;

        public static Item ok(String fileName, String studentNo, Long submissionId) {
            Item i = new Item();
            i.fileName = fileName;
            i.studentNo = studentNo;
            i.success = true;
            i.submissionId = submissionId;
            i.message = "已提交";
            return i;
        }

        public static Item fail(String fileName, String studentNo, String message) {
            Item i = new Item();
            i.fileName = fileName;
            i.studentNo = studentNo;
            i.success = false;
            i.message = message;
            return i;
        }
    }
}
