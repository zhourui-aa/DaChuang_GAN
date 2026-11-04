package com.integrity.assignment.util;

/**
 * 文本清洗：把解析出来的原始文本规范化成「证据定位坐标系」。
 *
 * <p>为什么必须统一清洗：detect_evidence 的 begin_pos/end_pos 相对
 * submissions.raw_text 定位，前端也只能渲染 raw_text；任何一次清洗规则变化
 * 都会让历史证据区间错位。因此清洗规则一经发布即冻结。
 *
 * <p>规则：去 BOM → 统一换行 → 去零宽/控制字符 → 逐行去首尾空白 → 折叠多余空行。
 */
public final class TextCleaner {

    private TextCleaner() {
    }

    public static String clean(String text) {
        if (text == null) {
            return "";
        }
        String s = text;
        // 去 BOM
        if (!s.isEmpty() && s.charAt(0) == '\uFEFF') {
            s = s.substring(1);
        }
        // 统一换行
        s = s.replace("\r\n", "\n").replace('\r', '\n');
        // 去零宽字符与除 \n、\t 外的控制字符
        s = s.replaceAll("[\\u200B-\\u200F\\u202A-\\u202E\\u2060\\uFEFF]", "");
        s = s.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
        // 逐行去首尾空白
        String[] lines = s.split("\n", -1);
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < lines.length; i++) {
            sb.append(lines[i].strip());
            if (i < lines.length - 1) {
                sb.append('\n');
            }
        }
        // 折叠 3 个以上连续换行为 2 个（保留段落分隔）
        String out = sb.toString().replaceAll("\n{3,}", "\n\n");
        return out.strip();
    }
}
