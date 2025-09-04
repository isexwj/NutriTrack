/**
 * 文件路径: src/main/java/com/myproject/utils/HashUtils.java
 * 说明: 为当天的 meals payload 计算哈希值（MD5），用于判断记录是否发生变化从而决定是否重新调用 AI。
 *       采用将对象序列化为 JSON 的方式再计算 MD5，保证更稳定的哈希值（避免直接 toString 导致不可控差异）。
 */

package com.myproject.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class HashUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 根据一个对象列表计算 MD5 哈希字符串
     * @param objects 任意对象列表，通常为 meal payload list
     * @return 小写 hex 的 MD5 字符串
     */
    public static String mealsHash(List<?> objects) {
        try {
            String json = mapper.writeValueAsString(objects);
            return DigestUtils.md5DigestAsHex(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            // 回退：如果序列化失败，使用简单拼接
            StringBuilder sb = new StringBuilder();
            if (objects != null) {
                for (Object o : objects) {
                    sb.append(o == null ? "null" : o.toString()).append("|");
                }
            }
            return DigestUtils.md5DigestAsHex(sb.toString().getBytes(StandardCharsets.UTF_8));
        }
    }
}
